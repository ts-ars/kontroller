package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import com.exempal.shiftcounter.features.shift.application.ShiftAnalyticsUseCase;
import com.exempal.shiftcounter.shared.event.ProductionStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ProductionStoppedListener {

    private final StoppageRepository stoppageRepository;
    private final ShiftAnalyticsUseCase analytics;
    private final SettingsPort settings;

    public ProductionStoppedListener(
            StoppageRepository stoppageRepository,
            ShiftAnalyticsUseCase analytics,
            SettingsPort settings
    ) {
        this.stoppageRepository = stoppageRepository;
        this.analytics = analytics;
        this.settings = settings;
    }

    @EventListener
    public void onProductionStopped(ProductionStoppedEvent event) {
        String timeStr = event.getTime().toLocalTime().toString();
        LocalDate date = event.getTime().toLocalDate();

        double minutes = event.getMinutes();
        int ppm = settings.getPpm(); // Получаем актуальную скорость

        int lostCans = analytics.calculateLostCans(minutes, ppm);

        StoppageEntry entry = new StoppageEntry(
                timeStr,
                minutes,
                lostCans,
                null,
                null,
                date
        );

        stoppageRepository.save(entry);
    }
}
