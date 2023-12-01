package pax.tecs.psconfig.web.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


class LoginControllerTest {

	@Test
	void testLogIn() {
		LoginController lController = new LoginController();
		assertEquals("login", lController.login());
	}

}
