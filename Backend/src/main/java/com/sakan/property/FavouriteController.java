package com.sakan.property;

import com.sakan.config.JwtService;
import com.sakan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/favourite")
@CrossOrigin(origins = "http://localhost:3000")
public class FavouriteController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ImageService imageService;

    @Autowired
    FavouriteService favouriteService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/add/{propertyId}")
    public ResponseEntity<String> addToFavourites(@RequestHeader("Authorization") String token, @PathVariable int propertyId) {
        token = token.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        int userId = user.getId();
        if (propertyService.getPropertyById(propertyId) == null) {
            return new ResponseEntity<>("No property with this id", HttpStatus.NOT_FOUND);
        }
        else if (favouriteService.getFavouriteByUserIdAndPropertyId(userId, propertyId) != null) {
            return new ResponseEntity<>("Property already added to favourites", HttpStatus.BAD_REQUEST);
        }
        Property property = propertyService.getPropertyById(propertyId);
        var favourite = Favourite.builder()
                .user(user)
                .property(property)
                .build();
        favouriteService.addFavourite(favourite);
        return new ResponseEntity<>("Property added to favourites", HttpStatus.CREATED);
    }

    @DeleteMapping("/remove/{propertyId}")
    public ResponseEntity<String> removeFromFavourites(@RequestHeader("Authorization") String token, @PathVariable int propertyId) {
        token = token.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        int userId = user.getId();
        if (propertyService.getPropertyById(propertyId) == null) {
            return new ResponseEntity<>("No property with this id", HttpStatus.NOT_FOUND);
        }
        else if (favouriteService.getFavouriteByUserIdAndPropertyId(userId, propertyId) == null) {
            return new ResponseEntity<>("Property not found at favourites", HttpStatus.NOT_FOUND);
        }
        Favourite favourite = favouriteService.getFavouriteByUserIdAndPropertyId(userId, propertyId);
        favouriteService.deleteFavourite(favourite);
        return new ResponseEntity<>("Property removed from favourites", HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<PropertyResponse>> getAllUserFavourites(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        int userId = user.getId();
        List<Favourite> favourites  = favouriteService.getAllUserFavourites(userId);
        List<Property> properties = new ArrayList<>();
        for (Favourite favourite : favourites) {
            Property property = propertyService.getPropertyById(favourite.getProperty().getId());
            properties.add(property);
        }
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
