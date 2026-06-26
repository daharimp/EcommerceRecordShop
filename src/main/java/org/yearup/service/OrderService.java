package org.yearup.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.*;
import org.yearup.repository.OrderLineItemRepository;
import org.yearup.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService
{
    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final ShoppingCartService shoppingCartService;
    private final ProfileService profileService;

    public OrderService(OrderRepository orderRepository,
                        OrderLineItemRepository orderLineItemRepository,
                        ShoppingCartService shoppingCartService,
                        ProfileService profileService)
    {
        this.orderRepository = orderRepository;
        this.orderLineItemRepository = orderLineItemRepository;
        this.shoppingCartService = shoppingCartService;
        this.profileService = profileService;
    }

    // @Transactional: checkout writes an order, several line items, and clears the
    // cart. Wrapping it in one transaction means if anything fails partway, the
    // whole thing rolls back -- you never get an order with no items, or a cleared
    // cart with no order. (It also satisfies the derived delete in clearCart.)
    @Transactional
    public Order checkout(int userId)
    {
        // 1. load the user's current cart
        ShoppingCart cart = shoppingCartService.getByUserId(userId);

        // can't check out an empty cart
        if (cart.getItems().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");

        // 2. pull the shipping address from the user's profile
        Profile profile = profileService.getByUserId(userId);

        // 3. create the order header and save it first, so it gets its generated order_id
        Order order = new Order();
        order.setUserId(userId);
        order.setDate(LocalDateTime.now());
        order.setAddress(profile.getAddress());
        order.setCity(profile.getCity());
        order.setState(profile.getState());
        order.setZip(profile.getZip());
        order.setShippingAmount(0.0);   // no shipping calc in scope; default 0
        order = orderRepository.save(order);

        // 4. turn each cart item into an order line item, snapshotting the price
        List<OrderLineItem> savedLines = new ArrayList<>();
        for (ShoppingCartItem cartItem : cart.getItems().values())
        {
            OrderLineItem line = new OrderLineItem();
            line.setOrderId(order.getOrderId());
            line.setProductId(cartItem.getProduct().getProductId());
            line.setSalesPrice(cartItem.getProduct().getPrice());   // freeze price at purchase time
            line.setQuantity(cartItem.getQuantity());
            line.setDiscount(cartItem.getDiscountPercent());
            savedLines.add(orderLineItemRepository.save(line));
        }

        // 5. empty the cart now that it's been converted into an order
        shoppingCartService.clearCart(userId);

        // 6. attach the saved line items to the response and return the finished order
        order.setLineItems(savedLines);
        return order;
    }
}