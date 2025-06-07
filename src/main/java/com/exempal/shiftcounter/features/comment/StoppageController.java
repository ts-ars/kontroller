package com.exempal.shiftcounter.features.comment;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class StoppageController {


    private final StoppageRepository repository;

    public StoppageController(StoppageRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody List<StoppageDTO> dtos) {
        List<StoppageEntry> entries = dtos.stream().map(dto -> {
            StoppageEntry entry = new StoppageEntry();
            entry.setTime(dto.time);
            entry.setMinutes(dto.minutes);
            entry.setType(dto.type);
            entry.setComment(dto.comment);
            entry.setDate(LocalDate.now());
            entry.setCans((int) (dto.minutes * 10)); // 10 шайб/мин
            return entry;
        }).toList();

        repository.saveAll(entries);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/range")
    public List<StoppageEntry> getRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return repository.findByDateBetween(from, to);
    }
}
