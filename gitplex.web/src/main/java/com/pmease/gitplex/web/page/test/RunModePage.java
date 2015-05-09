package com.pmease.gitplex.web.page.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import com.pmease.commons.wicket.CommonPage;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

@SuppressWarnings("serial")
public class RunModePage extends CommonPage {

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/runmode/runmode-standalone.js")));
		response.render(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/mode/meta.js")));
		response.render(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/mode/javascript/javascript.js")));
		response.render(CssHeaderItem.forReference(
				new WebjarsCssResourceReference("codemirror/current/lib/codemirror.css")));

		File file = new File("W:\\commons\\commons.tokenizer\\src\\test\\java\\com\\pmease\\commons\\tokenizer\\testfiles\\test.json");
		
		try {
			response.render(OnDomReadyHeaderItem.forScript(String.format(
					"var mime = CodeMirror.findModeByFileName('%s').mime;"
					+ "CodeMirror.runMode('%s', mime, document.getElementById('code'));", 
					file.getName(), StringEscapeUtils.escapeEcmaScript(FileUtils.readFileToString(file)))));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
