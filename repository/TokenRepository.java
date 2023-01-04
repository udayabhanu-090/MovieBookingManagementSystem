package com.axis.MovieTicket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.axis.MovieTicket.model.Token;


@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Token findByValue(final String value);
}