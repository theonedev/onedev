package com.pmease.gitop.web.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.wicket.util.io.IOUtils;
import org.junit.Test;

import com.ibm.icu.text.CharsetDetector;

public class CharsetDetectorTest {

	@Test public void testDetect() throws IOException, TikaException {
		InputStream in = new BufferedInputStream(new FileInputStream(new File("/Users/zhenyu/good")));
		CharsetDetector detector = new CharsetDetector();
		detector.enableInputFilter(true);
		detector.setText(in);
		System.out.println(detector.detect().getName());
		IOUtils.closeQuietly(in);
	}
}
