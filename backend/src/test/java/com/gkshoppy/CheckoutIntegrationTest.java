package com.gkshoppy;

import com.gkshoppy.model.Product;
import com.gkshoppy.model.User;
import com.gkshoppy.repository.ProductRepository;
import com.gkshoppy.repository.UserRepository;
import com.gkshoppy.service.CartService;
import com.gkshoppy.service.CheckoutService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class CheckoutIntegrationTest {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CartService cartService;
    @Autowired
    CheckoutService checkoutService;

    @Test
    public void checkoutCreatesOrderAndReducesStock() {
        // create product
        Product p = new Product(); p.setName("Test Product"); p.setPrice(100.0); p.setStockQuantity(10); productRepository.save(p);
        User u = new User(); u.setEmail("t@example.com"); u.setUsername("tuser"); u.setPasswordHash("x"); userRepository.save(u);
        Map<Long,Integer> sessionCart = new HashMap<>(); sessionCart.put(p.getId(), 2);
        cartService.migrateSessionCartToUser(u.getId(), sessionCart);
        var order = checkoutService.checkoutForUser(u.getId());
        Assertions.assertNotNull(order.getId());
        // product stock reduced
        Product p2 = productRepository.findById(p.getId()).orElseThrow();
        Assertions.assertEquals(8, p2.getStockQuantity());
    }
}
