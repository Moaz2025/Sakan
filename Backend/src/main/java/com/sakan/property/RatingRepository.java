package com.sakan.property;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Integer>{
    List<Rating> findByPropertyId(int propertyId);
    List<Rating> findByPropertyIdAndUserId(int propertyId, int userId);
}
