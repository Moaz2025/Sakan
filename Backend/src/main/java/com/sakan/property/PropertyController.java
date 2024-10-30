package com.sakan.property;

import com.sakan.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/property")
@CrossOrigin(origins = "http://localhost:3000")
public class PropertyController {
    @Autowired
    private PropertyService propertyService;

    @Autowired
    private UserService userService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ImageService imageService;

    @PostMapping("/add")
    public ResponseEntity<String> addNewProperty(@RequestBody PropertyRequest propertyRequest) {
        if (userService.getUserById(propertyRequest.getUserId()) == null) {
            return new ResponseEntity<>("Not a registered user", HttpStatus.FORBIDDEN);
        }
        var property = Property.builder()
                .user(userService.getUserById(propertyRequest.getUserId()))
                .title(propertyRequest.getTitle())
                .description(propertyRequest.getDescription())
                .saleStatus(propertyRequest.getSaleStatus())
                .price(propertyRequest.getPrice())
                .propertyType(propertyRequest.getPropertyType())
                .size(propertyRequest.getSize())
                .numberOfRooms(propertyRequest.getNumberOfRooms())
                .numberOfBathrooms(propertyRequest.getNumberOfBathrooms())
                .floorNumber(propertyRequest.getFloorNumber())
                .availabilityStatus(propertyRequest.getAvailabilityStatus())
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
