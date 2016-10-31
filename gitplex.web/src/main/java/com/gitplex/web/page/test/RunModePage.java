package com.gitplex.web.page.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import com.gitplex.commons.wicket.page.CommonPage;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

@SuppressWarnings("serial")
public class RunModePage extends CommonPage {

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		String mode = "clike/clike.js";
		String fileName = "clike/test.scala";
		
		response.render(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/runmode/runmode-standalone.js")));
		response.render(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/mode/meta.js")));
		response.render(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/mode/" + mode)));
		response.render(CssHeaderItem.forReference(
				new WebjarsCssResourceReference("codemirror/current/lib/codemirror.css")));

		try {
			File file = new File("W:/gitplex.commons/commons.lang/src/test/java/com/gitplex/commons/lang/tokenizers/" + fileName);
			String fileContent = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
			response.render(OnDomReadyHeaderItem.forScript(String.format(
					"var mime = CodeMirror.findModeByFileName('%s').mime;"
					+ "CodeMirror.runMode('%s', mime, document.getElementById('code'));", 
					fileName, StringEscapeUtils.escapeEcmaScript(fileContent))));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
