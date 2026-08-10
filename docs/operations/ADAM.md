# ADAM-6050 Production Procedure

Configure exactly one endpoint for each stable ID `sensor-1` through `sensor-6`. Host, port, slave ID,
counter channel, poll delay, timeout, retries and enablement are environment configuration. Keep ADAM
on a private network and restrict write/configuration access outside this application.

Before release, confirm the physical input-to-sensor mapping with a controlled pulse. Record the
counter before and after, the persisted baseline, accepted delta, production date and interval. Repeat
for all six sensors. Duplicate delivery must not increment Actual twice.

The separate `/actuator/health/adam` group reports connection state for every configured sensor. It is
an operational view and intentionally does not control application readiness: loss of one ADAM must
not crash or remove the rest of the application. Alert on disconnects, discontinuities, repeated poll
failures and absence of expected polls.
