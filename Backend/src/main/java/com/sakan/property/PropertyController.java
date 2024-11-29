package com.sakan.property;

import com.sakan.config.JwtService;
import com.sakan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
        Date currentDate = new Date();
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
                .listingDate(currentDate)
                .views(0)
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

    @PutMapping("/edit/{propertyId}")
    public ResponseEntity<String> editProperty(@PathVariable int propertyId, @RequestHeader("Authorization") String token, @RequestBody PropertyRequest propertyRequest) {
        token = token.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user == null) {
            return new ResponseEntity<>("Not a registered user", HttpStatus.FORBIDDEN);
        }
        else if (propertyService.getPropertyById(propertyId) == null) {
            return new ResponseEntity<>("No property with this id", HttpStatus.NOT_FOUND);
        }
        else if (!Objects.equals(propertyService.getPropertyById(propertyId).getUser().getEmail(), email)) {
            return new ResponseEntity<>("This property doesn't belong to this user", HttpStatus.FORBIDDEN);
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
        Property dataProperty = propertyService.getPropertyById(propertyId);
        var property = Property.builder()
                .user(user)
                .id(propertyId)
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
                .listingDate(dataProperty.getListingDate())
                .views(dataProperty.getViews())
                .build();
        propertyService.editProperty(property);
        int locationId = locationService.getLocationByPropertyId(propertyId).getId();
        var location = Location.builder()
                .id(locationId)
                .property(property)
                .streetAddress(propertyRequest.getStreetAddress())
                .city(propertyRequest.getCity())
                .state(propertyRequest.getState())
                .country(propertyRequest.getCountry())
                .postalCode(propertyRequest.getPostalCode())
                .build();
        locationService.editLocation(location);
        List<Image> images = imageService.getAllPropertyImages(propertyId);
        for (Image image : images) {
            imageService.deleteImage(image);
        }
        List<String> imagesUrls = propertyRequest.getImagesUrls();
        for (String imageUrl : imagesUrls) {
            var image = Image.builder()
                    .property(property)
                    .imageUrl(imageUrl)
                    .build();
            imageService.addImage(image);
        }
        return new ResponseEntity<>("Property edited successfully", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{propertyId}")
    public ResponseEntity<String> deleteProperty(@PathVariable int propertyId, @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user == null) {
            return new ResponseEntity<>("Not a registered user", HttpStatus.FORBIDDEN);
        }
        else if (propertyService.getPropertyById(propertyId) == null) {
            return new ResponseEntity<>("No property with this id", HttpStatus.NOT_FOUND);
        }
        else if (!Objects.equals(propertyService.getPropertyById(propertyId).getUser().getEmail(), email)) {
            return new ResponseEntity<>("This property doesn't belong to this user", HttpStatus.FORBIDDEN);
        }
        Location location = locationService.getLocationByPropertyId(propertyId);
        locationService.deleteLocation(location);
        List<Image> images = imageService.getAllPropertyImages(propertyId);
        for (Image image : images) {
            imageService.deleteImage(image);
        }
        Property property = propertyService.getPropertyById(propertyId);
        propertyService.deleteProperty(property);
        return new ResponseEntity<>("Property deleted successfully", HttpStatus.OK);
    }

    @GetMapping("/get/{propertyId}")
    public ResponseEntity<PropertyResponse> getProperty(@PathVariable int propertyId) {
        PropertyResponse propertyResponse = new PropertyResponse();
        if (propertyService.getPropertyById(propertyId) == null) {
            propertyResponse.setMessage("No property with this id");
            return new ResponseEntity<>(propertyResponse, HttpStatus.NOT_FOUND);
        }
        Property property = propertyService.getPropertyById(propertyId);
        Location location = locationService.getLocationByPropertyId(propertyId);
        List<Image> images = imageService.getAllPropertyImages(propertyId);
        List<String> imagesUrls = images.stream()
                .map(Image::getImageUrl)
                .toList();
        property.setViews(property.getViews() + 1);
        propertyService.editProperty(property);
        propertyResponse = PropertyResponse.builder()
                .message("Property got successfully")
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .saleStatus(property.getSaleStatus().toString())
                .price(property.getPrice())
                .propertyType(property.getPropertyType().toString())
                .size(property.getSize())
                .numberOfRooms(property.getNumberOfRooms())
                .numberOfBathrooms(property.getNumberOfBathrooms())
                .floorNumber(property.getFloorNumber())
                .availabilityStatus(property.getAvailabilityStatus().toString())
                .buildingYear(property.getBuildingYear())
                .listingDate(property.getListingDate())
                .views(property.getViews())
                .streetAddress(location.getStreetAddress())
                .city(location.getCity())
                .state(location.getState())
                .country(location.getCountry())
                .postalCode(location.getPostalCode())
                .imagesUrls(imagesUrls)
                .build();
        return new ResponseEntity<>(propertyResponse, HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<PropertyResponse>> getAllProperties(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Property> properties = propertyService.getProperties(pageNo, pageSize);
        List<PropertyResponse> propertyResponsesList = new ArrayList<>();
        for (Property property : properties) {
            Location location = locationService.getLocationByPropertyId(property.getId());
            List<Image> images = imageService.getAllPropertyImages(property.getId());
            List<String> imagesUrls = images.stream()
                    .map(Image::getImageUrl)
                    .toList();
            PropertyResponse propertyResponse;
            propertyResponse = PropertyResponse.builder()
                    .message("Property got successfully")
                    .id(property.getId())
                    .title(property.getTitle())
                    .description(property.getDescription())
                    .saleStatus(property.getSaleStatus().toString())
                    .price(property.getPrice())
                    .propertyType(property.getPropertyType().toString())
                    .size(property.getSize())
                    .numberOfRooms(property.getNumberOfRooms())
                    .numberOfBathrooms(property.getNumberOfBathrooms())
                    .floorNumber(property.getFloorNumber())
                    .availabilityStatus(property.getAvailabilityStatus().toString())
                    .buildingYear(property.getBuildingYear())
                    .listingDate(property.getListingDate())
                    .views(property.getViews())
                    .streetAddress(location.getStreetAddress())
                    .city(location.getCity())
                    .state(location.getState())
                    .country(location.getCountry())
                    .postalCode(location.getPostalCode())
                    .imagesUrls(imagesUrls)
                    .build();
            propertyResponsesList.add(propertyResponse);
        }
        Page<PropertyResponse> propertyResponsePage = new PageImpl<>(propertyResponsesList, properties.getPageable(), properties.getTotalElements());
        return ResponseEntity.ok(propertyResponsePage);
    }

    @GetMapping("/getAllUserProperties")
    public ResponseEntity<List<PropertyResponse>> getAllUserProperties(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        List<Property> properties = propertyService.getAllUserProperties(user.getId());
        List<PropertyResponse> propertyResponsesList = new ArrayList<>();
        for (Property property : properties) {
            Location location = locationService.getLocationByPropertyId(property.getId());
            List<Image> images = imageService.getAllPropertyImages(property.getId());
            List<String> imagesUrls = images.stream()
                    .map(Image::getImageUrl)
                    .toList();
            PropertyResponse propertyResponse;
            propertyResponse = PropertyResponse.builder()
                    .message("Property got successfully")
                    .id(property.getId())
                    .title(property.getTitle())
                    .description(property.getDescription())
                    .saleStatus(property.getSaleStatus().toString())
                    .price(property.getPrice())
                    .propertyType(property.getPropertyType().toString())
                    .size(property.getSize())
                    .numberOfRooms(property.getNumberOfRooms())
                    .numberOfBathrooms(property.getNumberOfBathrooms())
                    .floorNumber(property.getFloorNumber())
                    .availabilityStatus(property.getAvailabilityStatus().toString())
                    .buildingYear(property.getBuildingYear())
                    .listingDate(property.getListingDate())
                    .views(property.getViews())
                    .streetAddress(location.getStreetAddress())
                    .city(location.getCity())
                    .state(location.getState())
                    .country(location.getCountry())
                    .postalCode(location.getPostalCode())
                    .imagesUrls(imagesUrls)
                    .build();
            propertyResponsesList.add(propertyResponse);
        }
        return ResponseEntity.ok(propertyResponsesList);
    }
}
