package com.ridebooking.user_service.Repository;

import com.ridebooking.user_service.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {

   Optional<User>   findByEmail(String email);
   List<User> findByFirstNameContainingIgnoreCase(String keyword);


}
