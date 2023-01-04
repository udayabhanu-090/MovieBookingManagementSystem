package com.axis.MovieTicket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.axis.MovieTicket.model.Spectacle;

@Repository
public interface SpectacleRepository extends JpaRepository<Spectacle, Long> {

    Spectacle findByTitle(final String title);
}