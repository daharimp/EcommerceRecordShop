package org.yearup.service;

import org.springframework.stereotype.Service;
import org.yearup.models.CartItem;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.repository.ShoppingCartRepository;

import java.util.List;

@Service
public class ShoppingCartService
{
    // a shopping cart is built from cart rows plus a product lookup for each row
    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductService productService;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository, ProductService productService)
    {
        this.shoppingCartRepository = shoppingCartRepository;
        this.productService = productService;
    }

    public ShoppingCart getByUserId(int userId)
    {
        // start with an empty cart (its items map and total build themselves)
        ShoppingCart cart = new ShoppingCart();

        // load this user's stored rows: each row is just userId/productId/quantity
        List<CartItem> rows = shoppingCartRepository.findByUserId(userId);

        for (CartItem row : rows)
        {
            // look up the FULL product for this row so the response has real details
            Product product = productService.getById(row.getProductId());

            // skip a row whose product no longer exists, rather than crashing
            if (product == null)
                continue;

            // build the rich response item: set product + quantity, line total computes itself
            ShoppingCartItem item = new ShoppingCartItem();
            item.setProduct(product);
            item.setQuantity(row.getQuantity());

            // add() keys the item by productId inside the cart's map
            cart.add(item);
        }

        return cart;
    }

    // add additional methods here (POST add, DELETE clear) in the next step
}