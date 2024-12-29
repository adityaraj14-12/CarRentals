package com.casestudy.carmanagement.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.casestudy.carmanagement.entities.Car;
import com.casestudy.carmanagement.repositories.CarRepository;
import com.casestudy.carmanagement.services.CarService;
import com.casestudy.carmanagement.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class CarController {

	private static Logger logger = LoggerFactory.getLogger(CarController.class); 
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
            System.out.println("Is Authenticated : "+jwtUtil.validateToken(token, username));
            return jwtUtil.validateToken(token, username);  // Validate the token
        }
        return false; // No token found or invalid
    }

    private boolean hasRole(HttpServletRequest request, String role) {
        String token = getTokenFromRequest(request);
        if (token != null) {
        	 String username = jwtUtil.extractUsername(token);
             String userRole = jwtUtil.extractRole(token);
             System.out.println("Extracted Role controller in Car Management Service: " + userRole);
             System.out.println("Role being checked in Car Management Service: " + role);
             System.out.println("In hasRole in Car Management Service: " +userRole.equals(role));
             
             logger.info("Token from request: " + token);
             logger.info("Authentication status: " + jwtUtil.validateToken(token, username));
             logger.info("User role: " + jwtUtil.extractRole(token));
             return userRole.equals(role);
        }
        return false; // No token found or invalid
    }

    // Get all cars - Restricted to ADMIN role

   // @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/api/cars")
    public ResponseEntity<List<Car>> getAllCars(HttpServletRequest request) {
        if (!isAuthenticated(request) || !hasRole(request, "admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Forbidden if not an admin
        }

        List<Car> cars = carRepo.findAll();
        return ResponseEntity.ok(cars);  // Return all cars if the user is an admin
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
  //  @PreAuthorize("hasAuthority('admin')")
    @PostMapping("/api/cars/add")
    public ResponseEntity<Car> addCar(
        @RequestParam("carModel") String carModel,
        @RequestParam("location") String location,
        @RequestParam("price") Double price,
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate,
        @RequestParam("image") MultipartFile image,
        HttpServletRequest request) throws IOException {

    	  if (!isAuthenticated(request) || !hasRole(request, "admin")) {
              return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Forbidden if not an admin
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

    // Update a car - Requires Authentication (JWT Token) and ADMIN role
  //  @PreAuthorize("hasAuthority('admin')")
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
    	  if (!isAuthenticated(request) || !hasRole(request, "admin")) {
              return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Forbidden if not an admin
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

    //@PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/api/cars/delete/{id}")
    public ResponseEntity<String> deleteCar(@PathVariable Long id, HttpServletRequest request) {
        System.out.println("Inside deleteCar method for ID: " + id);
        

        // Check if the user is authenticated
        if (!isAuthenticated(request)) {
        	logger.warn("Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Invalid or missing token");
        }

        // Check if the user has the 'admin' role
        if (!hasRole(request, "admin")) {
        	logger.warn("Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: You do not have permission to delete this car");
        }

        // Proceed with deletion if checks pass
        if (carRepo.existsById(id)) {
            carRepo.deleteById(id);
            return ResponseEntity.ok("Car deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Car not found");
        }
    }




    // Get available cars - Public access (no token validation needed)
    @GetMapping("/api/cars/filter")
    public List<Car> getAvailableCars(@RequestParam String startDate, @RequestParam String endDate) throws ParseException {
        return carService.getAvailableCars(startDate, endDate);
    }


}


