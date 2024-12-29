package com.casestudy.carmanagement.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.casestudy.carmanagement.entities.Car;

@Repository
public interface CarRepository extends JpaRepository<Car, Long>{

	Car findByCarModel(String carModel);
	@Query(value = "SELECT c.* FROM car c WHERE STR_TO_DATE(c.start_date, '%d-%m-%Y') <= :startDate " +
            "AND STR_TO_DATE(c.end_date, '%d-%m-%Y') >= :endDate " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM booking b WHERE b.car_id = c.id " +
            "  AND NOT (" +
            "    :startDate > STR_TO_DATE(b.end_date, '%d-%m-%Y') OR :endDate < STR_TO_DATE(b.start_date, '%d-%m-%Y')" +
            "  )" +
            ")", nativeQuery = true)
   List<Car> findAvailableCars(LocalDate startDate, LocalDate endDate);



}
