package com.gkshoppy.service;

import com.gkshoppy.model.Cart;
import com.gkshoppy.model.CartItem;
import com.gkshoppy.model.Product;
import com.gkshoppy.model.User;
import com.gkshoppy.repository.CartRepository;
import com.gkshoppy.repository.ProductRepository;
import com.gkshoppy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void migrateSessionCartToUser(Long userId, Map<Long, Integer> sessionCart) {
        if (sessionCart == null || sessionCart.isEmpty()) return;
        User user = userRepository.findById(userId).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart(); c.setUser(user); return c;
        });
        for (Map.Entry<Long, Integer> e : sessionCart.entrySet()) {
            Long productId = e.getKey();
            Integer qty = e.getValue();
            Product p = productRepository.findById(productId).orElse(null);
            if (p == null) continue;
            var existing = cart.getItems().stream().filter(i -> i.getProduct().getId().equals(productId)).findFirst();
            if (existing.isPresent()) {
                existing.get().setQuantity(existing.get().getQuantity() + qty);
            } else {
                CartItem ci = new CartItem(); ci.setProduct(p); ci.setQuantity(qty); cart.addItem(ci);
            }
        }
        cartRepository.save(cart);
    }
}
