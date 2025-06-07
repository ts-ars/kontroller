package com.exempal.shiftcounter.core;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PageModelResolver {

    private final Map<String, PageModel> pageModelMap;

    @Autowired
    public PageModelResolver(List<PageModel> models) {
        this.pageModelMap = models.stream()
                .collect(Collectors.toMap(PageModel::getPageName, Function.identity()));
    }

    public String resolve(String pageName, Model model, HttpServletRequest request) {
        PageModel page = pageModelMap.get(pageName);
        if (page == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found: " + pageName);
        }

        model.addAttribute("contentTemplate", "features/" + pageName + "/" + pageName);
        model.addAttribute("pageTitle", capitalize(pageName));
        model.addAttribute("currentPage", pageName);

        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));

        page.populateModel(model, params);

        return "layout";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
