package com.casestudy.carmanagement.repositories;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
 
import com.casestudy.carmanagement.entities.Car;
 
import java.time.LocalDate;
import java.util.List;
 
@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
 
    @Query(value = "SELECT c.* FROM car c WHERE c.start_date <= :startDate " +
                   "AND c.end_date >= :endDate " +
                   "AND NOT EXISTS (" +
                   "  SELECT 1 FROM booking b WHERE b.car_id = c.id " +
                   "  AND NOT (" +
                   "    :startDate > b.end_date OR :endDate < b.start_date" +
                   "  )" +
                   ")", nativeQuery = true)
    List<Car> findAvailableCars(LocalDate startDate, LocalDate endDate);
 
 
}
 