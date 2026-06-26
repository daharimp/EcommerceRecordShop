package org.yearup.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.CartItem;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.repository.ShoppingCartRepository;
import org.springframework.transaction.annotation.Transactional;

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

    public ShoppingCart addProduct(int userId, int productId)
    {
        // make sure the product actually exists -> clean 404 instead of a FK violation crash
        Product product = productService.getById(productId);
        if (product == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");

        // is this product already in the user's cart?
        CartItem existing = shoppingCartRepository.findByUserIdAndProductId(userId, productId);

        if (existing != null)
        {
            // already in the cart -> bump the quantity instead of adding a duplicate row
            existing.setQuantity(existing.getQuantity() + 1);
            shoppingCartRepository.save(existing);
        }
        else
        {
            // not in the cart yet -> create a new row with quantity 1
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(productId);
            item.setQuantity(1);
            shoppingCartRepository.save(item);
        }

        // return the freshly rebuilt cart so the caller sees the updated state
        return getByUserId(userId);
    }

    public ShoppingCart updateQuantity(int userId, int productId, int quantity)
    {
        // the product must already be in this user's cart to update it
        CartItem existing = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
        if (existing == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not in cart");

        // SET the quantity to the new value (PUT replaces; it does not increment)
        existing.setQuantity(quantity);
        shoppingCartRepository.save(existing);

        return getByUserId(userId);
    }

    @Transactional   // derived delete methods require an active transaction
    public ShoppingCart clearCart(int userId)
    {
        // remove every row belonging to this user
        shoppingCartRepository.deleteByUserId(userId);

        // return the now-empty cart so the front end can refresh
        return getByUserId(userId);
    }

    public ShoppingCart removeItem(int userId, int productId)
    {
        shoppingCartRepository.deleteByUserIdAndProductId(userId, productId);
        return getByUserId(userId);
    }
}