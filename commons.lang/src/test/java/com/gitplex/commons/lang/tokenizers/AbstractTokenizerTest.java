package com.gitplex.commons.lang.tokenizers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import com.gitplex.commons.lang.tokenizers.CmToken;
import com.gitplex.commons.lang.tokenizers.Tokenizer;
import com.google.common.base.Splitter;
import com.google.common.io.Resources;

public abstract class AbstractTokenizerTest {

	protected void verify(Tokenizer tokenizer, String[] loadModes, String fileName) {
		try {
			byte[] bytes = Resources.toByteArray(Resources.getResource("META-INF/maven/org.webjars/codemirror/pom.properties"));
			Properties cmProps = new Properties();
			cmProps.load(new ByteArrayInputStream(bytes));
			String cmResPrefix = "META-INF/resources/webjars/codemirror/" + cmProps.getProperty("version") + "/";

	        ScriptEngineManager factory = new ScriptEngineManager();
			ScriptEngine engine = factory.getEngineByName("JavaScript");
			
			Charset utf8 = Charset.forName("UTF-8");
			String script = Resources.toString(Resources.getResource(cmResPrefix + "addon/runmode/runmode-standalone.js"), utf8);
			script = StringUtils.replace(script, "window.CodeMirror", "CodeMirror");
			engine.eval(script);
			
			engine.eval(Resources.toString(Resources.getResource(cmResPrefix + "mode/meta.js"), utf8));
			
			for (String mode: loadModes) 
				engine.eval(Resources.toString(Resources.getResource(cmResPrefix + "mode/" + mode), utf8));
			
			String fileContent = Resources.toString(Resources.getResource(getClass(), fileName), utf8);
			if (fileName.contains("/"))
				fileName = StringUtils.substringAfterLast(fileName, "/");
			if (fileName.endsWith(".txt"))
				fileName = fileName.substring(0, fileName.length()-4); 
			engine.put("fileName", fileName);
			engine.put("fileContent", fileContent);

			Callback callback = new Callback();
			engine.put("callback", callback);
			
	        engine.eval("var mime = CodeMirror.findModeByFileName(fileName).mime;"
	        		+ "CodeMirror.runMode(fileContent, mime, function(text, style) {callback.token(style, text);});");
	        
	        List<String> lines = new ArrayList<>();
	        for (String line: Splitter.on('\n').splitToList(fileContent))
	        	lines.add(StringUtils.stripEnd(line, "\r"));

	        String expected = toString(callback.getTokenizedLines());
	        String actual = toString(tokenizer.tokenize(lines));
	        
	        Assert.assertEquals(expected, actual);
	    } catch (IOException | ScriptException e) {
			throw new RuntimeException(e);
		} 
	}
	
	public static class Callback {

		private List<List<CmToken>> tokenizedLines = new ArrayList<>();
	
		private List<CmToken> currentLine = new ArrayList<>();
		
		public void token(String style, String text) {
			if (style == null || style.equals("null") || style.equals("undefined"))
				style = "";
			if (text.equals("\n")) {
				tokenizedLines.add(currentLine);
				currentLine = new ArrayList<>();
			} else {
				CmToken token = new CmToken(style, text);
				List<CmToken> splitted = token.split();
				if (splitted != null)
					currentLine.addAll(splitted);
				else
					currentLine.add(token);
			}
		}
		
		public List<List<CmToken>> getTokenizedLines() {
			List<List<CmToken>> copy = new ArrayList<>(tokenizedLines);
			if (!currentLine.isEmpty())
				copy.add(currentLine);
			return copy;
		}
	}
	
	private String toString(List<List<CmToken>> lines) {
		StringBuilder builder = new StringBuilder();
        for (int i=0; i<lines.size(); i++) {
        	for (CmToken token: lines.get(i)) 
        		builder.append(token.toString());
    		builder.append("\n");
        }
		return StringUtils.stripEnd(builder.toString(), "\n");
	}
}
