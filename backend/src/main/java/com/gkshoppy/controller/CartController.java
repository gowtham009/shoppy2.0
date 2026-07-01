package com.gkshoppy.controller;

import com.gkshoppy.model.Product;
import com.gkshoppy.repository.ProductRepository;
import com.gkshoppy.repository.CartRepository;
import com.gkshoppy.repository.UserRepository;
import com.gkshoppy.repository.OrderRepository;
import com.gkshoppy.service.CheckoutService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CartController {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CheckoutService checkoutService;

    public CartController(ProductRepository productRepository, CartRepository cartRepository, UserRepository userRepository, OrderRepository orderRepository, CheckoutService checkoutService) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/cart")
    public String cartPage(HttpSession session, Model model) {
        // prefer persisted cart for logged-in users
        Long userId = currentUserId(session);
        Map<Product, Integer> items = new HashMap<>();
        double total = 0.0;
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                cartRepository.findByUser(user).ifPresent(cart -> {
                    for (var ci : cart.getItems()) items.put(ci.getProduct(), ci.getQuantity());
                });
            });
        } else {
            Map<Long, Integer> cart = cartFromSession(session);
            for (Map.Entry<Long, Integer> e : cart.entrySet()) {
                productRepository.findById(e.getKey()).ifPresent(p -> items.put(p, e.getValue()));
            }
        }
        for (Map.Entry<Product, Integer> e : items.entrySet()) total += e.getKey().getPrice() * e.getValue();
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId, @RequestParam(defaultValue = "1") Integer qty, HttpSession session) {
        Long userId = currentUserId(session);
        if (userId != null) {
            var userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                var cart = cartRepository.findByUser(user).orElseGet(() -> {
                    var c = new com.gkshoppy.model.Cart(); c.setUser(user); return c;
                });
                // find existing item
                var existing = cart.getItems().stream().filter(i -> i.getProduct().getId().equals(productId)).findFirst();
                Product prod = productRepository.findById(productId).orElseThrow();
                if (existing.isPresent()) {
                    existing.get().setQuantity(existing.get().getQuantity() + qty);
                } else {
                    com.gkshoppy.model.CartItem ci = new com.gkshoppy.model.CartItem();
                    ci.setProduct(prod); ci.setQuantity(qty); cart.addItem(ci);
                }
                cartRepository.save(cart);
                return "redirect:/cart";
            }
        }
        // anonymous: keep in session
        Map<Long, Integer> cart = cartFromSession(session);
        cart.put(productId, cart.getOrDefault(productId, 0) + qty);
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId, HttpSession session) {
        Long userId = currentUserId(session);
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                cartRepository.findByUser(user).ifPresent(cart -> {
                    cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
                    cartRepository.save(cart);
                });
            });
            return "redirect:/cart";
        }
        Map<Long, Integer> cart = cartFromSession(session);
        cart.remove(productId);
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (userId == null) {
            // require login
            return "redirect:/auth?next=/cart";
        }
        try {
            var order = checkoutService.checkoutForUser(userId);
            model.addAttribute("order", order);
            session.removeAttribute("cart");
            return "order-confirmation";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "cart";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> cartFromSession(HttpSession session) {
        Object c = session.getAttribute("cart");
        if (c instanceof Map) {
            return (Map<Long, Integer>) c;
        }
        Map<Long, Integer> cart = new java.util.HashMap<>();
        session.setAttribute("cart", cart);
        return cart;
    }

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
}
