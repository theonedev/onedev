package gitplex.product;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;

import com.pmease.commons.lang.Outline;
import com.pmease.commons.lang.java.JavaAnalyzer;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		String text = FileUtils.readFileToString(new File("w:\\temp\\RequestDetailPage.java"));
		
		Outline outline = new JavaAnalyzer().analyze(text).getOutline();
		String defs = outline.toString();
		byte[] defBytes = SerializationUtils.serialize(defs);
		
		System.out.println("defs size: " + defs.length()*2);
		System.out.println("defBytes size: " + defBytes.length);
		
		long time = System.currentTimeMillis();
		for (int i=0; i<100; i++) {
			SerializationUtils.deserialize(defBytes);
		}
		System.out.println(System.currentTimeMillis()-time);
		
		JavaAnalyzer analyzer = new JavaAnalyzer();
		time = System.currentTimeMillis();
		for (int i=0; i<100; i++) {
			analyzer.analyze(defs);
		}
		System.out.println(System.currentTimeMillis()-time);
		
	}
	
}