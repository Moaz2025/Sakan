package com.sakan.property;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavouriteRepository extends JpaRepository<Favourite, Integer> {
    List<Favourite> findByUserId(int userId);
    Favourite findByUserIdAndPropertyId(int userId, int PropertyId);
}
