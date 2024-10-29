package com.sakan.property;

import org.springframework.beans.factory.annotation.Autowired;
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

    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }
}
