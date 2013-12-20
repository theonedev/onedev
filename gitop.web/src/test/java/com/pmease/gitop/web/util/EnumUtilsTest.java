package com.pmease.gitop.web.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.pmease.gitop.model.permission.operation.GeneralOperation;

public class EnumUtilsTest {

	@Test
	public void testInc() {
		GeneralOperation op = GeneralOperation.READ;
		assertEquals(EnumUtils.inc(op), GeneralOperation.WRITE);
		
		op = GeneralOperation.ADMIN;
		assertEquals(EnumUtils.inc(op), GeneralOperation.ADMIN);
	}
	
	@Test
	public void testDec() {
		GeneralOperation op = GeneralOperation.READ;
		assertEquals(EnumUtils.dec(op), GeneralOperation.NO_ACCESS);
		
		op = GeneralOperation.NO_ACCESS;
		assertEquals(EnumUtils.dec(op), GeneralOperation.NO_ACCESS);
	}
	
}
