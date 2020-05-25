package io.onedev.server.web.page.project.tags;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.component.createtag.TagBean;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.RevisionPick;

@Editable
public class TagBeanWithRevision extends TagBean {

	private static final long serialVersionUID = 1L;

	private String revision;

	@Editable(order=1000, name="Revision")
	@RevisionPick
	@NotEmpty(message = "Please select revision to create tag from")
	@OmitName
	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}
	
}
