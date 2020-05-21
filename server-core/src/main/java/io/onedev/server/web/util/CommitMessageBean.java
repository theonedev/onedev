package io.onedev.server.web.util;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ReferenceAware;

@Editable
public class CommitMessageBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String summary;
	
	private String body;

	@Editable(order=100, name="Commit Message Summary")
	@NotEmpty
	@OmitName
	@ReferenceAware
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Editable(order=200, name="Commit Message Body")
	@Multiline
	@OmitName
	@ReferenceAware
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
}
