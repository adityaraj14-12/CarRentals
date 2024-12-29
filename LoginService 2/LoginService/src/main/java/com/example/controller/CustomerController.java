package com.example.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.Customer;
import com.example.service.CustomerService;
import com.example.util.JwtUtil; // Import JWT utility class

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")  // Allow CORS from your frontend React app
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JwtUtil jwtUtil;  // Inject JwtUtil

    // Structured response for login
    public static class LoginResponse {
        private String message;
        private String token;

        // Constructor
        public LoginResponse(String message, String token) {
            this.message = message;
            this.token = token;
        }

        // Getters
        public String getMessage() {
            return message;
        }

        public String getToken() {
            return token;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@RequestBody Customer customer, BindingResult result) {
        List<String> errors = new ArrayList<>();

        // Check if email and password are provided
        if (customer.getEmail() == null || customer.getEmail().isEmpty()) {
            errors.add("Email is required");
        }

        if (customer.getPassword() == null || customer.getPassword().isEmpty()) {
            errors.add("Password is required");
        }

        // If there are validation errors, return them
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        // Check if the customer exists based on the provided email
        Customer foundCustomer = customerService.findByEmail(customer.getEmail());
        if (foundCustomer == null) {
            errors.add("User not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);  // User not found
        }

        // Validate the password (compare entered password with stored password)
        if (!customerService.isPasswordValid(customer.getPassword(), foundCustomer.getPassword())) {
            errors.add("Invalid password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);  // Invalid password
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(foundCustomer.getEmail(),foundCustomer.getId());

        // Return structured response
        LoginResponse response = new LoginResponse("Login successful", token);
        return ResponseEntity.ok(response);  // Return success response with token
    }
    
    
    
}
