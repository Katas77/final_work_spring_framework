
package com.example.FinalWorkDevelopmentOnSpringFramework.service.impl;


import com.example.FinalWorkDevelopmentOnSpringFramework.modelEntity.Booking;
import com.example.FinalWorkDevelopmentOnSpringFramework.modelEntity.Room;
import com.example.FinalWorkDevelopmentOnSpringFramework.repository.BookingRepository;
import com.example.FinalWorkDevelopmentOnSpringFramework.service.BookingService;
import com.example.FinalWorkDevelopmentOnSpringFramework.statistics.kafka.service.ServiceProducer;
import com.example.FinalWorkDevelopmentOnSpringFramework.statistics.kafka.template.model.BookingEvent;
import com.example.FinalWorkDevelopmentOnSpringFramework.utils.BeanUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookRepository;
    private final ServiceProducer producer;


    @Override
    public List<Booking> findAll(int pageNumber, int pageSize) {
        return bookRepository.findAll(PageRequest.of(pageNumber, pageSize)).getContent();
    }

    @Override
    public Booking findById(Long id) {
        return bookRepository.findById(id).orElseThrow(() ->
        new EntityNotFoundException(MessageFormat.format("Booking with ID {0} not found", id)));
    }

    @Override
    public ResponseEntity<String> save(Booking booking) {
        if (booking.getDateCheck_in().isBefore(LocalDate.now()) || booking.getDateCheck_out().isBefore(LocalDate.now())) {
            return ResponseEntity.ok("selected dates are already past tense");
        }
        if (booking.getDateCheck_in().isAfter(booking.getDateCheck_out())) {
            return ResponseEntity.ok("Check-in date must be before check-out date");
        }
        if (booking.getDateCheck_in().equals(booking.getDateCheck_out())) {
            return ResponseEntity.ok("indicate different check-in and check-out dates");
        }
        if (isCheckInTime(booking.getDateCheck_in(), booking.getRoom())) {
            return ResponseEntity.ok("Check-in from  " + booking.getRoom().getUnavailableBegin() + "   to   " + booking.getRoom().getUnavailableEnd() + " is not possible");
        }
        if (isCheckInTime(booking.getDateCheck_out(), booking.getRoom())) {
            return ResponseEntity.ok("Check_out from  " + booking.getRoom().getUnavailableBegin() + "    to   " + booking.getRoom().getUnavailableEnd() + " is not possible");
        }
        bookRepository.save(booking);
        BookingEvent bookingEvent=BookingEvent.builder()
                .dateCheck_in(String.valueOf(booking.getDateCheck_in()))
                .dateCheck_out(String.valueOf(booking.getDateCheck_out()))
                .recordingFacts(String.valueOf(LocalDateTime.now()))
                .UserId(booking.getUser().getId())
                .build();
        producer.sendBookingEvent(bookingEvent);
        return ResponseEntity.ok(MessageFormat.format("Booking with Id -    {0} save", booking.getId()));
    }

    @Transactional
    @Override
    public ResponseEntity<String> update(Booking booking) {
        Optional<Booking> repositoryById = bookRepository.findById(booking.getId());
        if (repositoryById.isPresent()) {
            BeanUtils.copyNonNullProperties(booking, repositoryById.get());
            save(repositoryById.get());
            return ResponseEntity.ok(MessageFormat.format("Booking  with ID {0} updated", booking.getId()));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(MessageFormat.format("Booking with ID {0} not found", booking.getId()));
        }
    }


    @Override
    public void dellAll() {
        bookRepository.deleteAll();
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteById(Long id) {
        Optional<Booking> newsRepositoryById = bookRepository.findById(id);
        if (newsRepositoryById.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(MessageFormat.format("Booking  with ID {0} not found", id));
        } else {
            bookRepository.deleteById(id);
            return ResponseEntity.ok(MessageFormat.format("Booking  with ID {0} deleted", id));
        }
    }


    boolean isCheckInTime(LocalDate localDate, Room room) {
        return !(localDate.isBefore(room.getUnavailableBegin()) || localDate.isAfter(room.getUnavailableEnd()));
    }

}
