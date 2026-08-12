package com.exempal.shiftcounter.features.report.adapter;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.report.application.ReportQueryUseCase;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.*;

@Component
public class ReportPage implements PageModel {

    private final ReportQueryUseCase reports;

    public ReportPage(ReportQueryUseCase reports) {
        this.reports = reports;
    }

    @Override
    public String getPageName() {
        return "report";
    }

    @Override
    public void populateModel(Model model) {
        // The parameter-aware overload is used by PageModelResolver.
    }

    @Override
    public void populateModel(Model model, Map<String, String> params) {
        var report = reports.query(params);
        model.addAttribute("problems", report.rows());
        model.addAttribute("startDate", report.from().toString());
        model.addAttribute("endDate", report.to().toString());
        model.addAttribute("sensorId", report.sensorId());
        model.addAttribute("totalMinutes", report.totalMinutes());
        model.addAttribute("totalCans", report.totalCans());
        model.addAttribute("signalTotals", report.signalTotals());
        model.addAttribute("sensorOptions", SensorCatalog.all());
    }
}
