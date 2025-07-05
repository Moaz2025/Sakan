package com.sakan.prediction;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class PredictionController {

    public boolean isValidFinishType(String finishType) {
        try {
            FinishType.valueOf(finishType.trim().toUpperCase().replace(" ", "_"));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isValidView(String view) {
        try {
            View.valueOf(view.trim().toUpperCase().replace(" ", "_"));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String convertFinishType(String finishType) {
        return FinishType.valueOf(finishType.trim().toUpperCase().replace(" ", "_")).getValue();
    }

    public String convertView(String view) {
        return View.valueOf(view.trim().toUpperCase().replace(" ", "_")).getValue();
    }

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestBody PredictionRequest predictionRequest) {
        try {
            if (!isValidFinishType(predictionRequest.getFinish_type())) {
                return new ResponseEntity<>("Invalid Finish Type", HttpStatus.BAD_REQUEST);
            }
            else if (!isValidView(predictionRequest.getView())) {
                return new ResponseEntity<>("Invalid View", HttpStatus.BAD_REQUEST);
            }

            predictionRequest.setType("Apartment");
            predictionRequest.setFinish_type(convertFinishType(predictionRequest.getFinish_type()));
            predictionRequest.setView(convertView(predictionRequest.getView()));

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonInput = objectMapper.writeValueAsString(predictionRequest);

            jsonInput = "\"" + jsonInput.replace("\"", "\\\"") + "\"";

            ProcessBuilder builder = new ProcessBuilder(
                    "python", "Backend/src/main/java/com/sakan/prediction/predict.py", jsonInput
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return ResponseEntity.ok(output.toString().trim());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Python script failed:\n" + output);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Java error: " + e.getMessage());
        }
    }
}

