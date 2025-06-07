package com.exempal.shiftcounter.core;

import org.springframework.ui.Model;
import java.util.Map;

public interface PageModel {

    String getPageName();

    /**
     * Обязательный базовый метод — для совместимости с простыми страницами
     */
    void populateModel(Model model);

    /**
     * Новый метод с параметрами. По умолчанию вызывает старый.
     * Используется PageModelResolver.
     */
    default void populateModel(Model model, Map<String, String> params) {
        populateModel(model);
    }
}
