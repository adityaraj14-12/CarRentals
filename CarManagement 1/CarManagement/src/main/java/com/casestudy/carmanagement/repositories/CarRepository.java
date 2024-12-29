package com.casestudy.carmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.casestudy.carmanagement.entities.Car;

@Repository
public interface CarRepository extends JpaRepository<Car, Long>{

	Car findByCarModel(String carModel);

}
