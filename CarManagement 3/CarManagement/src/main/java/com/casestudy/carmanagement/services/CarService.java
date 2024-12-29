package com.casestudy.carmanagement.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.casestudy.carmanagement.entities.Car;
import com.casestudy.carmanagement.repositories.CarRepository;

@Service
public class CarService {

    @Autowired
    private CarRepository carRepository;

    // Use the correct format for parsing 'dd-MM-yyyy'
    private static final DateTimeFormatter carDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public List<Car> getAvailableCars(String startDateStr, String endDateStr) {
        // Parse the date strings using the correct formatter
        LocalDate startDate = LocalDate.parse(startDateStr, carDateFormatter);
        LocalDate endDate = LocalDate.parse(endDateStr, carDateFormatter);

        // Log requested date range for debugging
        System.out.println("Requested Date Range: " + startDate + " to " + endDate);

        // Fetch all cars from the repository
        List<Car> cars = carRepository.findAll();

        // Filter cars based on their availability within the given date range
        return carRepository.findAvailableCars(startDate, endDate);
    }
}
