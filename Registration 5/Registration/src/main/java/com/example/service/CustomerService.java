package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.model.Customer;
import com.example.repository.CustomerRepository;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    // Create an instance of BCryptPasswordEncoder
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Customer registerCustomer(Customer customer) {
        System.out.println("Hello Service");

        // Ensure password is valid before hashing
        String password = customer.getPassword();
        if (!isPasswordValid(password)) {
            throw new IllegalArgumentException("Password is not strong enough");
        }

        // Hash the password before saving
        String hashedPassword = passwordEncoder.encode(password);
        customer.setPassword(hashedPassword);  // Set the hashed password

        return customerRepository.save(customer);  // Save the customer with the hashed password
    }

    private boolean isPasswordValid(String password) {
        // Apply the validation logic for password strength manually, if needed
        // Use the regex pattern directly or other logic to check validity
        return password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    // Find customer by email for login
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }
}
