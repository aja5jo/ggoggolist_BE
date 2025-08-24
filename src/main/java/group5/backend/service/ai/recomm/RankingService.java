package group5.backend.service.ai.recomm;

import com.fasterxml.jackson.databind.ObjectMapper;
import group5.backend.config.ai.OpenAiProperties;
import group5.backend.domain.event.Event;
import group5.backend.domain.popup.Popup;
import group5.backend.domain.store.Store;
import group5.backend.domain.recomm.ItemEmbedding;
import group5.backend.domain.recomm.ItemType;
import group5.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    public record Scored(ItemCandidate item, double score) {}

    private final ItemEmbeddingRepository itemEmbRepo;
    private final StoreRepository storeRepository;
    private final EventRepository eventRepository;
    private final PopupRepository popupRepository;

    private final OpenAIEmbeddingClient openai;
    private final OpenAiProperties props;

    private final ObjectMapper om = new ObjectMapper();

    public List<Scored> rank(float[] userVec, List<ItemCandidate> candidates, int size) {
        // 0) 없으면 생성 (최소 변경)
        hydrateMissingEmbeddings(candidates);

        Map<Long, float[]> storeVecs = batchVec(ItemType.STORE, idsOf(candidates, ItemType.STORE));
        Map<Long, float[]> eventVecs = batchVec(ItemType.EVENT, idsOf(candidates, ItemType.EVENT));
        Map<Long, float[]> popupVecs = batchVec(ItemType.POPUP, idsOf(candidates, ItemType.POPUP));

        List<Scored> list = new ArrayList<>(candidates.size());
        for (var c : candidates) {
            float[] v = switch (c.type()) {
                case STORE -> storeVecs.get(c.id());
                case EVENT -> eventVecs.get(c.id());
                case POPUP -> popupVecs.get(c.id());
            };
            if (v == null) continue;

            double sim = cosine(userVec, v);
            double like = Math.tanh(Math.log(1 + c.likeCount()) / 5.0);
            double recent = recentness(c);

            double score = 0.75*sim + 0.10*recent + 0.10*like + 0.05*0.0;
            list.add(new Scored(c, score));
        }

        list.sort(Comparator.comparingDouble(Scored::score).reversed()
                .thenComparing(s -> s.item().likeCount(), Comparator.reverseOrder())
                .thenComparing(s -> s.item().id(), Comparator.reverseOrder()));

        return list.size() > size ? list.subList(0, size) : list;
    }

    /* ===== 아래는 내부 유틸 ===== */

    private Map<Long, float[]> batchVec(ItemType type, List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        var rows = itemEmbRepo.findByItemTypeAndItemIdInAndModel(type, ids, openai.modelName());
        Map<Long, float[]> m = new HashMap<>(rows.size());
        for (var r : rows) m.put(r.getItemId(), fromJson(r.getVecJson()));
        return m;
    }

    private List<Long> idsOf(List<ItemCandidate> cs, ItemType t) {
        List<Long> ids = new ArrayList<>();
        for (var c : cs) if (c.type()==t) ids.add(c.id());
        return ids;
    }

    private double recentness(ItemCandidate c) {
        if (c.startDate()!=null && c.endDate()!=null) {
            var today = LocalDate.now();
            if (today.isAfter(c.endDate())) return 0.0;
            long daysToEnd = ChronoUnit.DAYS.between(today, c.endDate());
            return daysToEnd <= 0 ? 1.0 : 1.0 / (1.0 + daysToEnd / 7.0);
        }
        return 0.3;
    }

    private static double cosine(float[] a, float[] b) {
        if (a==null || b==null || a.length!=b.length) return 0.0;
        double dot=0, na=0, nb=0;
        for (int i=0;i<a.length;i++){ dot+=a[i]*b[i]; na+=a[i]*a[i]; nb+=b[i]*b[i]; }
        if (na==0 || nb==0) return 0.0;
        return dot / (Math.sqrt(na)*Math.sqrt(nb));
    }

    private float[] fromJson(String s) {
        try { return om.readValue(s, float[].class); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    /** 후보 중 임베딩 없는 아이템은 즉시 생성 후 저장 */
    public void hydrateMissingEmbeddings(List<ItemCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) return;

        Map<ItemType, List<Long>> idsByType = new EnumMap<>(ItemType.class);
        idsByType.put(ItemType.STORE, idsOf(candidates, ItemType.STORE));
        idsByType.put(ItemType.EVENT, idsOf(candidates, ItemType.EVENT));
        idsByType.put(ItemType.POPUP, idsOf(candidates, ItemType.POPUP));

        Map<ItemType, List<Long>> missing = new EnumMap<>(ItemType.class);
        for (var e : idsByType.entrySet()) {
            var type = e.getKey();
            var ids  = e.getValue();
            if (ids.isEmpty()) { missing.put(type, List.of()); continue; }

            var existing = itemEmbRepo.findByItemTypeAndItemIdInAndModel(type, ids, openai.modelName())
                    .stream().map(ItemEmbedding::getItemId).collect(Collectors.toSet());
            var miss = ids.stream().filter(id -> !existing.contains(id)).toList();
            missing.put(type, miss);
        }

        int totalMissing = missing.values().stream().mapToInt(List::size).sum();
        if (totalMissing == 0) return;

        log.debug("[RANK] missing embeddings store/event/popup = {}/{}/{}",
                missing.get(ItemType.STORE).size(),
                missing.get(ItemType.EVENT).size(),
                missing.get(ItemType.POPUP).size());

        final int LIMIT_NEW = 120; // 과금 보호

        // STORE
        if (!missing.get(ItemType.STORE).isEmpty()) {
            var pick = missing.get(ItemType.STORE).stream().limit(LIMIT_NEW).toList();
            for (Store s : storeRepository.findAllById(pick)) {
                var text = embedText(s);
                var vec = safeEmbed(text);
                if (vec != null) saveEmbedding(ItemType.STORE, s.getId(), vec);
            }
        }
        // EVENT
        if (!missing.get(ItemType.EVENT).isEmpty()) {
            var pick = missing.get(ItemType.EVENT).stream().limit(LIMIT_NEW).toList();
            for (Event e : eventRepository.findAllById(pick)) {
                var text = embedText(e);
                var vec = safeEmbed(text);
                if (vec != null) saveEmbedding(ItemType.EVENT, e.getId(), vec);
            }
        }
        // POPUP
        if (!missing.get(ItemType.POPUP).isEmpty()) {
            var pick = missing.get(ItemType.POPUP).stream().limit(LIMIT_NEW).toList();
            for (Popup p : popupRepository.findAllById(pick)) {
                var text = embedText(p);
                var vec = safeEmbed(text);
                if (vec != null) saveEmbedding(ItemType.POPUP, p.getId(), vec);
            }
        }
    }

    private String embedText(Store s) {
        return String.join(" ",
                "Store", nz(s.getName()), nz(s.getIntro()),
                "Category", String.valueOf(s.getCategory()),
                "Address", nz(s.getAddress())
        );
    }

    private String embedText(Event e) {
        var df = DateTimeFormatter.ISO_LOCAL_DATE;
        return String.join(" ",
                "Event", nz(e.getName()), nz(e.getDescription()),
                "Start", e.getStartDate()!=null?e.getStartDate().format(df):"",
                "End",   e.getEndDate()!=null?e.getEndDate().format(df):""
        );
    }

    private String embedText(Popup p) {
        var df = DateTimeFormatter.ISO_LOCAL_DATE;
        return String.join(" ",
                "Popup", nz(p.getName()), nz(p.getDescription()),
                "Category", String.valueOf(p.getCategory()),
                "Address", nz(p.getAddress()),
                "Start", p.getStartDate()!=null?p.getStartDate().format(df):"",
                "End",   p.getEndDate()!=null?p.getEndDate().format(df):""
        );
    }

    private String nz(String s) { return s == null ? "" : s; }

    private float[] safeEmbed(String text) {
        try { return openai.embed(text); }
        catch (Exception ex) {
            log.warn("[RANK] embed failed: {}", ex.toString());
            return null;
        }
    }

    private void saveEmbedding(ItemType type, Long id, float[] vec) {
        try {
            String json = om.writeValueAsString(vec);
            ItemEmbedding row = ItemEmbedding.builder()
                    .itemType(type)
                    .itemId(id)
                    .model(openai.modelName())
                    .dim(props.getEmbeddingDim())
                    .vecJson(json)
                    .build();
            itemEmbRepo.save(row);
        } catch (Exception e) {
            log.warn("[RANK] saveEmbedding failed type={}, id={}", type, id, e);
        }
    }
}
