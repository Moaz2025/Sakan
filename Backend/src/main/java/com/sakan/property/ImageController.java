package com.sakan.property;

import com.sakan.config.JwtService;
import com.sakan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/image")
@CrossOrigin(origins = "http://localhost:3000")
public class ImageController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    ImageService imageService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PropertyService propertyService;

    @PostMapping("/upload/{propertyId}")
    public ResponseEntity<String> uploadImages(@PathVariable int propertyId, @RequestHeader("Authorization") String token, @RequestParam List<MultipartFile> multipartFiles) throws IOException {
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
        List<String> imagesUrls = new ArrayList<>();
        List<String> cloudIds = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            BufferedImage bufferedImage = ImageIO.read(multipartFile.getInputStream());
            if (bufferedImage == null) {
                return new ResponseEntity<>("Image not valid", HttpStatus.BAD_REQUEST);
            }
            Map result = cloudinaryService.upload(multipartFile);
            String imageUrl = (String) result.get("url");
            String cloudId = (String) result.get("public_id");
            imagesUrls.add(imageUrl);
            cloudIds.add(cloudId);
        }
        for (int i = 0; i < imagesUrls.size(); i++) {
            var image = Image.builder()
                    .property(propertyService.getPropertyById(propertyId))
                    .imageUrl(imagesUrls.get(i))
                    .cloudId(cloudIds.get(i))
                    .build();
            imageService.addImage(image);
        }
        return new ResponseEntity<>("Images uploaded successfully", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{propertyId}")
    public ResponseEntity<String> deleteImage(@PathVariable int propertyId, @RequestHeader("Authorization") String token, @RequestParam String imageUrl) {
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
        List<Image> images = imageService.getAllPropertyImages(propertyId);
        boolean found = false;
        int index = -1;
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getImageUrl().equals(imageUrl)) {
                found = true;
                index = i;
                break;
            }
        }
        if (!found) {
            return new ResponseEntity<>("Image not found", HttpStatus.NOT_FOUND);
        }
        Image image = images.get(index);
        String cloudinaryImageId = image.getCloudId();
        try {
            cloudinaryService.delete(cloudinaryImageId);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to delete image from Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        imageService.deleteImage(image);
        return new ResponseEntity<>("image deleted successfully", HttpStatus.OK);
    }
}
