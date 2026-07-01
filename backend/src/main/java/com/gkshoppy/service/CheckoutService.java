package com.gkshoppy.service;

import com.gkshoppy.model.Cart;
import com.gkshoppy.model.CartItem;
import com.gkshoppy.model.Order;
import com.gkshoppy.model.OrderItem;
import com.gkshoppy.model.Product;
import com.gkshoppy.model.User;
import com.gkshoppy.repository.CartRepository;
import com.gkshoppy.repository.OrderRepository;
import com.gkshoppy.repository.ProductRepository;
import com.gkshoppy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    public CheckoutService(CartRepository cartRepository, OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository, PaymentService paymentService) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Order checkoutForUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new IllegalStateException("Cart is empty"));
        if (cart.getItems().isEmpty()) throw new IllegalStateException("Cart is empty");

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setStatus("CREATED");
        double total = 0.0;

        for (CartItem ci : cart.getItems()) {
            Product product = productRepository.findById(ci.getProduct().getId()).orElseThrow();
            int qty = ci.getQuantity();
            if (product.getStockQuantity() != null && product.getStockQuantity() < qty) {
                throw new IllegalStateException("Insufficient stock for: " + product.getName());
            }
            // Deduct stock
            if (product.getStockQuantity() != null) {
                product.setStockQuantity(product.getStockQuantity() - qty);
                productRepository.save(product);
            }
            OrderItem oi = new OrderItem();
            oi.setProduct(product);
            oi.setQuantity(qty);
            oi.setPriceAtPurchase(product.getPrice());
            order.addItem(oi);
            total += product.getPrice() * qty;
        }
        order.setTotal(total);

        // Process payment (stub)
        boolean paid = paymentService.processPayment(order);
        if (!paid) {
            throw new IllegalStateException("Payment failed");
        }

        orderRepository.save(order);

        // clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return order;
    }
}
