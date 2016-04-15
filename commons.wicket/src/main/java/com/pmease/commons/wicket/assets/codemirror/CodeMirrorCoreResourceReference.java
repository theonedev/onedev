package com.pmease.commons.wicket.assets.codemirror;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.util.StringUtils;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class CodeMirrorCoreResourceReference extends WebjarsJavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final CodeMirrorCoreResourceReference INSTANCE = new CodeMirrorCoreResourceReference();
	
	private CodeMirrorCoreResourceReference() {
		super("codemirror/current/mode/meta.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		String modeBase = StringUtils.substringBeforeLast(RequestCycle.get().urlFor(this, new PageParameters()).toString(), "/");
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/lib/codemirror.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/mode/overlay.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/mode/loadmode.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/edit/matchbrackets.js")));
		dependencies.add(CssHeaderItem.forReference(
				new WebjarsCssResourceReference("codemirror/current/lib/codemirror.css")));
		dependencies.add(CssHeaderItem.forReference(
				new WebjarsCssResourceReference("codemirror/current/theme/eclipse.css")));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(CodeMirrorCoreResourceReference.class, "codemirror.css")));
		dependencies.add(OnDomReadyHeaderItem.forScript("CodeMirror.modeURL = '" + modeBase + "/%N/%N.js';"));		
		return dependencies;
	}
}
