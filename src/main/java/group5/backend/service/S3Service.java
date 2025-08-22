package group5.backend.service;



import group5.backend.config.s3.S3Config;
import group5.backend.dto.s3.PresignedDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Config s3Config;

    public List<PresignedDto> createUploadUrls(String dir, int count, String contentType) {
        String safeDir = switch (dir) {
            case "events", "popups", "stores" -> dir;
            default -> "misc";
        };
        String ext = extFromContentType(contentType);
        String date = LocalDate.now().toString();

        return IntStream.range(0, Math.max(1, count))
                .mapToObj(i -> {
                    String key = "public/%s/%s/%s.%s".formatted(safeDir, date, UUID.randomUUID(), ext);
                    String uploadUrl = presignPut(key, contentType, Duration.ofMinutes(10));
                    String publicUrl = toPublicUrl(key);
                    return new PresignedDto(key, uploadUrl, publicUrl);
                })
                .toList();
    }

    private String presignPut(String key, String contentType, Duration ttl) {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(s3Config.getBucket())
                .key(key)
                .contentType(contentType)
                .cacheControl("public, max-age=31536000, immutable")
                .build();

        PresignedPutObjectRequest req = s3Presigner.presignPutObject(b -> b
                .signatureDuration(ttl)
                .putObjectRequest(put));
        return req.url().toString();
    }

    private String toPublicUrl(String key) {
        // CloudFront를 쓴다면 여기서 CF 도메인으로 변환
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(
                s3Config.getBucket(), "ap-northeast-2", urlEncodePath(key)
        );
    }

    private String urlEncodePath(String path) {
        return Arrays.stream(path.split("/"))
                .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
                .reduce((a, b) -> a + "/" + b).orElse("");
    }

    private String extFromContentType(String ct) {
        return switch (ct) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "bin";
        };
    }
}

