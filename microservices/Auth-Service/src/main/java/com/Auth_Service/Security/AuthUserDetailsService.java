package com.Auth_Service.Security;

import com.Auth_Service.DTO.UserDto;
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
public class AuthUserDetailsService
        implements UserDetailsService {

    private final UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        UserDto user =
                userClient.getByEmail(email);

        System.out.println("User: " + user);
        System.out.println("Email: " + email);
        System.out.println("Password: " + user.getPassword());
        System.out.println("Role: " + user.getRole());

        if (user == null) {

            throw new UsernameNotFoundException(
                    "User not found"
            );
        }

        return new User(

                user.getEmail(),

                user.getPassword(),

                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole()
                        )
                )
        );
    }
}