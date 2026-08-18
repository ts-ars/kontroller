package com.exempal.shiftcounter.features.user.adapter.web;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.user.application.UserAdministration;
import com.exempal.shiftcounter.features.user.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;

@Controller
public class UsersPage implements PageModel {
    private final UserAdministration users;
    public UsersPage(UserAdministration users){this.users=users;}
    @Override public String getPageName(){return "users";}
    @Override public void populateModel(Model model){model.addAttribute("users",users.list());model.addAttribute("roles",UserRole.values());}

    @PostMapping("/users") public String create(@RequestParam String displayName,@RequestParam String pin,@RequestParam UserRole role,RedirectAttributes flash){return action(flash,()->users.create(displayName,pin,role),"User created");}
    @PostMapping("/users/{id}/profile") public String profile(@PathVariable UUID id,@RequestParam String displayName,@RequestParam UserRole role,RedirectAttributes flash){return action(flash,()->users.updateProfile(id,displayName,role),"User updated");}
    @PostMapping("/users/{id}/pin") public String pin(@PathVariable UUID id,@RequestParam String pin,RedirectAttributes flash){return action(flash,()->users.changePin(id,pin),"PIN changed");}
    @PostMapping("/users/{id}/status") public String status(@PathVariable UUID id,@RequestParam UserStatus status,RedirectAttributes flash){return action(flash,()->users.changeStatus(id,status),status==UserStatus.ACTIVE?"User activated":"User blocked");}
    private static String action(RedirectAttributes flash,Runnable operation,String success){try{operation.run();flash.addFlashAttribute("message",success);}catch(RuntimeException ex){flash.addFlashAttribute("error",ex.getMessage());}return "redirect:/page/users";}
}
