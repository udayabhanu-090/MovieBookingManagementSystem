package com.axis.MovieTicket.service.impl;

import static java.lang.Boolean.TRUE;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
//import pl.markowski.kinoteatr.model.Movie;
//import pl.markowski.kinoteatr.model.Repertoire;
//import pl.markowski.kinoteatr.model.Reservation;
//import pl.markowski.kinoteatr.model.ReserveSeatConfiguration;
//import pl.markowski.kinoteatr.model.SeatReservation;
//import pl.markowski.kinoteatr.model.Spectacle;
//import pl.markowski.kinoteatr.model.Ticket;
//import pl.markowski.kinoteatr.repository.MovieRepository;
//import pl.markowski.kinoteatr.repository.RepertoireRepository;
//import pl.markowski.kinoteatr.repository.ReservationRepository;
//import pl.markowski.kinoteatr.repository.SpectacleRepository;
//import pl.markowski.kinoteatr.repository.TicketRepository;
//import pl.markowski.kinoteatr.repository.UserRepository;
//import pl.markowski.kinoteatr.service.ReservationService;

import com.axis.MovieTicket.model.Movie;
import com.axis.MovieTicket.model.Repertoire;
import com.axis.MovieTicket.model.Reservation;
import com.axis.MovieTicket.model.ReserveSeatConfiguration;
import com.axis.MovieTicket.model.SeatReservation;
import com.axis.MovieTicket.model.Spectacle;
import com.axis.MovieTicket.model.Ticket;
import com.axis.MovieTicket.repository.MovieRepository;
import com.axis.MovieTicket.repository.RepertoireRepository;
import com.axis.MovieTicket.repository.ReservationRepository;
import com.axis.MovieTicket.repository.SpectacleRepository;
import com.axis.MovieTicket.repository.TicketRepository;
import com.axis.MovieTicket.repository.UserRepository;
import com.axis.MovieTicket.service.ReservationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
//@RequiredArgsConstructor

@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private static final List<String> rows = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");

    private final RepertoireRepository repertoireRepository = null;
    private final MovieRepository movieRepository = null;
    private final SpectacleRepository spectacleRepository = null;
    private final TicketRepository ticketRepository = null;
    private final ReservationRepository reservationRepository = null;
    private final UserRepository userRepository = null;

    @Override
    public String showMovieReservationPage(final String movieName, final Model model) {
        final Movie movie = movieRepository.findByTitle(movieName);
        final List<Repertoire> repertoires = repertoireRepository.findByMovieId(movie.getId());
        model.addAttribute("repertoires", repertoires);
        return "reservation-movie";
    }

    @Override
    public String showSpectacleReservationPage(final String spectacleName, final Model model) {
        final Spectacle spectacle = spectacleRepository.findByTitle(spectacleName);
        final List<Repertoire> repertoires = repertoireRepository.findBySpectacleId(spectacle.getId());
        model.addAttribute("repertoires", repertoires);
        return "reservation-spectacle";
    }

    @Override
    public String showMovieReservationSeatPage(final String movieName, final Long repertoireId, final Model model) {
        reserveSeats(model, repertoireId);
        model.addAttribute("movieName", movieName);
        addRows(model, repertoireId);
        return "reservation-seat-movie";
    }

    @Override
    public String spectacleReservationSeatPage(final String spectacleName, final Long repertoireId, final Model model) {
        reserveSeats(model, repertoireId);
        model.addAttribute("spectacleName", spectacleName);
        addRows(model, repertoireId);
        return "reservation-seat-spectacle";
    }

    @Override
    public String reservation(final ReserveSeatConfiguration reserveSeatConfiguration, final Long repertoireId, final Principal principal) {
        final List<String> reservedSeats = getReservedSeats(reserveSeatConfiguration);
        if (reservedSeats.size() > 0 && reservedSeats.size() <= 15) {
            final UUID uuid = UUID.randomUUID();
            final Ticket ticket = new Ticket();
            ticket.setSeat(String.join(",", reservedSeats));
            ticket.setUuid(uuid);
            ticketRepository.save(ticket);

            final Reservation reservation = new Reservation();
            reservation.setTicket(ticketRepository.findByUuid(uuid).orElse(null));
            Repertoire repertoire = repertoireRepository.findById(repertoireId).orElse(null);
            try {
                if (repertoire != null) {
                    reservation.setMovie(movieRepository.findByTitle(repertoire.getMovie().getTitle()));
                }
            } catch (NullPointerException e) {
                if (repertoire.getSpectacle() != null) {
                    reservation.setSpectacle(spectacleRepository.findByTitle(repertoire.getSpectacle().getTitle()));
                }
            }
            reservation.setRepertoire(repertoire);
            reservation.setUser(userRepository.findByUsername(principal.getName()));
            reservationRepository.save(reservation);
            return "redirect:/successful";
        } else {
            return "redirect:/unsuccessful";
        }
    }

    private void reserveSeats(final Model model, @PathVariable("repertoireId") final Long repertoireId) {
        final ReserveSeatConfiguration reserveSeatConfigurationMovie = new ReserveSeatConfiguration();
        final SeatReservation seatReservation = new SeatReservation();
        final Map<String, Boolean> mapMovie = new HashMap<>();
        getReservedSeats(repertoireId).forEach(seat -> mapMovie.put(seat, true));
        reserveSeatConfigurationMovie.setMap(mapMovie);
        reserveSeatConfigurationMovie.setSeatReservation(seatReservation);
        model.addAttribute("seatInfo", reserveSeatConfigurationMovie);
    }

    private Set<String> getReservedSeats(@PathVariable("repertoireId") final Long repertoireId) {
        final Repertoire repertoire = repertoireRepository.getOne(repertoireId);
        final Set<String> reservedSeats = new HashSet<>();
        for (final Reservation reservation : repertoire.getReservations()) {
            reservedSeats.addAll(Arrays.asList(reservation.getTicket().getSeat().split(",")));
        }
        log.info("Reserved: " + reservedSeats);
		return reservedSeats;
        }
    private void addRows(final Model model, final Long repertoireId) {
        model.addAttribute("repertoireId", repertoireId);
        model.addAttribute("rows", rows);
    }

    private List<String> getReservedSeats(@ModelAttribute("seatInfo") final ReserveSeatConfiguration reserveSeatConfiguration) {
        final List<String> reservedSeats = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : reserveSeatConfiguration.getMap().entrySet()) {
            if (TRUE.equals(entry.getValue())) {
                reservedSeats.add(entry.getKey());
            }
        }
        return reservedSeats;
    }
}