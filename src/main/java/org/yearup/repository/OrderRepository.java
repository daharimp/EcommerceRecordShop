package org.yearup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.yearup.models.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>
{
    // handy for a future "order history" endpoint
    List<Order> findByUserId(int userId);
}