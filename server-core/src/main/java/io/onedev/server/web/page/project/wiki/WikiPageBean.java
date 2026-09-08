package io.onedev.server.web.page.project.wiki;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.Path;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.HierarchicalContext;
import io.onedev.server.validation.Validatable;

@Editable
@ClassValidating
public class WikiPageBean implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private String name;
	private byte[] content = new byte[0];
	private String commitMessage;

	@Editable(order=100, name="Page name")
	@NotEmpty
	@Path(Path.Type.RELATIVE)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200)
	@OmitName
	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	@Editable(order=300, name="Commit message", placeholderProvider="getDefaultCommitMessage")
	@Multiline
	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}

	@SuppressWarnings("unused")
	private static String getDefaultCommitMessage() {
		String name = (String) EditContext.get().getInputValue("name");
		return HierarchicalContext.get().findData(ProjectWikiPage.class).getDefaultPageCommitMessage(name);
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		return HierarchicalContext.get().findData(ProjectWikiPage.class).validatePage(this, context);
	}
	
}
