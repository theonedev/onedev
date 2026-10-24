package io.onedev.server.web.page.admin.issuesetting.issuetemplate;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class IssueTemplateCssResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public IssueTemplateCssResourceReference() {
		super(IssueTemplateCssResourceReference.class, "issue-template.css");
	}

}
