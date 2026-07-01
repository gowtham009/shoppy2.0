package com.gkshoppy.controller;

import com.gkshoppy.model.User;
import com.gkshoppy.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gkshoppy.service.CartService;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final CartService cartService;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, CartService cartService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/auth")
    public String authPage(@RequestParam(value = "next", required = false) String next, Model model) {
        model.addAttribute("next", next);
        return "auth";
    }

    @PostMapping("/auth/signup")
    public String signup(@RequestParam String email, @RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already registered");
            return "auth";
        }
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already taken");
            return "auth";
        }
        User u = new User();
        u.setEmail(email);
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole("USER");
        userRepository.save(u);

        // migrate session cart into persisted cart
        try {
            Object c = session.getAttribute("cart");
            if (c instanceof java.util.Map) {
                @SuppressWarnings("unchecked") java.util.Map<Long,Integer> map = (java.util.Map<Long,Integer>) c;
                this.cartService.migrateSessionCartToUser(u.getId(), map);
                session.removeAttribute("cart");
            }
        } catch (Exception ex) { /* ignore migration errors */ }

        // Programmatically authenticate the new user so they don't have to login immediately
        try {
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authReq =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(username, password);
            org.springframework.security.core.Authentication auth = authenticationManager.authenticate(authReq);
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ex) {
            // if authentication manager not configured or fails, fallback to session-based userId
            session.setAttribute("userId", u.getId());
        }

        return "redirect:/profile";
    }
}
