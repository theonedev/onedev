package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.JobChoice;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
@ClassValidating
public class FileProtection implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private String paths;
	
	private String reviewRequirement;
	
	private transient ReviewRequirement parsedReviewRequirement;
	
	private List<String> jobNames = new ArrayList<>();
	
	@Editable(order=100, description="Specify space-separated paths to be protected. Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude")
	@Patterns(suggester = "suggestPaths", path=true)
	@NotEmpty
	public String getPaths() {
		return paths;
	}

	public void setPaths(String paths) {
		this.paths = paths;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestPaths(String matchWith) {
		if (Project.get() != null)
			return SuggestionUtils.suggestBlobs(Project.get(), matchWith);
		else
			return new ArrayList<>();
	}

	@Editable(order=200, name="Reviewers", description="Specify required reviewers if specified path is "
			+ "changed. Note that the user submitting the change is considered to reviewed the change automatically")
	@io.onedev.server.web.editable.annotation.ReviewRequirement
	public String getReviewRequirement() {
		return reviewRequirement;
	}

	public void setReviewRequirement(String reviewRequirement) {
		this.reviewRequirement = reviewRequirement;
	}
	
	public ReviewRequirement getParsedReviewRequirement() {
		if (parsedReviewRequirement == null)
			parsedReviewRequirement = ReviewRequirement.parse(reviewRequirement, true);
		return parsedReviewRequirement;
	}
	
	public void setParsedReviewRequirement(ReviewRequirement parsedReviewRequirement) {
		this.parsedReviewRequirement = parsedReviewRequirement;
		reviewRequirement = parsedReviewRequirement.toString();
	}
	
	@Editable(order=500, name="Required Builds", description="Optionally choose required builds")
	@JobChoice
	@NameOfEmptyValue("No any")
	public List<String> getJobNames() {
		return jobNames;
	}

	public void setJobNames(List<String> jobNames) {
		this.jobNames = jobNames;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (getJobNames().isEmpty() && getReviewRequirement() == null) {
			context.disableDefaultConstraintViolation();
			String message = "Either reviewer or required builds should be specified";
			context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
