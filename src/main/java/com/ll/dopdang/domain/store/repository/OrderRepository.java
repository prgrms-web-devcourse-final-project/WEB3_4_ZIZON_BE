package com.ll.dopdang.domain.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.store.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}