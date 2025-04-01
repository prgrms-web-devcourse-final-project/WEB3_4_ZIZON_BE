package com.ll.dopdang.domain.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.project.entity.Offer;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
}
