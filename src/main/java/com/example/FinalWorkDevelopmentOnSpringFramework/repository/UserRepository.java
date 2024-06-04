package com.example.FinalWorkDevelopmentOnSpringFramework.repository;


import com.example.FinalWorkDevelopmentOnSpringFramework.modelEntity.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {


    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByName(String name);

    @Query(value = "SELECT * FROM app_schema.our_user WHERE our_user.email=?", nativeQuery = true)
    Optional<User> findByEmailAddress( String emailAddress);




}