package com.pmease.commons.lang.tokenizers;

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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.io.Resources;
import com.pmease.commons.lang.Token;
import com.pmease.commons.lang.TokenizedLine;
import com.pmease.commons.lang.Tokenizer;

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
	        
	        List<String> lines = Splitter.on(new CharMatcher() {
				
				@Override
				public boolean matches(char c) {
					return c == '\r' || c == '\n';
				}
				
			}).omitEmptyStrings().splitToList(fileContent);
	        Assert.assertEquals(callback.getTokenizedLines().toString(), tokenizer.tokenize(lines).toString());
	    } catch (IOException | ScriptException e) {
			throw new RuntimeException(e);
		} 
	}
	
	public static class Callback {

		private List<TokenizedLine> tokenizedLines = new ArrayList<>();
	
		private TokenizedLine currentLine = new TokenizedLine();
		
		public void token(String style, String text) {
			if (style == null || style.equals("null") || style.equals("undefined"))
				style = "";
			if (text.equals("\n")) {
				if (!currentLine.getTokens().isEmpty()) {
					tokenizedLines.add(currentLine);
					currentLine = new TokenizedLine();
				}
			} else {
				currentLine.getTokens().add(new Token(style, text));
			}
		}
		
		public List<TokenizedLine> getTokenizedLines() {
			List<TokenizedLine> copy = new ArrayList<>(tokenizedLines);
			if (!currentLine.getTokens().isEmpty())
				copy.add(currentLine);
			return copy;
		}
	}
}
