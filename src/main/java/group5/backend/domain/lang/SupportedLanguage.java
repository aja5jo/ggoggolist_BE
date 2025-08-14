package group5.backend.domain.lang;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지원 대상 언어")
public enum SupportedLanguage {
    KOREAN("ko", "한국어"),
    ENGLISH("en", "English"),
    JAPANESE("ja", "日本語"),
    CHINESE("zh-CN", "中文(简体)"),
    FRENCH("fr", "Français"),
    ARABIC("ar", "العربية"),
    VIETNAMESE("vi", "Tiếng Việt"),
    THAI("th", "ไทย"),
    ITALIAN("it", "Italiano"),
    SPANISH("es", "Español"),
    GERMAN("de", "Deutsch");

    private final String code;
    private final String label;

    SupportedLanguage(String code, String label) {
        this.code = code;
        this.label = label;
    }
    public String code() { return code; }
    public String label() { return label; }
}