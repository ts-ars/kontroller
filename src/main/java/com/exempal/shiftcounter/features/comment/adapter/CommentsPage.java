package com.exempal.shiftcounter.features.comment.adapter;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.comment.adapter.dto.CommentRowDto;
import com.exempal.shiftcounter.features.comment.adapter.mapper.CommentRowMapper;
import com.exempal.shiftcounter.features.comment.application.CommentsReadUseCase;
import com.exempal.shiftcounter.features.comment.application.StoppageTimeService;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;

@Component
public class CommentsPage implements PageModel {

    private final CommentsReadUseCase useCase;
    private final StoppageTimeService timeService;
    private final ActualDataPort actualDataPort;

    public CommentsPage(CommentsReadUseCase useCase, StoppageTimeService timeService, ActualDataPort actualDataPort) {
        this.useCase = useCase;
        this.timeService = timeService;
        this.actualDataPort = actualDataPort;
    }

    @Override public String getPageName() { return "comment"; }

    @Override
    public void populateModel(Model model) {
        var date = LocalDate.now();
        var data = useCase.read(date);

        if (data.shift() == null) {
            model.addAttribute("rows", List.of());
            model.addAttribute("alerts", List.of("No shift found for today"));
            return;
        }

        actualDataPort.findByDate(date).ifPresent(id -> model.addAttribute("shiftid",id));

        List<CommentRowDto> rows = data.rows().stream()
                .map(CommentRowMapper::toDto)
                .toList();

        // Формирование человеко читаемых алертов — это ответственность UI
        var alerts = data.missing().stream()
                .map(e -> String.format(
                        "Missing explanation for stoppage at %s — please add a comment.",
                        timeService.getPreciseTime(e, data.shift())
                ))
                .toList();

        model.addAttribute("rows", rows);
        model.addAttribute("alerts", alerts);
    }
}