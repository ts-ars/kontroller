# Stage 9 вЂ” Architectural Cleanup

Status: **APPROVED / NOT IMPLEMENTED**

This stage adds no business functionality. It brings the implementation into compliance with Stages 0вЂ“8.

## Rules

1. Dependencies point `adapter в†’ application в†’ domain`.
2. Feature internals are private; cross-feature calls use public application ports.
3. One business scenario has one use case; one algorithm has one implementation; each aggregate has one source of truth.
4. Domain objects, JPA entities and REST/WebSocket DTOs are separate and mapped in centralized locations.
5. Transactions begin at application use-case boundaries, not controllers, repositories or listeners.
6. No cyclic dependencies, god classes, duplicated logic, dead paths, obsolete endpoints/listeners/adapters or magic business values remain.
7. Time and event publication use their single approved mechanisms.
8. Domain invariants prevent invalid state (`actual/plan >= 0`, one settings group per sensor, ordered non-overlapping intervals).
9. Final feature packages follow `domain/`, `application/`, `adapter/`; `shared/` contains only genuinely shared elements.
10. ArchUnit enforces layer direction, controller/repository separation, feature boundaries and separation of domain from JPA.

## Verification

Every use case and adapter has appropriately scoped tests; the full unit/integration suite passes; a final audit checks compliance with Stages 0вЂ“8.

Security, deployment, backup, secrets, CI/CD and production infrastructure are not Stage 9 work; they belong to Stage 10.

## Definition of Done

All ten rules above are satisfied, architecture tests enforce the automatable rules, all tests pass and the Stage 0вЂ“8 audit has no unresolved violation.

