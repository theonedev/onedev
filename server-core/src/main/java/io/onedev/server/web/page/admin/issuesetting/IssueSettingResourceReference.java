package io.onedev.server.web.page.admin.issuesetting;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class IssueSettingResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public IssueSettingResourceReference() {
		super(IssueSettingResourceReference.class, "issue-setting.css");
	}

}
