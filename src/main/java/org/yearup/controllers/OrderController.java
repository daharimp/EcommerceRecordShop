package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.models.Order;
import org.yearup.models.User;
import org.yearup.service.OrderService;
import org.yearup.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("orders")
@CrossOrigin
@PreAuthorize("isAuthenticated()")   // only logged-in users can check out
public class OrderController
{
    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public OrderController(OrderService orderService, UserService userService)
    {
        this.orderService = orderService;
        this.userService = userService;
    }

    // POST https://localhost:8080/orders  -> convert the current user's cart into an order
    // returns the created order (with its line items) and status 201 Created
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order checkout(Principal principal)
    {
        String userName = principal.getName();
        User user = userService.getByUserName(userName);
        return orderService.checkout(user.getId());
    }
}

