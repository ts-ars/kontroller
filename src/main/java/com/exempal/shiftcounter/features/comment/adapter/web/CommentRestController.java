package com.exempal.shiftcounter.features.comment.adapter.web;

import com.exempal.shiftcounter.features.comment.adapter.dto.CommentRowDto;
import com.exempal.shiftcounter.features.comment.application.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentRestController {

    private final CommentService commentService;

    @PostMapping
    public void saveComments(
            @RequestBody List<CommentRowDto> rows,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        commentService.saveComments(rows, date);
    }
}