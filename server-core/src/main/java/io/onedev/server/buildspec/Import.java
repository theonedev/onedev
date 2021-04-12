package io.onedev.server.buildspec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable
@ClassValidating
public class Import implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private String projectName;
	
	private String tag;
	
	private transient BuildSpec buildSpec;
	
	private static ThreadLocal<Stack<String>> importChain = new ThreadLocal<Stack<String>>() {

		@Override
		protected Stack<String> initialValue() {
			return new Stack<>();
		}
		
	};
	
	// change Named("projectName") also if change name of this property 
	@Editable(order=100, name="Project", description="Specify project to import build spec from. "
			+ "Default role of this project should have <tt>read code</tt> permission")
	@ChoiceProvider("getProjectChoices")
	@NotEmpty
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		List<String> choices = new ArrayList<>();
		Project project = ((ProjectPage)WicketUtils.getPage()).getProject();
		for (Project each: OneDev.getInstance(ProjectManager.class).getPermittedProjects(new AccessProject())) {
			if (!each.equals(project))
				choices.add(each.getName());
		}
		
		Collections.sort(choices);
		
		return choices;
	}
	
	@Nullable
	private static Project getInputProject() {
		String projectName = (String) EditContext.get().getInputValue("projectName");
		if (projectName != null) {
			Project project = OneDev.getInstance(ProjectManager.class).find(projectName);
			if (project != null && SecurityUtils.canReadCode(project))
				return project;
		}
		return null;
	}
	
	@Editable(order=200, description="Specify tag in above project to import build spec from")
	@Interpolative(variableSuggester="suggestTags", literalSuggester="suggestTags")
	@NotEmpty
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestTags(String matchWith) {
		Project project = getInputProject();
		if (project != null)
			return SuggestionUtils.suggestTags(project, matchWith);
		else
			return new ArrayList<>();
	}

	private boolean canReadCode(Project project) {
		return SecurityUtils.asAnonymous().isPermitted(new ProjectPermission(project, new ReadCode()));
	}
	
	public BuildSpec getBuildSpec() {
		if (buildSpec == null) {
			Project project = getProject();
			
			if (canReadCode(project)) {
				RevCommit commit = project.getRevCommit(tag, false);
				if (commit == null) {
					String errorMessage = String.format("Unable to find tag to import build spec (project: %s, tag: %s)", 
							projectName, tag);
					throw new ExplicitException(errorMessage);
				}
				try {
					buildSpec = project.getBuildSpec(commit);
				} catch (BuildSpecParseException e) {
					String errorMessage = String.format("Malformed build spec (project: %s, tag: %s)", 
							projectName, tag);
					throw new ExplicitException(errorMessage);
				}
				if (buildSpec == null) {
					String errorMessage = String.format("Build spec not defined (project: %s, tag: %s)", 
							projectName, tag);
					throw new ExplicitException(errorMessage);
				}
			} else {
				String errorMessage = String.format("Unable to read build spec. Make sure default role of the project "
						+ "has permission to read code (project: %s, tag: %s)", projectName, tag);
				throw new ExplicitException(errorMessage);
			}
			
		}
		return buildSpec;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (importChain.get().contains(projectName)) {
			List<String> circular = new ArrayList<>(importChain.get());
			circular.add(projectName);
			String errorMessage = "Circular build spec imports (" + circular + ")";
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
			return false;
		} else {
			importChain.get().push(projectName);
			try {
				Validator validator = OneDev.getInstance(Validator.class);
				for (int i=0; i<getBuildSpec().getImports().size(); i++) {
					Import aImport = getBuildSpec().getImports().get(i);
					for (ConstraintViolation<Import> violation: validator.validate(aImport)) {
						String location = "imports[" + i + "]";
						if (violation.getPropertyPath().toString().length() != 0)
							location += "." + violation.getPropertyPath();
			    		String message = String.format("Error validating imported build spec (project: %s, location: %s, message: %s)", 
			    				projectName, location, violation.getMessage());
			    		throw new ValidationException(message);
					}
				}
				return true;
			} catch (Exception e) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
				return false;
			} finally {
				importChain.get().pop();
			}
		}
	}
	
	public Project getProject() {
		Project project = OneDev.getInstance(ProjectManager.class).find(projectName);
		if (project == null) 
			throw new ExplicitException("Unable to find project to import build spec (" + projectName + ")");
		else 
			return project;
	}
	
}
