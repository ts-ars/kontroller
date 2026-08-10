package com.exempal.shiftcounter.features.shift.adapter.persistence;

import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

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
    public Shift save(Shift shift) {
        ShiftEntity entity = repository.findByDateAndSensorId(shift.getDate(), shift.getSensorId())
                .map(existing -> {
                    replace(existing, shift);
                    return existing;
                })
                .orElse(ShiftEntityMapper.fromDomain(shift));

        // 🔍 Подробный лог перед сохранением
        log.info("📋 Сохраняем смену: date = {}, labels = {}, plan = {}, actual = {}",
                shift.getDate(),
                shift.getHourlyLabels(),
                shift.getHourlyPlanValues(),
                shift.getHourlyActualValues()
        );

        ShiftEntity saved = repository.save(entity);

        log.info("💾 Сохраняем actual: {}", saved.getHourlyActualValues());

        return ShiftEntityMapper.toDomain(saved);
    }

    private void replace(ShiftEntity target, Shift source) {
        ShiftEntityMapper.replace(target, source);
    }

    @Override
    public void saveOrReplace(Shift shift) {
        repository.findByDateAndSensorId(shift.getDate(), shift.getSensorId())
                .ifPresent(existing -> repository.deleteById(existing.getId()));
        repository.flush();
        repository.save(ShiftEntityMapper.fromDomain(shift));
    }

    @Override
    public Optional<Shift> findByDateAndSensorId(LocalDate date, String sensorId) {
        return repository.findByDateAndSensorId(date, sensorId)
                .map(entity -> {
                    Hibernate.initialize(entity.getHourlyActualValues());
                    Hibernate.initialize(entity.getHourlyPlanValues());
                    Hibernate.initialize(entity.getHourlyLabels());

                    log.info("🧪 Читаем shift из БД: {}", date);
                    log.info("⏱ hourlyLabels = {}", entity.getHourlyLabels());
                    log.info("📊 hourlyActual = {}", entity.getHourlyActualValues());

                    return ShiftEntityMapper.toDomain(entity);
                });
    }

    @Override
    public Optional<Shift> findForUpdateByDateAndSensorId(LocalDate date, String sensorId) {
        return repository.findForUpdateByDateAndSensorId(date, sensorId)
                .map(ShiftEntityMapper::toDomain);
    }

    @Override
    public void deleteByDateAndSensorId(LocalDate date, String sensorId) {
        repository.findByDateAndSensorId(date, sensorId).ifPresent(shift -> {
            log.info("🧹 Удаление смены за дату {}", date);
            repository.delete(shift);
        });
    }
    public Optional<ShiftEntity> findEntityByDate(LocalDate date) {
        return repository.findByDateAndSensorId(date,
                com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1);
    }

    @Override
    public Optional<Shift> findById(long shiftId) {
        return repository.findById(shiftId).map(ShiftEntityMapper::toDomain);
    }
}
