package io.onedev.server.buildspec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.JobSecretAuthorizationContext;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable
@ClassValidating
public class Import implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	private String tag;
	
	private String accessTokenSecret;
	
	private transient BuildSpec buildSpec;
	
	private static ThreadLocal<Stack<String>> importChain = new ThreadLocal<Stack<String>>() {

		@Override
		protected Stack<String> initialValue() {
			return new Stack<>();
		}
		
	};
	
	// change Named("projectPath") also if change name of this property 
	@Editable(order=100, name="Project", description="Specify project to import build spec from. "
			+ "Default role of this project should have <tt>read code</tt> permission")
	@ChoiceProvider("getProjectChoices")
	@NotEmpty
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		Project project = ((ProjectPage)WicketUtils.getPage()).getProject();
		
		Collection<Project> projects = projectManager.getPermittedProjects(new AccessProject());
		projects.remove(project);
		
		ProjectCache cache = projectManager.cloneCache();

		List<String> choices = projects.stream().map(it->cache.getPath(it.getId())).collect(Collectors.toList());
		Collections.sort(choices);
		
		return choices;
	}
	
	@Nullable
	private static Project getInputProject() {
		String projectPath = (String) EditContext.get().getInputValue("projectPath");
		if (projectPath != null) {
			Project project = OneDev.getInstance(ProjectManager.class).findByPath(projectPath);
			if (project != null && SecurityUtils.canReadCode(project))
				return project;
		}
		return null;
	}
	
	@Editable(order=200, description="Specify tag in above project to import build spec from")
	@Interpolative(variableSuggester="suggestVariables", literalSuggester="suggestTags")
	@NotEmpty
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false);
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestTags(String matchWith) {
		Project project = getInputProject();
		if (project != null)
			return SuggestionUtils.suggestTags(project, matchWith);
		else
			return new ArrayList<>();
	}

	@Editable(order=500, placeholder="Access Anonymously", description="Specify a secret to be used as "
			+ "access token to import build spec from above project. If not specified, OneDev will try "
			+ "to import build spec anonymously")
	@ChoiceProvider("getAccessTokenSecretChoices")
	@Nullable
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getAccessTokenSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
	public BuildSpec getBuildSpec() {
		if (buildSpec == null) {
			Project project = getProject();

			Subject subject;
			if (getAccessTokenSecret() != null) {
				JobSecretAuthorizationContext context = Preconditions.checkNotNull(JobSecretAuthorizationContext.get());
				String accessToken;
				
				if (WicketUtils.getPage() instanceof ProjectBlobPage) {
					ProjectBlobPage page = (ProjectBlobPage) WicketUtils.getPage();
					if (context.getProject().equals(page.getProject()) 
							&& (page.getMode() == Mode.ADD || page.getMode() == Mode.EDIT)) {
						String branchName = page.getBlobIdent().revision;
						if (branchName == null)
							branchName = "master";
						accessToken = context.getSecretValue(branchName, accessTokenSecret);
					} else {
						accessToken = context.getSecretValue(accessTokenSecret);
					}
				} else {
					accessToken = context.getSecretValue(accessTokenSecret);
				}
				User user = OneDev.getInstance(UserManager.class).findByAccessToken(accessToken);
				if (user == null) {
					throw new ExplicitException(String.format(
							"Unable to import build spec as access token is invalid (import project: %s)", 
							projectPath));
				}
				subject = user.asSubject();
			} else {
				subject = SecurityUtils.asSubject(0L);
			}
			if (!subject.isPermitted(new ProjectPermission(project, new ReadCode())) 
					&& !project.isPermittedByLoginUser(new ReadCode())) {
				String errorMessage = String.format(
						"Code read permission is required to import build spec (import project: %s)", 
						projectPath);
				throw new ExplicitException(errorMessage);
			}
			
			RevCommit commit = getCommit();
			try {
				buildSpec = project.getBuildSpec(commit);
			} catch (BuildSpecParseException e) {
				String errorMessage = String.format("Malformed build spec (import project: %s, tag: %s)", 
						projectPath, tag);
				throw new ExplicitException(errorMessage);
			}
			if (buildSpec == null) {
				String errorMessage = String.format("Build spec not defined (import project: %s, tag: %s)", 
						projectPath, tag);
				throw new ExplicitException(errorMessage);
			}
			
		}
		return buildSpec;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (importChain.get().contains(projectPath)) {
			List<String> circular = new ArrayList<>(importChain.get());
			circular.add(projectPath);
			String errorMessage = "Circular build spec imports (" + circular + ")";
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
			return false;
		} else {
			importChain.get().push(projectPath);
			try {
				Validator validator = OneDev.getInstance(Validator.class);
				BuildSpec buildSpec = getBuildSpec();
				JobSecretAuthorizationContext.push(new JobSecretAuthorizationContext(getProject(), getCommit(), null));
				try {
					for (int i=0; i<buildSpec.getImports().size(); i++) {
						Import aImport = buildSpec.getImports().get(i);
						for (ConstraintViolation<Import> violation: validator.validate(aImport)) {
							String location = "imports[" + i + "]";
							if (violation.getPropertyPath().toString().length() != 0)
								location += "." + violation.getPropertyPath();
				    		String message = String.format("Error validating imported build spec (import project: %s, location: %s, message: %s)", 
				    				projectPath, location, violation.getMessage());
				    		throw new ValidationException(message);
						}
					}
					return true;
				} finally {
					JobSecretAuthorizationContext.pop();
				}
			} catch (Exception e) {
				context.disableDefaultConstraintViolation();
				String message = e.getMessage();
				if (message == null) 
					message = Throwables.getStackTraceAsString(e);
				context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			} finally {
				importChain.get().pop();
			}
		}
	}
	
	public Project getProject() {
		Project project = OneDev.getInstance(ProjectManager.class).findByPath(projectPath);
		if (project == null) 
			throw new ExplicitException("Unable to find project to import build spec: " + projectPath);
		else 
			return project;
	}
	
	public RevCommit getCommit() {
		RevCommit commit = getProject().getRevCommit(tag, false);
		if (commit == null) {
			String errorMessage = String.format("Unable to find tag to import build spec (import project: %s, tag: %s)", 
					projectPath, tag);
			throw new ExplicitException(errorMessage);
		}
		return commit;
	}
	
}
