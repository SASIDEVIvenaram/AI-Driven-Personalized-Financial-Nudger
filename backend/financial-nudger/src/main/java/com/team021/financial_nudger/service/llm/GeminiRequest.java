package com.team021.financial_nudger.service.llm;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiRequest(
        List<GeminiContent> contents
) {

    public static GeminiRequest jsonRequest(String instructions, List<GeminiPart> dynamicParts) {
        List<GeminiPart> parts = new ArrayList<>();
        parts.add(GeminiPart.text(instructions));
        if (dynamicParts != null) {
            parts.addAll(dynamicParts);
        }

        // ✅ No generation_config block — Gemini v1 handles JSON automatically
        return new GeminiRequest(List.of(new GeminiContent(parts)));
    }
}
