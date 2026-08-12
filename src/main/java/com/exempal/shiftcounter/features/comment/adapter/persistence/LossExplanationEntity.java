package com.exempal.shiftcounter.features.comment.adapter.persistence;

import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "loss_explanations")
@Getter
@Setter
@NoArgsConstructor
public class LossExplanationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stoppage_id", nullable = false)
    private StoppageEntity stoppage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LossCategory category;

    @Column(nullable = false)
    private String comment = "";

    @Column(name = "allocated_minutes", nullable = false)
    private int allocatedMinutes;

    @Column(name = "allocated_cans", nullable = false)
    private int allocatedCans;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_user_id", nullable = false, updatable = false)
    private com.exempal.shiftcounter.features.user.adapter.persistence.AppUserEntity author;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by")
    private com.exempal.shiftcounter.features.user.adapter.persistence.AppUserEntity lastModifiedBy;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
