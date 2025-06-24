package com.sakan.property;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
    List<Property> findByUserId(int userId);
    Page<Property> findByPriceBetween(int minPrice, int maxPrice, Pageable pageable);
    Page<Property> findByPropertyType(PropertyType propertyType, Pageable pageable);

    @Query(value = "SELECT p.* FROM property p JOIN property_location l ON p.id = l.property_id WHERE LOWER(l.city) LIKE LOWER(CONCAT(?1, '%'))",
            countQuery = "SELECT COUNT(*) FROM property p JOIN property_location l ON p.id = l.property_id WHERE LOWER(l.city) LIKE LOWER(CONCAT(?1, '%'))",
            nativeQuery = true)
    Page<Property> findByCityStartingWith(String city, Pageable pageable);
}
