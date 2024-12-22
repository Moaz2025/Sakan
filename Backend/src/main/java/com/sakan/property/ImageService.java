package com.sakan.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    public Image addImage(Image image) {
        return imageRepository.save(image);
    }

    public void deleteImage(Image image) {
        imageRepository.delete(image);
    }

    public List<Image> getAllPropertyImages(int propertyId) {
        return imageRepository.findByPropertyId(propertyId);
    }
}
