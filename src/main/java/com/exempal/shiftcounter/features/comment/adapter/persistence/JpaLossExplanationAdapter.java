package com.exempal.shiftcounter.features.comment.adapter.persistence;

import com.exempal.shiftcounter.features.comment.application.LossExplanationRepository;
import com.exempal.shiftcounter.features.comment.domain.LossExplanation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaLossExplanationAdapter implements LossExplanationRepository {
    private final LossExplanationJpaRepository repository;

    @Override
    public List<LossExplanation> findByStoppageId(long stoppageId) {
        return repository.findByStoppageIdOrderById(stoppageId).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<LossExplanation> findById(long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public LossExplanation save(LossExplanation explanation) {
        LossExplanationEntity entity = new LossExplanationEntity();
        entity.setId(explanation.id());
        entity.setStoppageId(explanation.stoppageId());
        entity.setCategory(explanation.category());
        entity.setComment(explanation.comment());
        entity.setAllocatedMinutes(explanation.allocatedMinutes());
        entity.setAllocatedCans(explanation.allocatedCans());
        return toDomain(repository.save(entity));
    }

    @Override
    public void delete(LossExplanation explanation) {
        repository.deleteById(explanation.id());
    }

    private LossExplanation toDomain(LossExplanationEntity entity) {
        return new LossExplanation(entity.getId(), entity.getStoppageId(), entity.getCategory(),
                entity.getComment(), entity.getAllocatedMinutes(), entity.getAllocatedCans());
    }
}
