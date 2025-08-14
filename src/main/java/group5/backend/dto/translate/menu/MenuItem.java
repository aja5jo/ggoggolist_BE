package group5.backend.dto.translate.menu;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MenuItem {
    String section;         // 섹션명(없으면 null)
    String originalName;    // OCR 원문
    String translatedName;  // 번역 결과
    String price;           // "8,000원" 등 (번역 안함)
    String option;          // 곱빼기/세트/핫/아이스 등 (있으면)
}

