package com.exempal.shiftcounter.features.comment.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.time.Instant;

public final class Stoppage {
    public static final String SENSOR_1 = com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1;
    @Deprecated(forRemoval = false)
    public static final String PRIMARY_SENSOR = SENSOR_1;

    private final Long id;
    private final UUID detectionKey;
    private final UUID incidentKey;
    private final long shiftId;
    private final String sensorKey;
    private final int intervalIndex;
    private final LocalDateTime startedAt;
    private final Duration exactDuration;
    private final int roundedMinutes;
    private final int lostCans;
    private final DetectionType detectionType;
    private final StoppageState state;
    private final List<LossExplanation> explanations;
    private final long version;

    public Stoppage(Long id, UUID detectionKey, long shiftId, String sensorKey, int intervalIndex,
                    LocalDateTime startedAt, Duration exactDuration, int roundedMinutes, int lostCans,
                    DetectionType detectionType, StoppageState state,
                    List<LossExplanation> explanations, long version) {
        this(id, detectionKey, detectionKey, shiftId, sensorKey, intervalIndex, startedAt, exactDuration,
                roundedMinutes, lostCans, detectionType, state, explanations, version);
    }

    public Stoppage(Long id, UUID detectionKey, UUID incidentKey, long shiftId, String sensorKey,
                    int intervalIndex, LocalDateTime startedAt, Duration exactDuration,
                    int roundedMinutes, int lostCans, DetectionType detectionType, StoppageState state,
                    List<LossExplanation> explanations, long version) {
        this.id = id;
        this.detectionKey = Objects.requireNonNull(detectionKey, "detectionKey");
        this.incidentKey = Objects.requireNonNull(incidentKey, "incidentKey");
        if (shiftId <= 0) throw new IllegalArgumentException("shiftId must be positive");
        this.shiftId = shiftId;
        this.sensorKey = requireText(sensorKey, "sensorKey");
        if (intervalIndex < 0) throw new IllegalArgumentException("intervalIndex must not be negative");
        this.intervalIndex = intervalIndex;
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.exactDuration = Objects.requireNonNull(exactDuration, "exactDuration");
        if (exactDuration.isNegative()) throw new IllegalArgumentException("exactDuration must not be negative");
        if (roundedMinutes != roundHalfUpMinutes(exactDuration)) {
            throw new IllegalArgumentException("roundedMinutes must be derived from exactDuration");
        }
        this.roundedMinutes = roundedMinutes;
        if (lostCans < 0) throw new IllegalArgumentException("lostCans must not be negative");
        this.lostCans = lostCans;
        this.detectionType = Objects.requireNonNull(detectionType, "detectionType");
        this.state = Objects.requireNonNull(state, "state");
        this.explanations = List.copyOf(explanations == null ? List.of() : explanations);
        if (version < 0) throw new IllegalArgumentException("version must not be negative");
        this.version = version;
        if (id != null && this.explanations.stream().anyMatch(e -> e.stoppageId() != id)) {
            throw new IllegalArgumentException("explanation belongs to another stoppage");
        }
    }

    public static Stoppage detected(UUID detectionKey, long shiftId, String sensorKey, int intervalIndex,
                                    LocalDateTime startedAt, Duration duration, int lostCans,
                                    DetectionType detectionType) {
        return new Stoppage(null, detectionKey, shiftId, sensorKey, intervalIndex, startedAt, duration,
                roundHalfUpMinutes(duration), lostCans, detectionType, StoppageState.ACTIVE, List.of(), 0L);
    }

    public static Stoppage detected(UUID detectionKey, UUID incidentKey, long shiftId, String sensorKey,
                                    int intervalIndex, LocalDateTime startedAt, Duration duration,
                                    int lostCans, DetectionType detectionType) {
        return new Stoppage(null, detectionKey, incidentKey, shiftId, sensorKey, intervalIndex, startedAt,
                duration, roundHalfUpMinutes(duration), lostCans, detectionType, StoppageState.ACTIVE,
                List.of(), 0L);
    }

