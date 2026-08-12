package com.exempal.shiftcounter.features.shift.adapter.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShiftPageController {
    @GetMapping("/shift")
    public String showShift() {
        return "redirect:/page/shift";
    }
}
