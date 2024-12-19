package com.sakan.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavouriteService {
    @Autowired
    FavouriteRepository favouriteRepository;

    public Favourite addFavourite(Favourite favourite) {
        return favouriteRepository.save(favourite);
    }

    public void deleteFavourite(Favourite favourite) {
        favouriteRepository.delete(favourite);
    }

    public Favourite getFavouriteByUserIdAndPropertyId(int userId, int propertyId) {
        return favouriteRepository.findByUserIdAndPropertyId(userId, propertyId);
    }

    public List<Favourite> getAllUserFavourites(int userId) {
        return favouriteRepository.findByUserId(userId);
    }
}
