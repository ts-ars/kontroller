package com.exempal.shiftcounter.features.comment.adapter.web;

import com.exempal.shiftcounter.features.comment.adapter.dto.CommentRowDto;
import com.exempal.shiftcounter.features.comment.application.CommentService;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftCommentsController {

    private final CommentService commentService;
    private final ActualDataPort actualDataPort;

    @PostMapping("/{shiftId}/comments")
    public ResponseEntity<Void> saveForShift(
            @PathVariable long shiftId,
            @RequestBody List<CommentRowDto> rows
    ) {
        Shift shift = actualDataPort.findById(shiftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "shift " + shiftId + " not found"));

        LocalDate date = shift.getDate();
        commentService.saveComments(rows, date);   // существующий метод по дате
        return ResponseEntity.ok().build();
    }
}