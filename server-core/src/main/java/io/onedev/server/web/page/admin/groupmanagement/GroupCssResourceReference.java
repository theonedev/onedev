package io.onedev.server.web.page.admin.groupmanagement;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class GroupCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public GroupCssResourceReference() {
		super(GroupCssResourceReference.class, "group.css");
	}

}
