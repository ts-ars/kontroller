package com.exempal.shiftcounter.features.comment.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LossExplanationJpaRepository extends JpaRepository<LossExplanationEntity, Long> {
    List<LossExplanationEntity> findByStoppageIdOrderById(long stoppageId);
}
