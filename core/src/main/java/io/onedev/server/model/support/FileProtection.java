package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class FileProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private String paths;
	
	private String reviewRequirement;
	
	@Editable(order=100, description="Specify space-separated paths to be protected. Use * or ? for wildcard match")
	@Patterns("getPathSuggestions")
	@NotEmpty
	public String getPaths() {
		return paths;
	}

	public void setPaths(String paths) {
		this.paths = paths;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> getPathSuggestions(String matchWith) {
		return SuggestionUtils.suggestBlobs(OneContext.get().getProject(), matchWith);
	}

	@Editable(order=200, name="Reviewers", description="Specify required reviewers if specified path is "
			+ "changed. Note that the user submitting the change is considered to reviewed the change automatically")
	@io.onedev.server.web.editable.annotation.ReviewRequirement
	@NotEmpty
	public String getReviewRequirement() {
		return reviewRequirement;
	}

	public void setReviewRequirement(String reviewRequirement) {
		this.reviewRequirement = reviewRequirement;
	}
	
}
