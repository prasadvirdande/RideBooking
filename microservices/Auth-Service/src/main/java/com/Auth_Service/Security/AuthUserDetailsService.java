package com.Auth_Service.Security;

import com.Auth_Service.DTO.UserDto;
import com.Auth_Service.Feign.DriverClient;
import com.Auth_Service.Feign.UserClient;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final UserClient userClient;
    private final DriverClient driverClient;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserDto user = null;
        UserDto driver = null;

        try {
            user = userClient.getByEmail(email);
        } catch (Exception ignored) {
        }

        try {
            driver = driverClient.getByEmail(email);
        } catch (Exception ignored) {
        }

        if (user != null) {
            return new User(
                    user.getEmail(),
                    user.getPassword(),
                    List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole())
                    )
            );
        }

        if (driver != null) {
            return new User(
                    driver.getEmail(),
                    driver.getPassword(),
                    List.of(
                            new SimpleGrantedAuthority("ROLE_" + driver.getRole())
                    )
            );
        }

        throw new UsernameNotFoundException("User or Driver not found with email: " + email);
    }
}