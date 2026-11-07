package io.onedev.server.web.component.codecomment;

import java.io.Serial;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class CodeCommentResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public CodeCommentResourceReference() {
		super(CodeCommentResourceReference.class, "code-comment.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(CodeCommentResourceReference.class, "code-comment.css")));
		return dependencies;
	}

}
