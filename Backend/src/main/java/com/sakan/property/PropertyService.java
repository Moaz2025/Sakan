package com.sakan.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {
    @Autowired
    private PropertyRepository propertyRepository;

    public Property addProperty(Property property) {
        return propertyRepository.save(property);
    }

    public Property editProperty(Property property) {
        return propertyRepository.save(property);
    }

    public void deleteProperty(Property property) {
        propertyRepository.delete(property);
    }

    public Property getPropertyById(int id) {
        return propertyRepository.findById(id).orElse(null);
    }

    public List<Property> getAllUserProperties(int userId) {
        return propertyRepository.findByUserId(userId);
    }

    public Page<Property> getAllProperties(Pageable pageable) {
        return propertyRepository.findAll(pageable);
    }

    public Page<Property> getAllPropertiesFilteredByPrice(int minPrice, int maxPrice, Pageable pageable) {
        return propertyRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    }
}