    public Stoppage resolve() {
        return copy(exactDuration, roundedMinutes, lostCans, StoppageState.RESOLVED, explanations);
    }

    public Stoppage withLostCans(int cans) {
        if (cans < 0) throw new IllegalArgumentException("lostCans must not be negative");
        return copy(exactDuration, roundedMinutes, cans, state,
                rebalanceExplanations(explanations, roundedMinutes, cans));
    }

    public Stoppage withSystemMeasurement(LocalDateTime newStartedAt, Duration newDuration, int newLostCans) {
        Objects.requireNonNull(newStartedAt, "startedAt");
        Objects.requireNonNull(newDuration, "exactDuration");
        if (newDuration.isNegative()) throw new IllegalArgumentException("exactDuration must not be negative");
        if (newLostCans < 0) throw new IllegalArgumentException("lostCans must not be negative");
        return new Stoppage(id, detectionKey, incidentKey, shiftId, sensorKey, intervalIndex, newStartedAt,
                newDuration, roundHalfUpMinutes(newDuration), newLostCans, detectionType, state,
                rebalanceExplanations(explanations, roundHalfUpMinutes(newDuration), newLostCans), version);
    }

    public Stoppage addExplanation(LossCategory category, String comment, int allocatedMinutes) {
        return addExplanation(category, comment, allocatedMinutes, null, "", null);
    }

    public Stoppage addExplanation(LossCategory category, String comment, int allocatedMinutes,
                                   UUID authorUserId, String authorDisplayName, Instant now) {
        validateNormalAllocation(allocatedMinutes, null);
        List<LossExplanation> updated = new ArrayList<>(explanations);
        updated.add(new LossExplanation(null, requirePersistedId(), category, comment, allocatedMinutes, 0,
                authorUserId, authorDisplayName, now, now, null, 0L));
        return copy(exactDuration, roundedMinutes, lostCans, state,
                rebalanceExplanations(updated, roundedMinutes, lostCans));
    }

    public Stoppage updateExplanation(long explanationId, LossCategory category, String comment,
                                      int allocatedMinutes) {
        return updateExplanation(explanationId, category, comment, allocatedMinutes, null, null);
    }

    public Stoppage updateExplanation(long explanationId, LossCategory category, String comment,
                                      int allocatedMinutes, UUID modifiedBy, Instant now) {
        LossExplanation current = explanation(explanationId);
        validateNormalAllocation(allocatedMinutes, current.id());
        List<LossExplanation> updated = explanations.stream()
                .map(value -> value.id().equals(explanationId)
                        ? new LossExplanation(value.id(), requirePersistedId(), category, comment,
                        allocatedMinutes, 0, value.authorUserId(), value.authorDisplayName(),
                        value.createdAt(), now == null ? value.updatedAt() : now, modifiedBy, value.version())
                        : value)
                .toList();
        return copy(exactDuration, roundedMinutes, lostCans, state,
                rebalanceExplanations(updated, roundedMinutes, lostCans));
    }

    public Stoppage removeExplanation(long explanationId) {
        explanation(explanationId);
        return copy(exactDuration, roundedMinutes, lostCans, state,
                explanations.stream().filter(value -> !value.id().equals(explanationId)).toList());
    }

    public ExplanationStatus explanationStatus() {
        long allocated = allocatedMinutes();
        if (allocated == 0) return ExplanationStatus.UNEXPLAINED;
        if (allocated < roundedMinutes) return ExplanationStatus.PARTIALLY_EXPLAINED;
        if (allocated == roundedMinutes) return ExplanationStatus.FULLY_EXPLAINED;
        return ExplanationStatus.ALLOCATION_CONFLICT;
    }

    public long allocatedMinutes() {
        return explanations.stream().mapToLong(LossExplanation::allocatedMinutes).sum();
    }

    private void validateNormalAllocation(int requestedMinutes, Long replacedId) {
        if (requestedMinutes < 0) throw new IllegalArgumentException("allocatedMinutes must not be negative");
        long total = explanations.stream()
                .filter(value -> replacedId == null || !replacedId.equals(value.id()))
                .mapToLong(LossExplanation::allocatedMinutes)
                .sum() + requestedMinutes;
        if (total > roundedMinutes) throw new IllegalArgumentException("allocated minutes exceed stoppage rounded minutes");
    }

