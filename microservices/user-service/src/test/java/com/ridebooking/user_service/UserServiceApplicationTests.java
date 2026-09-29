package com.ridebooking.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class UserServiceApplicationTests {

	@Test
	void generatePasswordHash() {

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		String hash = encoder.encode("Akash123");

		System.out.println(hash);
	}
}