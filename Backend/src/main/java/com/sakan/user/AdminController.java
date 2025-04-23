package com.sakan.user;

import com.sakan.auth.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/new-admin")
    public ResponseEntity assignNewAdmin(
            @RequestBody SignUpRequest request,
            Principal principal
    ) {
        adminService.assignNewAdmin(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


}
