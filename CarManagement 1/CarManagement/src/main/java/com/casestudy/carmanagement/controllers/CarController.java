package com.casestudy.carmanagement.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.casestudy.carmanagement.entities.Car;
import com.casestudy.carmanagement.repositories.CarRepository;
import com.casestudy.carmanagement.services.CarService;
import com.casestudy.carmanagement.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class CarController {

    @Autowired
    private CarService carService;

    @Autowired
    private CarRepository carRepo;

    @Autowired
    private JwtUtil jwtUtil;

    // Helper method to get the JWT token from the Authorization header
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Extract the token from 'Bearer token'
        }
        return null; // No token found
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null) {
            String username = jwtUtil.extractUsername(token);
            return jwtUtil.validateToken(token, username);  // Validate the token
        }
        return false; // No token found or invalid
    }

    // Get all cars - Public access (no token validation needed)
    @GetMapping("/api/cars")
    public List<Car> getAllCars() {
        return carRepo.findAll();
    }

    // Get a car by its ID - Requires Authentication (JWT Token)
    @GetMapping("/api/cars/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id, HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Unauthorized if token is invalid
        }

        return carRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add a new car - Requires Authentication (JWT Token)
    @PostMapping("/api/cars/add")
    public ResponseEntity<Car> addCar(
        @RequestParam("carModel") String carModel,
        @RequestParam("location") String location,
        @RequestParam("price") Double price,
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate,
        @RequestParam("image") MultipartFile image,
        HttpServletRequest request) throws IOException {

        if (!isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Unauthorized access
        }

        Car car = new Car();
        car.setCarModel(carModel);
        car.setLocation(location);
        car.setPrice(price);
        car.setStartDate(startDate);
        car.setEndDate(endDate);
        car.setImage(image.getBytes());

        return ResponseEntity.ok(carRepo.save(car));  // Add car
    }

    // Update a car - Requires Authentication (JWT Token)
    @PutMapping("/api/cars/edit/{id}")
    public ResponseEntity<Car> updateCar(
        @PathVariable("id") Long id,
        @RequestParam("carModel") String carModel,
        @RequestParam("location") String location,
        @RequestParam("price") double price,
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate,
        @RequestParam("image") MultipartFile image,
        HttpServletRequest request) {

        if (!isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Unauthorized access
        }

        Optional<Car> existingCar = carRepo.findById(id);
        if (existingCar.isPresent()) {
            Car car = existingCar.get();
            car.setCarModel(carModel);
            car.setLocation(location);
            car.setPrice(price);
            car.setStartDate(startDate);
            car.setEndDate(endDate);
            try {
                car.setImage(image.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            carRepo.save(car);
            return ResponseEntity.ok(car);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a car - Requires Authentication (JWT Token)
    @DeleteMapping("/api/cars/delete/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable("id") Long id, HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Unauthorized access
        }

        Optional<Car> car = carRepo.findById(id);
        if (car.isPresent()) {
            carRepo.delete(car.get());
            return ResponseEntity.noContent().build(); // Successfully deleted, no content returned
        } else {
            return ResponseEntity.notFound().build(); // Car not found
        }
    }

    // Get available cars - Public access (no token validation needed)
    @GetMapping("/api/cars/filter")
    public List<Car> getAvailableCars(@RequestParam String startDate, @RequestParam String endDate) throws ParseException {
        return carService.getAvailableCars(startDate, endDate);
    }

    // Update car availability - Requires Authentication (JWT Token)
    @PutMapping("/api/cars/update-availability/{carId}")
    public ResponseEntity<String> updateCarAvailability(
        @PathVariable Long carId, 
        @RequestParam String bookedStartDate, 
        @RequestParam String bookedEndDate, 
        HttpServletRequest request) {

    	 System.out.println("Checking authentication..."); // Log for debugging
    	    if (!isAuthenticated(request)) {
    	        System.out.println("Unauthorized access - Invalid token");
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access - Invalid token");
    	    }

        try {
            // Proceed with updating the car availability
            Car updatedCar = carService.updateCarAvailability(carId, bookedStartDate, bookedEndDate);
            return ResponseEntity.ok("Car availability updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating car availability: " + e.getMessage());
        }
    }
}
