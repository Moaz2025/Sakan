package com.sakan.property;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
    List<Property> findByUserId(int userId);
    Page<Property> findByPriceBetween(int minPrice, int maxPrice, Pageable pageable);
}
