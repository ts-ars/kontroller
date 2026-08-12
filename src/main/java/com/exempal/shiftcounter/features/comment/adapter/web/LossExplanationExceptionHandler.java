package com.exempal.shiftcounter.features.comment.adapter.web;

import com.exempal.shiftcounter.features.comment.application.LossAllocationException;
import com.exempal.shiftcounter.features.comment.application.LossExplanationNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = LossExplanationController.class)
public class LossExplanationExceptionHandler {
    @ExceptionHandler(LossExplanationNotFoundException.class)
    ProblemDetail notFound(LossExplanationNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({LossAllocationException.class, IllegalArgumentException.class})
    ProblemDetail invalid(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(com.exempal.shiftcounter.features.comment.application.CommentAccessDeniedException.class)
    ProblemDetail forbidden(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(OptimisticLockException.class)
    ProblemDetail conflict(OptimisticLockException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "stoppage was changed by another transaction; reload and retry");
    }
}