    private LossExplanation explanation(long explanationId) {
        return explanations.stream().filter(value -> value.id() != null && value.id() == explanationId)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("explanation not found"));
    }

    private List<LossExplanation> rebalanceExplanations(List<LossExplanation> source,
                                                        int systemMinutes, int systemCans) {
        long totalMinutes = source.stream().mapToLong(LossExplanation::allocatedMinutes).sum();
        if (source.isEmpty() || totalMinutes == 0 || systemCans == 0) {
            return source.stream().map(value -> withAllocatedCans(value, 0)).toList();
        }
        long target = systemMinutes <= 0 || totalMinutes >= systemMinutes
                ? systemCans
                : Math.round((double) systemCans * totalMinutes / systemMinutes);
        record Share(int index, int floor, double remainder, Long id) {}
        List<Share> shares = new ArrayList<>();
        int floorSum = 0;
        for (int index = 0; index < source.size(); index++) {
            LossExplanation value = source.get(index);
            double exact = (double) target * value.allocatedMinutes() / totalMinutes;
            int floor = (int) Math.floor(exact);
            floorSum += floor;
            shares.add(new Share(index, floor, exact - floor, value.id()));
        }
        int[] cans = shares.stream().mapToInt(Share::floor).toArray();
        shares.stream().sorted((left, right) -> {
            int remainder = Double.compare(right.remainder(), left.remainder());
            if (remainder != 0) return remainder;
            if (left.id() == null && right.id() != null) return 1;
            if (left.id() != null && right.id() == null) return -1;
            if (left.id() != null) {
                int id = Long.compare(left.id(), right.id());
                if (id != 0) return id;
            }
            return Integer.compare(left.index(), right.index());
        }).limit(target - floorSum).forEach(share -> cans[share.index()]++);
        List<LossExplanation> result = new ArrayList<>(source.size());
        for (int index = 0; index < source.size(); index++) {
            result.add(withAllocatedCans(source.get(index), cans[index]));
        }
        return List.copyOf(result);
    }

    private LossExplanation withAllocatedCans(LossExplanation value, int cans) {
        return new LossExplanation(value.id(), value.stoppageId(), value.category(), value.comment(),
                value.allocatedMinutes(), cans, value.authorUserId(), value.authorDisplayName(),
                value.createdAt(), value.updatedAt(), value.lastModifiedBy(), value.version());
    }

    private long requirePersistedId() {
        if (id == null) throw new IllegalStateException("explanations require a persisted stoppage");
        return id;
    }

    private Stoppage copy(Duration duration, int rounded, int cans, StoppageState newState,
                          List<LossExplanation> updatedExplanations) {
        return new Stoppage(id, detectionKey, incidentKey, shiftId, sensorKey, intervalIndex, startedAt, duration,
                rounded, cans, detectionType, newState, updatedExplanations, version);
    }

    public static int roundHalfUpMinutes(Duration duration) {
        if (duration.isNegative()) throw new IllegalArgumentException("duration must not be negative");
        return Math.toIntExact(Math.addExact(duration.getSeconds(), 30L) / 60L);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value.trim();
    }

    public Long id() { return id; }
    public UUID detectionKey() { return detectionKey; }
    public UUID incidentKey() { return incidentKey; }
    public long shiftId() { return shiftId; }
    public String sensorKey() { return sensorKey; }
    public int intervalIndex() { return intervalIndex; }
    public LocalDateTime startedAt() { return startedAt; }
    public Duration exactDuration() { return exactDuration; }
    public LocalDateTime endedAt() { return startedAt.plus(exactDuration); }
    public int roundedMinutes() { return roundedMinutes; }
    public int lostCans() { return lostCans; }
    public DetectionType detectionType() { return detectionType; }
    public StoppageState state() { return state; }
    public List<LossExplanation> explanations() { return explanations; }
    public long version() { return version; }
}
