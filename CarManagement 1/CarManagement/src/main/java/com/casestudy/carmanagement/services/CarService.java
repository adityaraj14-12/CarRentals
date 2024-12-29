package com.casestudy.carmanagement.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.casestudy.carmanagement.entities.Car;
import com.casestudy.carmanagement.repositories.CarRepository;

@Service
public class CarService {

    @Autowired
    private CarRepository carRepository;

    // Date format for parsing (URL format yyyy-MM-dd)
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Date format for parsing car's availability (assumed to be dd-MM-yyyy)
    private static final DateTimeFormatter carDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public List<Car> getAvailableCars(String startDateStr, String endDateStr) {
        // Parse the start and end dates from the URL (assumed to be yyyy-MM-dd format)
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        // Log the requested date range for debugging
        System.out.println("Requested Date Range: " + startDate + " to " + endDate);

        // Fetch all cars from the repository
        List<Car> cars = carRepository.findAll();

        // Filter cars based on their availability within the given date range
        return cars.stream()
                .filter(car -> {
                    // Parse the car's start and end dates (assumed to be dd-MM-yyyy format)
                    LocalDate carStartDate = LocalDate.parse(car.getStartDate(), carDateFormatter);
                    LocalDate carEndDate = LocalDate.parse(car.getEndDate(), carDateFormatter);

                    // Log the car's availability dates for debugging
                    System.out.println("Car Availability: " + carStartDate + " to " + carEndDate);

                    // Ensure that the car's availability fully overlaps with the requested range
                    boolean isAvailable =((carEndDate.isAfter(endDate) || carEndDate.isEqual(endDate)) && (carStartDate.isBefore(startDate) || carStartDate.isEqual(startDate)));

                    // Log the result of availability check for debugging
                    System.out.println("Car Available: " + isAvailable);
                    return isAvailable;
                })
                .collect(Collectors.toList());
    }

    public Car updateCarAvailability(Long carId, String bookedStartDate, String bookedEndDate) throws Exception {
        // Retrieve the car entity
        Optional<Car> carOptional = carRepository.findById(carId);
        if (carOptional.isEmpty()) {
            throw new Exception("Car not found");
        }

        Car car = carOptional.get();

        // Convert the booked dates to LocalDate
        LocalDate bookedStart = LocalDate.parse(bookedStartDate, formatter);
        LocalDate bookedEnd = LocalDate.parse(bookedEndDate, formatter);

        // Check if the booking dates overlap with the car's availability
        LocalDate carStart = LocalDate.parse(car.getStartDate(), formatter);
        LocalDate carEnd = LocalDate.parse(car.getEndDate(), formatter);

        if (bookedStart.isBefore(carEnd) && bookedEnd.isAfter(carStart)) {
            // Split the availability into two periods (before and after the booking)
            if (bookedStart.isAfter(carStart)) {
                car.setEndDate(bookedStart.minusDays(1).toString()); // Update the availability before the booking
            }
            if (bookedEnd.isBefore(carEnd)) {
                car.setStartDate(bookedEnd.plusDays(1).toString()); // Update the availability after the booking
            }
        } else {
            throw new Exception("Car is not available for the selected booking period.");
        }

        // Save the updated car entity (make sure to return the saved entity or handle further business logic)
        return carRepository.save(car);
    }
}