package com.gitplex.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gitplex.commons.util.FileUtils;

public class LoadPropsTest {

	private File file;
	
	@Before
	public void setup() {
		try {
			file = File.createTempFile("load", ".properties");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void populateFile(String content) {
		try {
			FileUtils.writeStringToFile(file, content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void shouldTrimKey() {
		populateFile("key1=value1\n key1 =value2");
		Properties props = FileUtils.loadProperties(file);
		assertEquals("value2", props.getProperty("key1"));
		assertEquals(1, props.keySet().size());
	}

	@Test
	public void shouldTrimValue() {
		populateFile("key1 = value1 \r key2=   ");
		Properties props = FileUtils.loadProperties(file);
		assertEquals("value1", props.getProperty("key1"));
		assertNull(props.getProperty("key2"));
	}

	@Test
	public void shouldHandleEqualSignInValue() {
		populateFile("key1==\r\nkey2= a =b");
		Properties props = FileUtils.loadProperties(file);
		assertEquals("=", props.getProperty("key1"));
		assertEquals("a =b", props.getProperty("key2"));
	}

	@Test
	public void shouldHandleColonSeparator() {
		populateFile("key1:=\n\rkey2: a :b\r\rkey3=a:b");
		Properties props = FileUtils.loadProperties(file);
		assertEquals("=", props.getProperty("key1"));
		assertEquals("a :b", props.getProperty("key2"));
		assertEquals("a:b", props.getProperty("key3"));
	}

	@After
	public void teardown() {
		if (file != null)
			file.delete();
	}
}
