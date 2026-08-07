package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.comment.domain.StoppageRepository;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.exempal.shiftcounter.features.comment.domain.StoppageComparators.chronological;

@Service
public class CommentsReadService implements CommentsReadUseCase {

    private final StoppageRepository repository;
    private final ActualDataPort actualDataPort;
    private final LossExplanationRepository explanations;

    public CommentsReadService(StoppageRepository repository, ActualDataPort actualDataPort,
                               LossExplanationRepository explanations) {
        this.repository = repository;
        this.actualDataPort = actualDataPort;
        this.explanations = explanations;
    }

    @Override
    public Data read(LocalDate date) {
        List<StoppageEntry> all = repository.findByShiftDate(date);
        Shift shift = actualDataPort.findByDate(date).orElse(null);
        if (shift == null) return new Data(null, List.of(), List.of());

        // 1) Показать ВСЕ записи (и авто, и пользовательские)
        List<StoppageEntry> rows = new ArrayList<>(all);
        rows.sort(chronological());

        // 2) Подсветить «missing» именно для авто-строк без пояснений
        List<StoppageEntry> missing = rows.stream()
                .filter(e -> e.getType() != null && !e.getType().isUserEditable()) // FIXED/TEMPO
                .filter(e -> explanations.findByStoppageId(e.getId()).isEmpty())
                .toList();

        return new Data(shift, rows, missing);
    }
}
