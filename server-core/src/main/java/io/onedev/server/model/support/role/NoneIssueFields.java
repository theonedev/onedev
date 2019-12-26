package io.onedev.server.model.support.role;

import java.util.Collection;

import com.google.common.collect.Sets;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=400, name="None")
public class NoneIssueFields implements IssueFieldSet {

	private static final long serialVersionUID = 1L;

	@Override
	public Collection<String> getIncludeFields() {
		return Sets.newHashSet();
	}

}
