package io.onedev.server.model.support.role;

import java.util.Collection;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=100, name="All")
public class AllIssueFields implements IssueFieldSet {

	private static final long serialVersionUID = 1L;

	@Override
	public Collection<String> getIncludeFields() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldNames();
	}

}
