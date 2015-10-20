package com.pmease.gitplex.web.js;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

import com.google.common.io.Resources;
import com.pmease.gitplex.web.assets.commitlane.CommitLaneResourceReference;

public class CommitLaneTest {

	@Test
	public void test() {
		try {
			Charset utf8 = Charset.forName("UTF-8");
			String script = Resources.toString(Resources.getResource(
					CommitLaneResourceReference.class, "commit-lane.js"), utf8);

	        ScriptEngineManager factory = new ScriptEngineManager();
			ScriptEngine engine = factory.getEngineByName("JavaScript");
			engine.eval("var gitplex={};");
			engine.eval(script);
			
			engine.put("commits", "[]");

			List<Object> result = (List<Object>) engine.eval(""
					+ "importPackage(java.util);"
					+ "Arrays.asList(gitplex.commitlane(commits));");
	    } catch (IOException | ScriptException e) {
			throw new RuntimeException(e);
		} 
	}

}
