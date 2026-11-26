package com.theprofessor.reviews.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theprofessor.reviews.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID>{

}
