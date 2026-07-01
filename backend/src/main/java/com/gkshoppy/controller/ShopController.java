package com.gkshoppy.controller;

import com.gkshoppy.model.Product;
import com.gkshoppy.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ShopController {

    private final ProductRepository productRepository;

    public ShopController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping({"/", "/shop"})
    public String shop(@RequestParam(value = "category", required = false) String category, Model model) {
        List<Product> products;
        if (category == null || category.isBlank()) {
            products = productRepository.findAll();
        } else {
            products = productRepository.findByCategoryIgnoreCase(category);
        }
        model.addAttribute("products", products);
        return "shop";
    }
}
