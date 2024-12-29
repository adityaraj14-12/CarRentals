package com.example.controller;

import com.example.model.Customer;
import com.example.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // Registration endpoint with validation
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody Customer customer, BindingResult result) {
        // Log the password to see if there are any unexpected characters
        System.out.println("Password being validated: " + customer.getPassword());

        List<String> errors = new ArrayList<>();

        // Check if email already exists
        if (customerService.findByEmail(customer.getEmail()) != null) {
            errors.add("Email already exists");
            return ResponseEntity.badRequest().body(errors);
        }

        // Check validation issues for other fields
        if (result.hasErrors()) {
            errors.addAll(result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.badRequest().body(errors);  // Return validation errors
        }

        // Proceed with registration
        Customer savedCustomer = customerService.registerCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }

}
