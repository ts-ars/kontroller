package com.exempal.shiftcounter.core;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {

    private final PageModelResolver pageModelResolver;

    @Autowired
    public PageController(PageModelResolver pageModelResolver) {
        this.pageModelResolver = pageModelResolver;
    }

    @GetMapping("/page/{pageName}")
    public String render(@PathVariable String pageName, Model model, HttpServletRequest request) {
        return pageModelResolver.resolve(pageName, model, request);
    }
}
