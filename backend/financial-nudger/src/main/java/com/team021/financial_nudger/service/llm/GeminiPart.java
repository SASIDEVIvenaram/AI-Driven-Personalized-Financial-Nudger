package com.team021.financial_nudger.service.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiPart(
        String text,
        @JsonProperty("inline_data") InlineData inlineData
) {

    public static GeminiPart text(String value) {
        return new GeminiPart(value, null);
    }

    public static GeminiPart inlineData(String mimeType, String base64Data) {
        return new GeminiPart(null, new InlineData(mimeType, base64Data));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record InlineData(
            @JsonProperty("mime_type") String mimeType,
            String data
    ) { }
}

