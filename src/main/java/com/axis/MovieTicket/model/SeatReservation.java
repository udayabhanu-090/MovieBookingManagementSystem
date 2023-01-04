package com.axis.MovieTicket.model;

import lombok.Data;

@Data
public class SeatReservation {

    private String seat;
    private boolean active;

    public boolean isActive() {
        return active;
    }
}