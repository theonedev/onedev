package io.onedev.server.util;

import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.*;

public class SemanticVersionTest {

	@Test
	public void parseVersion() {
		try {
			new SemanticVersion("1.0.0");
			new SemanticVersion("1.0.0.0");
			new SemanticVersion("0.5.10");
			new SemanticVersion("100.1000.10000.100000");
			new SemanticVersion("0.5.10.2-alpha3+build4");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		try {
			new SemanticVersion("00");
			assertFalse(true);
		} catch (ParseException e) {
		}

		try {
			new SemanticVersion("1.02");
			assertFalse(true);
		} catch (ParseException e) {
		}

		try {
			new SemanticVersion("1.0.4 build4");
			assertFalse(true);
		} catch (ParseException e) {
		}
	}
	
}