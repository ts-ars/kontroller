package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.adapter.dto.CommentRowDto;

import java.time.LocalDate;
import java.util.List;

public interface CommentService {
    void saveComments(List<CommentRowDto> rows, LocalDate date);
}