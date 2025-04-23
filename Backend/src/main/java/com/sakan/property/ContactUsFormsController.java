package com.sakan.property;

import com.sakan.config.JwtService;
import com.sakan.user.Role;
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

@RestController
@RequestMapping("/contactUs")
@CrossOrigin(origins = "http://localhost:3000")
public class ContactUsFormsController {
    @Autowired
    private ContactUsFormsService contactUsFormsService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/newForm")
    public ResponseEntity<String> addNewForm(@RequestBody FormRequest formRequest) {
        Date currentDate = new Date();
        var form = Form.builder()
                .name(formRequest.getName())
                .email(formRequest.getEmail())
                .subject(formRequest.getSubject())
                .message(formRequest.getMessage())
                .date(currentDate)
                .build();
        contactUsFormsService.addForm(form);

        return new ResponseEntity<>("Form sent successfully", HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<FormResponse>> getAllForms(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        token = token.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        else if (user.getRole() == Role.USER) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Page<Form> forms = contactUsFormsService.getForms(pageNo, pageSize);
        List<FormResponse> formResponsesList = new ArrayList<>();
        for (Form form : forms) {
            FormResponse formResponse = new FormResponse();
            formResponse.setName(form.getName());
            formResponse.setEmail(form.getEmail());
            formResponse.setSubject(form.getSubject());
            formResponse.setMessage(form.getMessage());
            formResponse.setDate(form.getDate());
            formResponsesList.add(formResponse);
        }
        Page<FormResponse> formResponsePage = new PageImpl<>(formResponsesList, forms.getPageable(), forms.getTotalElements());
        return ResponseEntity.ok(formResponsePage);
    }
}
