package com.gkshoppy.controller;

import com.gkshoppy.model.Order;
import com.gkshoppy.repository.OrderRepository;
import com.gkshoppy.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class OrderController {

    private Long currentUserId(HttpSession session) {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                String username = auth.getName();
                var u = userRepository.findByUsername(username).orElse(null);
                if (u != null) return u.getId();
            }
        } catch (Exception ignored) {}
        Object id = session.getAttribute("userId");
        if (id instanceof Long) return (Long) id;
        return null;
    }

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderController(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/orders")
    public String myOrders(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/auth?next=/orders";
        }
        var user = userRepository.findById(userId).orElse(null);
        if (user == null) return "redirect:/auth?next=/orders";
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        model.addAttribute("orders", orders);
        return "orders";
    }

    // Admin list of all orders — secured by ROLE_ADMIN via Spring Security
    @GetMapping("/admin/orders")
    public String adminOrders(HttpSession session, Model model) {
        List<Order> orders = orderRepository.findAll();
        model.addAttribute("orders", orders);
        return "orders";
    }

    // Update status (admin)
    @PostMapping("/admin/order/{id}/status")
    public String updateOrderStatus(@org.springframework.web.bind.annotation.PathVariable Long id, @org.springframework.web.bind.annotation.RequestParam String status) {
        orderRepository.findById(id).ifPresent(o -> { o.setStatus(status); orderRepository.save(o); });
        return "redirect:/admin/orders";
    }
}
