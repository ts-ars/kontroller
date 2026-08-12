package com.exempal.shiftcounter.features.user.adapter.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignInController {
    private final SignInUserDirectory users;
    public SignInController(SignInUserDirectory users){this.users=users;}
    @GetMapping("/signin") public String signIn(Model model, Authentication authentication){
        if(authentication!=null && authentication.isAuthenticated()) return "redirect:/page/shift";
        model.addAttribute("users",users.findAllByOrderByDisplayNameAsc()); return "signin";
    }
}
