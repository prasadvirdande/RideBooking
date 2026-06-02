package com.ridebooking.driver_service.Security;

import com.ridebooking.driver_service.Entity.Driver;
import com.ridebooking.driver_service.Repository.Driverepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverUserService implements UserDetailsService {

    private final Driverepo driverepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Driver driver= driverepo.findByEmail(email).orElseThrow(()->new UsernameNotFoundException(email));
        return new User(driver.getEmail(),
        driver.getPassword(),
                List.of(new SimpleGrantedAuthority(driver.getRole().name())));
    }
}
