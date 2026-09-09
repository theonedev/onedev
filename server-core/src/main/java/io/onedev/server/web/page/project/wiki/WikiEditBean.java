package io.onedev.server.web.page.project.wiki;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.Path;
import io.onedev.server.annotation.Wiki;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.HierarchicalContext;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.web.util.WikiUtils;

@Editable
@ClassValidating
public class WikiEditBean implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String content;

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
	@NotEmpty 
	@Wiki
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
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
		if (name != null) 
			name = name.replace(' ', '-');

		var page = (ProjectWikiPage)WicketUtils.getPage();

		var project = page.getProject();
		var commitId = page.getCommitId();
		var revision = page.getRevision();
		boolean valid = true;
		if ((page.isCreating() || !name.equals(page.getPageName())) && commitId != null
				&& project.getBlob(new BlobIdent(commitId.name(), WikiUtils.pagePath(project.getWikiFolder().getPath(), name)), false) != null) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(_T("A page with this name already exists."))
					.addPropertyNode("name").addConstraintViolation();
			valid = false;
		}
		String message = getCommitMessage();
		if (message == null)
			message = page.getDefaultPageCommitMessage(name);
		String error = project.getBranchProtection(revision != null ? revision : "main", SecurityUtils.getAuthUser())
				.checkCommitMessage(message, false);
		if (error != null) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(error).addPropertyNode("commitMessage").addConstraintViolation();
			valid = false;
		}
		return valid;
	}
	
}
