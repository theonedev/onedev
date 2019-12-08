package io.onedev.server.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class VersionTest {

	@Test
	public void shouldDecodeVersionStringCorrectly() {
		Version version = new Version("1.2.3-build-100");
		assertEquals(version.toString(), "1.2.3-build-100");
		
		version = new Version("1.2-100");
		assertEquals(version.toString(), "1.2-100");
		
		version = new Version("1-build-10");
		assertEquals(version.toString(), "1-build-10");
	}

	@Test
	public void shouldConstructVersionCorrectly() {
		Version version = new Version(1, 2, 3, "beta", 100);
		assertEquals(version.toString(), "1.2.3-beta-100");
		
		version = new Version(1, 2, -1, "beta", -1);
		assertEquals(version.toString(), "1.2-beta");

		try {
			version = new Version(-1, 2, 1, "build", 1);
			fail("Can not detect construction errors.");
		} catch (Exception e) {
		}

		try {
			version = new Version(1, 2, -1, null, 1);
			fail("Can not detect construction errors.");
		} catch (Exception e) {
		}

		try {
			version = new Version(1, -1, 0, null, -1);
			fail("Can not detect construction errors.");
		} catch (Exception e) {
		}
	}

	@Test
	public void shouldCompareVersionCorrectly() {
		assertTrue(new Version("1").compareTo(new Version("1")) == 0);
		assertTrue(new Version("1.0.1").compareTo(new Version("1.0.1")) == 0);
		assertTrue(new Version("1").compareTo(new Version("1.0")) == 0);
		assertTrue(new Version("1.2-build").compareTo(new Version("1.2.0-build-0")) == 0);
		
		assertTrue(new Version("1").compareTo(new Version("2")) < 0);
		assertTrue(new Version("1.9.1").compareTo(new Version("1.10.1")) < 0);
		assertTrue(new Version("1.9.1-beta-1").compareTo(new Version("1.9.1-beta-4")) < 0);		
		assertTrue(new Version("1.9.1-alpha-1").compareTo(new Version("1.9.1-beta-1")) < 0);		
		assertTrue(new Version("1.9.1-alpha").compareTo(new Version("1.9.1-alpha-1")) < 0);		
		assertTrue(new Version("1.9-alpha").compareTo(new Version("1.9.1-alpha")) < 0);		
	}

	@Test
	public void shouldHandleCompatibleVersions() {
		assertTrue(new Version("1.0").isCompatible(new Version("1.0.1")));
		assertFalse(new Version("1.0").isCompatible(new Version("1.1")));
		assertTrue(new Version("1.0.2").isCompatible(new Version("1.0.3")));
		assertTrue(new Version("1.0.2").isCompatible(new Version("1.0.4-build-100")));
		assertFalse(new Version("6.0").isCompatible(new Version("5-beta-5")));
		assertTrue(new Version("6.3").isCompatible(new Version("6.3.2-rc-4")));
	}
}
