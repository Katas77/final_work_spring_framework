package com.example.FinalWorkDevelopmentOnSpringFramework.service;


import com.example.FinalWorkDevelopmentOnSpringFramework.modelEntity.Room;
import org.springframework.http.ResponseEntity;


import java.util.List;

public interface RoomService {

    List<Room> findAll(int pageNumber, int pageSize);

    Room findById(Long id);

    ResponseEntity<String> save(Room room);

    ResponseEntity<String> update(Room room);

    ResponseEntity<String> deleteById(Long id);


}