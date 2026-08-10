package com.exempal.shiftcounter.features.shift.infrastructure;

import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
public class JpaShiftAdapter implements ActualDataPort {

    private final ShiftJpaRepository repository;
    public JpaShiftAdapter(ShiftJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Shift save(Shift shift) {
        ShiftEntity entity = repository.findByDateAndSensorId(shift.getDate(), shift.getSensorId())
                .map(existing -> {
                    replace(existing, shift);
                    return existing;
                })
                .orElse(ShiftEntity.fromDomain(shift));

        // 🔍 Подробный лог перед сохранением
        log.info("📋 Сохраняем смену: date = {}, labels = {}, plan = {}, actual = {}",
                shift.getDate(),
                shift.getHourlyLabels(),
                shift.getHourlyPlanValues(),
                shift.getHourlyActualValues()
        );

        ShiftEntity saved = repository.save(entity);

        log.info("💾 Сохраняем actual: {}", saved.getHourlyActualValues());

        return saved.toDomain();
    }

    private void replace(ShiftEntity target, Shift source) {
        target.getHourlyActualValues().clear();
        target.getHourlyActualValues().addAll(source.getHourlyActualValues());

        target.getHourlyPlanValues().clear();
        target.getHourlyPlanValues().addAll(source.getHourlyPlanValues());

        target.getHourlyLabels().clear();
        target.getHourlyLabels().addAll(source.getHourlyLabels());

        target.setActual(source.getActual());
    }

    @Override
    @Transactional
    public void saveOrReplace(Shift shift) {
        repository.findByDateAndSensorId(shift.getDate(), shift.getSensorId())
                .ifPresent(existing -> repository.deleteById(existing.getId()));
        repository.flush();
        repository.save(ShiftEntity.fromDomain(shift));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shift> findByDateAndSensorId(LocalDate date, String sensorId) {
        return repository.findByDateAndSensorId(date, sensorId)
                .map(entity -> {
                    Hibernate.initialize(entity.getHourlyActualValues());
                    Hibernate.initialize(entity.getHourlyPlanValues());
                    Hibernate.initialize(entity.getHourlyLabels());

                    log.info("🧪 Читаем shift из БД: {}", date);
                    log.info("⏱ hourlyLabels = {}", entity.getHourlyLabels());
                    log.info("📊 hourlyActual = {}", entity.getHourlyActualValues());

                    return entity.toDomain();
                });
    }

    @Override
    @Transactional
    public void deleteByDateAndSensorId(LocalDate date, String sensorId) {
        repository.findByDateAndSensorId(date, sensorId).ifPresent(shift -> {
            log.info("🧹 Удаление смены за дату {}", date);
            repository.delete(shift);
        });
    }
    @Transactional(readOnly = true)
    public Optional<ShiftEntity> findEntityByDate(LocalDate date) {
        return repository.findByDateAndSensorId(date,
                com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shift> findById(long shiftId) {
        return repository.findById(shiftId).map(ShiftEntity::toDomain);
    }
}
