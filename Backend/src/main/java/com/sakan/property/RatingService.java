package com.sakan.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingService {
    @Autowired
    private RatingRepository ratingRepository;

    public Rating addRating(Rating rating){ return ratingRepository.save(rating); }

    public Rating editRating(Rating rating){ return ratingRepository.save(rating); }

    public void deleteRating(Rating rating){ ratingRepository.delete(rating); }

    public List<Rating> findByProperty(int propertyId){ return ratingRepository.findByPropertyId(propertyId); }

    public Rating findByPropertyAndUser(int propertyId, int userId){ return ratingRepository.findByPropertyIdAndUserId(propertyId, userId); }

    public List<Rating> getAllRatings(){ return ratingRepository.findAll(); }
}