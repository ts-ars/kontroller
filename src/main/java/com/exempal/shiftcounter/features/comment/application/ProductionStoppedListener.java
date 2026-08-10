package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.shared.event.ProductionStoppedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductionStoppedListener {
    private final ReconcileStoppagesUseCase reconcile;

    @EventListener
    public void onProductionStopped(ProductionStoppedEvent event) {
        reconcile.reconcile(new ReconcileStoppagesCommand(event.getTime().toLocalDate(),
                Stoppage.PRIMARY_SENSOR, null, event.getTime()));
    }
}
