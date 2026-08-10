# Stage 9 — Architectural Cleanup

Status: **APPROVED / IMPLEMENTED**

This stage adds no business functionality. It brings the implementation into compliance with Stages 0–8.

## Rules

1. Dependencies point `adapter → application → domain`.
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

Every use case and adapter has appropriately scoped tests; the full unit/integration suite passes; a final audit checks compliance with Stages 0–8.

Security, deployment, backup, secrets, CI/CD and production infrastructure are not Stage 9 work; they belong to Stage 10.

## Implementation evidence

- Feature packages now consistently use `domain`, `application`, and `adapter`; obsolete
  `infrastructure` and `api` production packages are removed.
- Cross-feature behavior is exposed through application ports. Signal registration invokes the
  shift product-registration boundary directly inside the Stage 7 transaction.
- Domain models are separated from persistence entities through centralized mappers; web adapters
  depend on application queries rather than repositories.
- Transaction ownership is located at application use-case boundaries. Persistence adapters contain
  no transaction demarcation.
- The duplicate synchronous domain-event path, obsolete listeners/controllers, factories, metrics
  calculators, and settings helpers are removed.
- `Stage9ArchitectureTest` enforces dependency direction, web/persistence separation, feature-cycle
  freedom, and the removal of obsolete package shapes. `Stage9DomainInvariantTest` protects the new
  domain guards.
- No schema migration is needed because the cleanup preserves the existing Stage 8 database schema.

Stage 10 production release work remains explicitly out of scope.

## Definition of Done

All ten rules above are satisfied, architecture tests enforce the automatable rules, all tests pass and the Stage 0–8 audit has no unresolved violation.

