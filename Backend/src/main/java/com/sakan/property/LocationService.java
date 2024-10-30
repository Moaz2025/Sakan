package com.sakan.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationService {
    @Autowired
    private LocationRepository locationRepository;

    public Location addLocation(Location location) {
        return locationRepository.save(location);
    }

    public Location editLocation(Location location) {
        return locationRepository.save(location);
    }

    public Location getLocationById(int id) {
        return locationRepository.findById(id).orElse(null);
    }

    public Location getLocationByPropertyId(int propertyId) {
        return locationRepository.findByPropertyId(propertyId);
    }
}
