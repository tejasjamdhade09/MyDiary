package com.diary.controller;

import com.diary.dto.RegisterForm;
import com.diary.model.AppUser;
import com.diary.repository.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult br) {
        String username = form.getUsername() == null ? "" : form.getUsername().trim().toLowerCase();
        if (!br.hasFieldErrors("username") && users.existsByUsername(username)) {
            br.rejectValue("username", "taken", "That username is already taken");
        }
        if (!br.hasFieldErrors("confirmPassword") && form.getPassword() != null
                && !form.getPassword().equals(form.getConfirmPassword())) {
            br.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (br.hasErrors()) return "register";

        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(form.getPassword()));
        users.save(u);
        return "redirect:/login?registered";
    }
}
