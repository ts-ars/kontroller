package com.exempal.shiftcounter.features.comment.adapter;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.comment.adapter.dto.LossExplanationResponse;
import com.exempal.shiftcounter.features.comment.adapter.dto.LossRowDto;
import com.exempal.shiftcounter.features.comment.application.CommentsReadUseCase;
import com.exempal.shiftcounter.features.comment.application.StoppageTimeService;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;

@Component
public class CommentsPage implements PageModel {
    private final CommentsReadUseCase useCase;
    private final StoppageTimeService timeService;

    public CommentsPage(CommentsReadUseCase useCase, StoppageTimeService timeService) {
        this.useCase = useCase;
        this.timeService = timeService;
    }

    @Override public String getPageName() { return "comment"; }

    @Override
    public void populateModel(Model model) {
        var data = useCase.read(LocalDate.now());
        if (data.shift() == null) {
            model.addAttribute("losses", List.of());
            model.addAttribute("alerts", List.of("No shift found for today"));
            return;
        }
        model.addAttribute("shiftId", data.shift().getId());
        List<LossRowDto> rows = data.rows().stream().map(stoppage -> {
            var childRows = stoppage.explanations().stream().map(LossExplanationResponse::from).toList();
            long allocated = stoppage.allocatedMinutes();
            return new LossRowDto(stoppage.id(), stoppage.detectionKey(), timeService.getPreciseTime(stoppage),
                    stoppage.roundedMinutes(), stoppage.lostCans(), stoppage.detectionType().name(),
                    stoppage.state(), stoppage.explanationStatus(), allocated,
                    Math.max(0, stoppage.roundedMinutes() - allocated), stoppage.version(), childRows);
        }).toList();
        var alerts = data.missing().stream()
                .map(value -> "Missing explanation for stoppage at " + timeService.getPreciseTime(value))
                .toList();
        model.addAttribute("losses", rows);
        model.addAttribute("alerts", alerts);
    }
}
