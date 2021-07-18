package io.onedev.server.product;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	@org.junit.Test
	public void test() throws InterruptedException, ExecutionException {
		Pattern PATTERN_ATTACHMENT = Pattern.compile("\\[(.+?)\\]\\s*\\((/uploads/.+?)\\)");

		StringBuffer buffer = new StringBuffer();  
	    Matcher matcher = PATTERN_ATTACHMENT.matcher("hello [kb.txt](/uploads/kb.txt)");  
	    while (matcher.find()) {  
	    	System.out.println(matcher.group(1));
	    	System.out.println(matcher.group(2));
	    	matcher.appendReplacement(buffer, "[kb.txt](hello.txt)");
	    }  
	    matcher.appendTail(buffer);  
		
	    System.out.println(buffer);
	}

}