package com.gkshoppy.security;

import com.gkshoppy.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // set session userId for compatibility with existing controllers that rely on session
        HttpSession session = request.getSession(false);
        if (session != null) {
            userRepository.findByUsername(authentication.getName()).ifPresent(u -> session.setAttribute("userId", u.getId()));
        }
        // redirect to profile or intended URL
        response.sendRedirect(request.getContextPath() + "/profile");
    }
}
