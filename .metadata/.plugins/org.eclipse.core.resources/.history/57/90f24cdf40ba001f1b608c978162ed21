package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
	
	// In your BookingRepository
	public List<Booking> findByUserId(Long userId);

}
