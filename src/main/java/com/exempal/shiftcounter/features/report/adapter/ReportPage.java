package com.exempal.shiftcounter.features.report.adapter;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.report.application.ReportQueryUseCase;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.*;
import com.exempal.shiftcounter.features.comment.application.CurrentCommentActor;
import com.exempal.shiftcounter.features.user.domain.UserRole;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class ReportPage implements PageModel {

    private final ReportQueryUseCase reports;
    private final CurrentCommentActor actors;

    @Autowired
    public ReportPage(ReportQueryUseCase reports, CurrentCommentActor actors) {
        this.reports = reports;
        this.actors = actors;
    }

    public ReportPage(ReportQueryUseCase reports) { this(reports, null); }

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
        model.addAttribute("lossTotals", report.lossTotals());
        model.addAttribute("timeTotals", report.timeTotals());
        model.addAttribute("timeGrouping", report.timeGrouping());
        model.addAttribute("productionTotals", report.productionTotals());
        model.addAttribute("totalProduction", report.totalProduction());
        model.addAttribute("unexplainedPlanTotals", report.unexplainedPlanTotals());
        model.addAttribute("sensorOptions", SensorCatalog.all());
        var role = actors == null ? null : actors.require().role();
        model.addAttribute("canExclude", role == UserRole.ADMIN || role == UserRole.OWNER);
        model.addAttribute("sourceOptions", List.of("sensor-1", "sensor-2", "sensor-3", "sensor-4"));
        model.addAttribute("typeOptions", LossCategory.values());
        model.addAttribute("sourceFilter", params.getOrDefault("source", ""));
        model.addAttribute("typeFilter", params.getOrDefault("type", ""));
        model.addAttribute("reasonFilter", params.getOrDefault("reason", ""));
        model.addAttribute("authorFilter", params.getOrDefault("author", ""));
    }
}
