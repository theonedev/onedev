package com.gitplex.commons.hibernate.command;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.gitplex.commons.hibernate.command.UpgradeUtils;

public class UpgradeUtilsTest {

	@Test
	public void test() {
		String from = ""
				+ "# specify RUN_AS_USER here\n"
				+ "RUN_AS_USER = robin \n"
				+ "some String here RUN_AS_USER=\"\"\n";
		String to = ""
				+ "# specify RUN_AS_USER here\n"
				+ "#RUN_AS_USER=\n"
				+ "some String here RUN_AS_USER=\"\"\n";
		String expected = ""
				+ "# specify RUN_AS_USER here\n"
				+ "RUN_AS_USER = robin \n"
				+ "some String here RUN_AS_USER=\"\"\n";
		assertEquals(expected, UpgradeUtils.copyRunAs(from, to));
		
		from = ""
				+ "# specify RUN_AS_USER here\r\n"
				+ "#RUN_AS_USER = robin \r\n"
				+ "some String here RUN_AS_USER=\"\"\r\n";
		to = ""
				+ "# specify RUN_AS_USER here\r\n"
				+ "#RUN_AS_USER=\r\n"
				+ "some String here RUN_AS_USER=\"\"\r\n";
		expected = ""
				+ "# specify RUN_AS_USER here\r\n"
				+ "#RUN_AS_USER = robin \r\n"
				+ "some String here RUN_AS_USER=\"\"\r\n";
		assertEquals(expected, UpgradeUtils.copyRunAs(from, to));
		
		from = ""
				+ "# specify RUN_AS_USER here\n"
				+ "#RUN_AS_USER=\n"
				+ "some String here RUN_AS_USER=\"\"\n";
		to = ""
				+ "# specify RUN_AS_USER here\n"
				+ "#RUN_AS_USER=\n"
				+ "some String here RUN_AS_USER=\"\"\n";
		expected = ""
				+ "# specify RUN_AS_USER here\n"
				+ "#RUN_AS_USER=\n"
				+ "some String here RUN_AS_USER=\"\"\n";
		assertEquals(expected, UpgradeUtils.copyRunAs(from, to));
		
		from = ""
				+ "# specify RUN_AS_USER here\n"
				+ "## RUN_AS_USER = robinshen \n"
				+ "some String here RUN_AS_USER=\"\"\n";
		to = ""
				+ "# specify RUN_AS_USER here\n"
				+ "#RUN_AS_USER=\n"
				+ "some String here RUN_AS_USER=\"\"\n";
		expected = ""
				+ "# specify RUN_AS_USER here\n"
				+ "## RUN_AS_USER = robinshen \n"
				+ "some String here RUN_AS_USER=\"\"\n";
		assertEquals(expected, UpgradeUtils.copyRunAs(from, to));
	}

}
