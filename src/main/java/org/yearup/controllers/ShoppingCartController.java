package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;
import org.yearup.service.ShoppingCartService;
import org.yearup.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("cart")
@CrossOrigin
@PreAuthorize("isAuthenticated()")   // only logged-in users can touch the cart
public class ShoppingCartController
{
    // a shopping cart controller depends on the service layer
    private ShoppingCartService shoppingCartService;
    private UserService userService;

    @Autowired
    public ShoppingCartController(ShoppingCartService shoppingCartService, UserService userService)
    {
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
    }

    // GET https://localhost:8080/cart  -> the current user's cart
    @GetMapping
    public ShoppingCart getCart(Principal principal)
    {
        return shoppingCartService.getByUserId(getUserId(principal));
    }

    // POST https://localhost:8080/cart/products/15  -> add product 15 (or increment if already present)
    // returns the updated cart with status 201 Created
    @PostMapping("products/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingCart addToCart(@PathVariable int productId, Principal principal)
    {
        return shoppingCartService.addProduct(getUserId(principal), productId);
    }

    // DELETE https://localhost:8080/cart  -> clear the whole cart, return the now-empty cart (200 OK)
    @DeleteMapping
    public ShoppingCart clearCart(Principal principal)
    {
        return shoppingCartService.clearCart(getUserId(principal));
    }

    // shared helper: turn the logged-in Principal into this user's database id
    private int getUserId(Principal principal)
    {
        String userName = principal.getName();
        User user = userService.getByUserName(userName);
        return user.getId();
    }
    // PUT https://localhost:8080/cart/products/15  -> set product 15's quantity to the body's value (200 OK)
    @PutMapping("products/{productId}")
    public ShoppingCart updateCart(@PathVariable int productId,
                                   @RequestBody ShoppingCartItem item,
                                   Principal principal)
    {
        return shoppingCartService.updateQuantity(getUserId(principal), productId, item.getQuantity());
    }

    @DeleteMapping("/products/{productId}")
    public ShoppingCart removeProduct(@PathVariable int productId, Principal principal)
    {
        String userName = principal.getName();
        User user = userService.getByUserName(userName);
        int userId = user.getId();

        return shoppingCartService.removeItem(userId, productId);
    }
}