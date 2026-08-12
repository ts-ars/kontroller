package com.exempal.shiftcounter.features.comment.application;

public class CommentAccessDeniedException extends RuntimeException {
    public CommentAccessDeniedException() { super("You may modify only your own comments"); }
}
