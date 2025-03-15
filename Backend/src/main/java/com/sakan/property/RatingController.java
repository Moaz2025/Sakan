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
@RequestMapping("/rating")
@CrossOrigin(origins = "http://localhost:3000")
public class RatingController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private JwtService jwtService;


    @PostMapping("/new/{propertyId}")
    public ResponseEntity<String> addNewRating(@RequestHeader("Authorization") String token, @RequestParam int rate, @PathVariable int propertyId) {
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
        Property property = propertyService.getPropertyById(propertyId);
        if(ratingService.findByPropertyAndUser(property.getId(), user.getId()) != null){
            return new ResponseEntity<>("User rated this property before", HttpStatus.BAD_REQUEST);
        }
        if(rate < 1 || rate > 5){
            return new ResponseEntity<>("Not a valid rating", HttpStatus.BAD_REQUEST);
        }
        var rating = Rating.builder()
                .property(property)
                .user(user)
                .rating(rate)
                .build();
        ratingService.addRating(rating);
        return new ResponseEntity<>("Rate sent successfully", HttpStatus.CREATED);
    }
    @GetMapping("/get/{propertyId}")
    public ResponseEntity<RatingResponse> getPropertyRates(@PathVariable int propertyId) {
        if (propertyService.getPropertyById(propertyId) == null) {
            return new ResponseEntity("No property with this id", HttpStatus.NOT_FOUND);
        }
        List<Rating> ratings = ratingService.findByProperty(propertyId);
        if (ratings.isEmpty()) {
            RatingResponse ratingResponse = new RatingResponse();
            ratingResponse.setRate(0);
            ratingResponse.setNumOfRates(0);
            return new ResponseEntity<>(ratingResponse, HttpStatus.OK);
        }
        int sum = 0;
        for(Rating rating : ratings){
            sum += rating.getRating();
        }
        int count = ratings.size();
        RatingResponse ratingResponse = new RatingResponse();
        ratingResponse.setRate((float) sum / count);
        ratingResponse.setNumOfRates(count);
        return new ResponseEntity<>(ratingResponse, HttpStatus.OK);
    }
}