package com.exempal.shiftcounter.features.comment.adapter.persistence;

import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loss_explanations")
@Getter
@Setter
@NoArgsConstructor
public class LossExplanationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stoppage_id", nullable = false)
    private Long stoppageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LossCategory category;

    @Column(nullable = false)
    private String comment = "";

    @Column(name = "allocated_minutes", nullable = false)
    private int allocatedMinutes;

    @Column(name = "allocated_cans", nullable = false)
    private int allocatedCans;
}
