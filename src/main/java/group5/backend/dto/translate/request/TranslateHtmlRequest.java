package group5.backend.dto.translate.request;

import group5.backend.domain.lang.SupportedLanguage;

public record TranslateHtmlRequest(
        String html,
        SupportedLanguage target // 예: ENGLISH, JAPANESE ...
) {}