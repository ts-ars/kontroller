package com.exempal.shiftcounter.features.comment.adapter.web;

import com.exempal.shiftcounter.features.comment.adapter.dto.LossExplanationRequest;
import com.exempal.shiftcounter.features.comment.adapter.dto.LossExplanationResponse;
import com.exempal.shiftcounter.features.comment.application.LossExplanationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stoppages/{stoppageId}/explanations")
@RequiredArgsConstructor
public class LossExplanationController {
    private final LossExplanationUseCase useCase;

    @GetMapping
    public List<LossExplanationResponse> findAll(@PathVariable long stoppageId) {
        return useCase.findByStoppage(stoppageId).stream().map(LossExplanationResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LossExplanationResponse create(@PathVariable long stoppageId,
                                          @Valid @RequestBody LossExplanationRequest request) {
        return LossExplanationResponse.from(useCase.create(stoppageId, request.category(), request.comment(),
                request.allocatedMinutes()));
    }

    @PutMapping("/{explanationId}")
    public LossExplanationResponse update(@PathVariable long stoppageId,
                                          @PathVariable long explanationId,
                                          @Valid @RequestBody LossExplanationRequest request) {
        return LossExplanationResponse.from(useCase.update(stoppageId, explanationId, request.category(),
                request.comment(), request.allocatedMinutes()));
    }

    @DeleteMapping("/{explanationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long stoppageId, @PathVariable long explanationId) {
        useCase.delete(stoppageId, explanationId);
    }
}
