package com.sakan.property;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    Location findByPropertyId(int propertyId);
}
