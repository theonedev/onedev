package io.onedev.server.model.support;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;

@Editable
public class ProjectAiSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private String excludedReviewFiles;	

	@Editable(order=100, name="Excluded Files for Review", placeholder="Inherit from parent", rootPlaceholder="No excluded files", description="""
		Optionally specify files to be excluded when reviewing code with AI user to save tokens""")
	@Patterns(path=true)
	@Nullable
	public String getExcludedReviewFiles() {
		return excludedReviewFiles;
	}

	public void setExcludedReviewFiles(@Nullable String excludedReviewFiles) {
		this.excludedReviewFiles = excludedReviewFiles;
	}
}
