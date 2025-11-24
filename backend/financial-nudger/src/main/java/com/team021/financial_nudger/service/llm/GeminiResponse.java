package com.team021.financial_nudger.service.llm;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiResponse(List<Candidate> candidates) {

    public Optional<String> firstText() {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        Candidate candidate = candidates.get(0);
        if (candidate.content == null || candidate.content.parts() == null || candidate.content.parts().isEmpty()) {
            return Optional.empty();
        }
        return candidate.content.parts()
                .stream()
                .map(GeminiPart::text)
                .filter(text -> text != null && !text.isBlank())
                .findFirst();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Candidate(Content content) { }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Content(List<GeminiPart> parts) { }
}

