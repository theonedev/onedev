package io.onedev.server.buildspec;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.job.JobAuthorizationContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.validation.Validatable;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

@Editable
@ClassValidating
public class Import implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	private String revision;
	
	private String accessTokenSecret;
	
	private transient Project project;
	
	private transient RevCommit commit;
	
	private transient BuildSpec buildSpec;
	
	private static ThreadLocal<Stack<String>> importChain = ThreadLocal.withInitial(Stack::new);
	
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

		List<String> choices = projects.stream().map(it->cache.get(it.getId()).getPath()).collect(Collectors.toList());
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
	
	@Editable(order=200, description="Specify branch, tag or commit in above project to import " +
			"build spec from")
	@Interpolative(variableSuggester="suggestVariables", literalSuggester="suggestRevisions")
	@NotEmpty
	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestRevisions(String matchWith) {
		Project project = getInputProject();
		if (project != null)
			return SuggestionUtils.suggestRevisions(project, matchWith);
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
				JobAuthorizationContext context = Preconditions.checkNotNull(JobAuthorizationContext.get());
				String accessToken = context.getSecretValue(accessTokenSecret);
				User user = OneDev.getInstance(UserManager.class).findByAccessToken(accessToken);
				if (user == null) {
					throw new ExplicitException(String.format(
							"Unable to import build spec as access token is invalid (import project: %s, import revision: %s)", 
							projectPath, revision));
				}
				subject = user.asSubject();
			} else {
				subject = SecurityUtils.asSubject(0L);
			}
			if (!subject.isPermitted(new ProjectPermission(project, new ReadCode())) 
					&& !project.isPermittedByLoginUser(new ReadCode())) {
				String errorMessage = String.format(
						"Code read permission is required to import build spec (import project: %s, import revision: %s)", 
						projectPath, revision);
				throw new ExplicitException(errorMessage);
			}
			
			RevCommit commit = getCommit();
			try {
				buildSpec = project.getBuildSpec(commit);
			} catch (BuildSpecParseException e) {
				String errorMessage = String.format("Malformed build spec (import project: %s, import revision: %s)", 
						projectPath, revision);
				throw new ExplicitException(errorMessage);
			}
			if (buildSpec == null) {
				String errorMessage = String.format("Build spec not defined (import project: %s, import revision: %s)", 
						projectPath, revision);
				throw new ExplicitException(errorMessage);
			}
			
		}
		return buildSpec;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		try {
			var commit = getCommit();
			if (importChain.get().contains(commit.name())) {
				List<String> circular = new ArrayList<>(importChain.get());
				circular.add(commit.name());
				String errorMessage = "Circular build spec imports (" + circular + ")";
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
				return false;
			} else {
				importChain.get().push(commit.name());
				try {
					Validator validator = OneDev.getInstance(Validator.class);
					BuildSpec buildSpec = getBuildSpec();
					JobAuthorizationContext.push(new JobAuthorizationContext(
							getProject(), getCommit(), SecurityUtils.getUser(), null));
					try {
						for (int i = 0; i < buildSpec.getImports().size(); i++) {
							Import aImport = buildSpec.getImports().get(i);
							for (ConstraintViolation<Import> violation : validator.validate(aImport)) {
								String location = "imports[" + i + "]";
								if (violation.getPropertyPath().toString().length() != 0)
									location += "." + violation.getPropertyPath();
								String message = String.format("Error validating imported build spec (import project: %s, import revision: %s, location: %s, message: %s)",
										projectPath, revision, location, violation.getMessage());
								throw new ValidationException(message);
							}
						}
						return true;
					} finally {
						JobAuthorizationContext.pop();
					}
				} finally {
					importChain.get().pop();
				}
			}
		} catch (Exception e) {
			context.disableDefaultConstraintViolation();
			String message = e.getMessage();
			if (message == null)
				message = Throwables.getStackTraceAsString(e);
			context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
	}
	
	public Project getProject() {
		if (project == null) {
			project = OneDev.getInstance(ProjectManager.class).findByPath(projectPath);
			if (project == null)
				throw new ExplicitException("Unable to find project to import build spec: " + projectPath);
		}
		return project;
	}
	
	public RevCommit getCommit() {
		if (commit == null) {
			commit = getProject().getRevCommit(revision, false);
			if (commit == null) {
				String errorMessage = String.format("Unable to find commit to import build spec (import project: %s, import revision: %s)",
						projectPath, revision);
				throw new ExplicitException(errorMessage);
			}
		}
		return commit;
	}
	
}
