package com.sakan.property;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sakan.config.JwtService;
import com.sakan.user.User;
import com.sakan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

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

    @Autowired
    CloudinaryService cloudinaryService;

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

    public String getJsonValue(JsonNode json, String field) {
        return json.has(field) ? json.get(field).asText() : null;
    }

    private int getJsonInteger(JsonNode json, String field) {
        return json.has(field) ? json.get(field).asInt() : 0;
    }

    private float getJsonFloat(JsonNode json, String field) {
        return json.has(field) ? json.get(field).floatValue() : 0;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addNewProperty(@RequestHeader("Authorization") String token, @RequestParam String metadataJson, @RequestParam List<MultipartFile> multipartFiles) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode metadata = mapper.readTree(metadataJson);
        var propertyRequest = PropertyRequest.builder()
                .title(getJsonValue(metadata, "title"))
                .description(getJsonValue(metadata, "description"))
                .saleStatus(getJsonValue(metadata, "saleStatus"))
                .price(getJsonInteger(metadata, "price"))
                .propertyType(getJsonValue(metadata, "propertyType"))
                .size(getJsonFloat(metadata, "size"))
                .numberOfRooms(getJsonInteger(metadata, "numberOfRooms"))
                .numberOfBathrooms(getJsonInteger(metadata, "numberOfBathrooms"))
                .floorNumber(getJsonInteger(metadata, "floorNumber"))
                .availabilityStatus(getJsonValue(metadata, "availabilityStatus"))
                .buildingYear(getJsonInteger(metadata, "buildingYear"))
                .streetAddress(getJsonValue(metadata, "streetAddress"))
                .city(getJsonValue(metadata, "city"))
                .state(getJsonValue(metadata, "state"))
                .country(getJsonValue(metadata, "country"))
                .postalCode(getJsonValue(metadata, "postalCode"))
                .build();
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
        else if (multipartFiles.isEmpty() || multipartFiles.size() > 10) {
            return new ResponseEntity<>("Number of images should be between 1 and 10", HttpStatus.BAD_REQUEST);
        }
        for (MultipartFile multipartFile : multipartFiles) {
            BufferedImage bufferedImage = ImageIO.read(multipartFile.getInputStream());
            if (bufferedImage == null) {
                return new ResponseEntity<>("Image is not valid", HttpStatus.BAD_REQUEST);
            }
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
        List<String> imagesUrls = new ArrayList<>();
        List<String> cloudIds = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            Map result = cloudinaryService.upload(multipartFile);
            String imageUrl = (String) result.get("url");
            String cloudId = (String) result.get("public_id");
            imagesUrls.add(imageUrl);
            cloudIds.add(cloudId);
        }
        for (int i = 0; i < imagesUrls.size(); i++) {
            var image = Image.builder()
                    .property(property)
                    .imageUrl(imagesUrls.get(i))
                    .cloudId(cloudIds.get(i))
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
            String cloudinaryImageId = image.getCloudId();
            try {
                cloudinaryService.delete(cloudinaryImageId);
            } catch (IOException e) {
                return new ResponseEntity<>("Failed to delete image from Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR);
            }
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
    public ResponseEntity<Page<PropertyResponse>> getAllProperties(Pageable pageable) {
        Page<Property> properties = propertyService.getAllProperties(pageable);
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

    @GetMapping("/getPropertyOwner/{propertyId}")
    public ResponseEntity<OwnerResponse> getPropertyOwner(@PathVariable int propertyId, @RequestHeader("Authorization") String token) {
        OwnerResponse ownerResponse = new OwnerResponse();
        token = token.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user == null) {
            ownerResponse.setMessage("Not a registered user");
            return new ResponseEntity<>(ownerResponse, HttpStatus.FORBIDDEN);
        }
        else if (propertyService.getPropertyById(propertyId) == null) {
            ownerResponse.setMessage("No property with this id");
            return new ResponseEntity<>(ownerResponse, HttpStatus.NOT_FOUND);
        }
        User owner = propertyService.getPropertyById(propertyId).getUser();
        ownerResponse = OwnerResponse.builder()
                .message("Owner details got successfully")
                .firstName(owner.getFirstName())
                .lastName(owner.getLastName())
                .email(owner.getEmail())
                .phoneNumber(owner.getPhoneNumber())
                .build();
        return new ResponseEntity<>(ownerResponse, HttpStatus.OK);
    }

    @GetMapping("/filterByPrice")
    public ResponseEntity<Page<PropertyResponse>> getAllPropertiesFilteredByPrice(
            Pageable pageable,
            @RequestParam(name = "min_price", defaultValue = "0") int minPrice,
            @RequestParam(name = "max_price", defaultValue = "100000000") int maxPrice) {
        Page<Property> properties = propertyService.getAllPropertiesFilteredByPrice(minPrice, maxPrice, pageable);
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

    @GetMapping("/filterByPropertyType")
    public ResponseEntity<Page<PropertyResponse>> getAllPropertiesFilteredByPropertyType(
            Pageable pageable,
            @RequestParam(name = "property_type", defaultValue = "APARTMENT") String propertyTypeStr) {
        if (!isValidPropertyType(propertyTypeStr)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        PropertyType propertyType = PropertyType.valueOf(propertyTypeStr.toUpperCase());
        Page<Property> properties = propertyService.getAllPropertiesFilteredByPropertyType(propertyType, pageable);
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

    @GetMapping("/filterByCity")
    public ResponseEntity<Page<PropertyResponse>> getAllPropertiesFilteredByCity(
            Pageable pageable,
            @RequestParam(name = "city_prefix", defaultValue = "Alex") String cityPrefix) {
        Page<Property> properties = propertyService.getAllPropertiesFilteredByCity(cityPrefix, pageable);
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
}
