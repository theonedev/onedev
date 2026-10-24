package io.onedev.server.buildspec.job.projectdependency;

import java.io.Serial;

import org.eclipse.jgit.lib.Constants;

import io.onedev.server.annotation.Editable;
import jakarta.validation.constraints.NotEmpty;

@Editable(order=200, name="Tag")
public class TagProvider implements RefNameProvider {

	@Serial
	private static final long serialVersionUID = 1L;

	private String tagPattern;

	@Editable(description="Specify tag. Use * for wildcard match")
	@NotEmpty
	public String getTagPattern() {
		return tagPattern;
	}

	public void setTagPattern(String tagPattern) {
		this.tagPattern = tagPattern;
	}

	@Override
	public String getRefName() {
		return Constants.R_TAGS + tagPattern;
	}

}
