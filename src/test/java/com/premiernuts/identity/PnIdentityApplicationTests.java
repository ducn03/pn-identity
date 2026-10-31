package com.premiernuts.identity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PnIdentityApplicationTests {

	@Test
	void contextLoads() {
		System.out.println("BCRYPT_ADMIN123=" + com.premiernuts.identity.core.utils.PasswordHelper.hash("admin123"));
	}

}
