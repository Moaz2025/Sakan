package com.sakan;

import com.github.javafaker.Faker;
import com.sakan.property.*;
import com.sakan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.IntStream;

@Component
public class TestDataSeeder implements CommandLineRunner {
    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    private ImageService imageService;

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final LocationRepository locationRepository;
    private final Faker faker;

    public TestDataSeeder(UserRepository userRepository, PropertyRepository propertyRepository, LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.locationRepository = locationRepository;
        this.faker = new Faker(new java.util.Locale("en-US"));
    }

    public List<MultipartFile> readImagesAsMultipartFiles(String directoryPath) throws IOException {
        List<MultipartFile> multipartFiles = new ArrayList<>();
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jfif"));
            if (files != null) {
                for (File file : files) {
                    try (FileInputStream input = new FileInputStream(file)) {
                        MultipartFile multipartFile = new MockMultipartFile(
                                file.getName(),
                                file.getName(),
                                Files.probeContentType(file.toPath()),
                                input
                        );
                        multipartFiles.add(multipartFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return multipartFiles;
    }

    public static List<MultipartFile> pickRandomFiles(List<MultipartFile> files, int numberOfFiles) {
        if (files.size() <= numberOfFiles) {
            return new ArrayList<>(files);
        }
        Collections.shuffle(files);
        return files.subList(0, numberOfFiles);
    }

    @Override
    public void run(String... args) {
        int[] ids = {1, 2, 52, 53, 54, 55};
        IntStream.rangeClosed(1, 10).forEach(i -> {
            Random random = new Random();
            int randomIndex = random.nextInt(ids.length);
            int randomId = ids[randomIndex];
            var user = userRepository.findById(randomId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Property property = Property.builder()
                    .user(user)
                    .title(faker.lorem().sentence())
                    .description(faker.lorem().paragraph())
                    .saleStatus(SaleStatus.values()[random.nextInt(SaleStatus.values().length)])
                    .price(faker.number().numberBetween(500000, 10000000))
                    .propertyType(PropertyType.values()[random.nextInt(PropertyType.values().length)])
                    .size((float) faker.number().randomDouble(2, 50, 500))
                    .numberOfRooms(faker.number().numberBetween(1, 6))
                    .numberOfBathrooms(faker.number().numberBetween(1, 4))
                    .floorNumber(faker.number().numberBetween(1, 20))
                    .availabilityStatus(AvailabilityStatus.values()[random.nextInt(AvailabilityStatus.values().length)])
                    .buildingYear(faker.number().numberBetween(1900, 2024))
                    .listingDate(faker.date().past(365, java.util.concurrent.TimeUnit.DAYS))
                    .views(faker.number().numberBetween(0, 10000))
                    .build();
            propertyRepository.save(property);
            Location location = Location.builder()
                    .property(property)
                    .streetAddress(faker.address().streetAddress())
                    .city(faker.address().city())
                    .state(faker.address().state())
                    .country(faker.address().country())
                    .postalCode(faker.address().zipCode())
                    .build();
            locationRepository.save(location);
            List<MultipartFile> multipartFiles;
            try {
                multipartFiles = readImagesAsMultipartFiles("C:\\Users\\Moaz\\Desktop\\Images");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<String> imagesUrls = new ArrayList<>();
            List<String> cloudIds = new ArrayList<>();
            int numberOfFilesToPick = 5;
            List<MultipartFile> randomFiles = pickRandomFiles(multipartFiles, numberOfFilesToPick);
            for (MultipartFile randomFile : randomFiles) {
                Map result;
                try {
                    result = cloudinaryService.upload(randomFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String imageUrl = (String) result.get("url");
                String cloudId = (String) result.get("public_id");
                imagesUrls.add(imageUrl);
                cloudIds.add(cloudId);
            }
            for (int j = 0; j < imagesUrls.size(); j++) {
                var image = Image.builder()
                        .property(property)
                        .imageUrl(imagesUrls.get(j))
                        .cloudId(cloudIds.get(j))
                        .build();
                imageService.addImage(image);
            }
        });
        System.out.println("Seeded 10 properties with random data into the database");
    }
}
