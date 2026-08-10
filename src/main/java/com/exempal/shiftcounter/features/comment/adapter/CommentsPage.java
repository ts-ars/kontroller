package com.exempal.shiftcounter.features.comment.adapter;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.comment.adapter.dto.LossExplanationResponse;
import com.exempal.shiftcounter.features.comment.adapter.dto.LossRowDto;
import com.exempal.shiftcounter.features.comment.application.LossExplanationUseCase;
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
    private final LossExplanationUseCase explanations;

    public CommentsPage(CommentsReadUseCase useCase, StoppageTimeService timeService,
                        LossExplanationUseCase explanations) {
        this.useCase = useCase;
        this.timeService = timeService;
        this.explanations = explanations;
    }

    @Override public String getPageName() { return "comment"; }

    @Override
    public void populateModel(Model model) {
        var date = LocalDate.now();
        var data = useCase.read(date);

        if (data.shift() == null) {
            model.addAttribute("losses", List.of());
            model.addAttribute("alerts", List.of("No shift found for today"));
            return;
        }

        model.addAttribute("shiftId", data.shift().getId());

        List<LossRowDto> rows = data.rows().stream()
                .filter(entry -> entry.getType() != null && !entry.getType().isUserEditable())
                .map(entry -> {
                    var childRows = explanations.findByStoppage(entry.getId()).stream()
                            .map(LossExplanationResponse::from)
                            .toList();
                    long rounded = Math.round(entry.getMinutes());
                    long allocated = childRows.stream().mapToLong(LossExplanationResponse::allocatedMinutes).sum();
                    return new LossRowDto(entry.getId(), timeService.getPreciseTime(entry, data.shift()),
                            rounded, entry.getCans(), entry.getType().name(), allocated,
                            Math.max(0, rounded - allocated), childRows);
                })
                .toList();

        // Формирование человеко читаемых алертов — это ответственность UI
        var alerts = data.missing().stream()
                .map(e -> String.format(
                        "Missing explanation for stoppage at %s — please add a comment.",
                        timeService.getPreciseTime(e, data.shift())
                ))
                .toList();

        model.addAttribute("losses", rows);
        model.addAttribute("alerts", alerts);
    }
}
