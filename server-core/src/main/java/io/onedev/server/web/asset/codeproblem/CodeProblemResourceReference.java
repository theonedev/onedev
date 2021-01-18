package io.onedev.server.web.asset.codeproblem;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class CodeProblemResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CodeProblemResourceReference() {
		super(CodeProblemResourceReference.class, "code-problem.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(
				Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				CodeProblemResourceReference.class, "code-problem.css")));
		return dependencies;
	}
	
}
