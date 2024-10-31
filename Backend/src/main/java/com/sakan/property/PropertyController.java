package com.sakan.property;

import com.sakan.config.JwtService;
import com.sakan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/property")
@CrossOrigin(origins = "http://localhost:3000")
public class PropertyController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private JwtService jwtService;

    public boolean isValidSaleStatus(String saleStatus) {
        try {
            SaleStatus.valueOf(saleStatus.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isValidPropertyType(String propertyType) {
        try {
            PropertyType.valueOf(propertyType.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isValidAvailabilityStatus(String availabilityStatus) {
        try {
            AvailabilityStatus.valueOf(availabilityStatus.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addNewProperty(@RequestHeader("Authorization") String token, @RequestBody PropertyRequest propertyRequest) {
        token = token.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user == null) {
            return new ResponseEntity<>("Not a registered user", HttpStatus.FORBIDDEN);
        }
        else if (!isValidSaleStatus(propertyRequest.getSaleStatus())) {
            return new ResponseEntity<>("Invalid Sale status", HttpStatus.BAD_REQUEST);
        }
        else if (!isValidPropertyType(propertyRequest.getPropertyType())) {
            return new ResponseEntity<>("Invalid property type", HttpStatus.BAD_REQUEST);
        }
        else if (!isValidAvailabilityStatus(propertyRequest.getAvailabilityStatus())) {
            return new ResponseEntity<>("Invalid availability status", HttpStatus.BAD_REQUEST);
        }
        PropertyType propertyType = PropertyType.valueOf(propertyRequest.getPropertyType().toUpperCase());
        AvailabilityStatus availabilityStatus = AvailabilityStatus.valueOf(propertyRequest.getAvailabilityStatus().toUpperCase());
        SaleStatus saleStatus = SaleStatus.valueOf(propertyRequest.getSaleStatus().toUpperCase());
        var property = Property.builder()
                .user(user)
                .title(propertyRequest.getTitle())
                .description(propertyRequest.getDescription())
                .saleStatus(saleStatus)
                .price(propertyRequest.getPrice())
                .propertyType(propertyType)
                .size(propertyRequest.getSize())
                .numberOfRooms(propertyRequest.getNumberOfRooms())
                .numberOfBathrooms(propertyRequest.getNumberOfBathrooms())
                .floorNumber(propertyRequest.getFloorNumber())
                .availabilityStatus(availabilityStatus)
                .buildingYear(propertyRequest.getBuildingYear())
                .listingDate(propertyRequest.getListingDate())
                .build();
        propertyService.addProperty(property);
        var location = Location.builder()
                .property(property)
                .streetAddress(propertyRequest.getStreetAddress())
                .city(propertyRequest.getCity())
                .state(propertyRequest.getState())
                .country(propertyRequest.getCountry())
                .postalCode(propertyRequest.getPostalCode())
                .build();
        locationService.addLocation(location);
        List<String> imagesUrls = propertyRequest.getImagesUrls();
        for (String imageUrl : imagesUrls) {
            var image = Image.builder()
                    .property(property)
                    .imageUrl(imageUrl)
                    .build();
            imageService.addImage(image);
        }
        return new ResponseEntity<>("Property added successfully", HttpStatus.CREATED);
    }

}
