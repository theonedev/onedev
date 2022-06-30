package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.Permission;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.role.AllIssueFields;
import io.onedev.server.model.support.role.CodePrivilege;
import io.onedev.server.model.support.role.IssueFieldSet;
import io.onedev.server.model.support.role.JobPrivilege;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessBuild;
import io.onedev.server.security.permission.AccessBuildLog;
import io.onedev.server.security.permission.AccessBuildReports;
import io.onedev.server.security.permission.AccessConfidentialIssues;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.CreateChildren;
import io.onedev.server.security.permission.EditIssueField;
import io.onedev.server.security.permission.EditIssueLink;
import io.onedev.server.security.permission.JobPermission;
import io.onedev.server.security.permission.ManageBuilds;
import io.onedev.server.security.permission.ManageCodeComments;
import io.onedev.server.security.permission.ManageIssues;
import io.onedev.server.security.permission.ManageJob;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.security.permission.ManagePullRequests;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.security.permission.RunJob;
import io.onedev.server.security.permission.ScheduleIssues;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.util.validation.annotation.RoleName;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.ShowCondition;

/**
 * @author robin
 *
 */
@Entity
@Table(indexes={@Index(columnList="name")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@ClassValidating
@Editable
public class Role extends AbstractEntity implements Permission, Validatable {

	private static final long serialVersionUID = 1L;

	public static final Long OWNER_ID = 1L;
	
	@Column(nullable=false, unique=true)
	private String name;
	
	private boolean manageProject;
	
	private boolean createChildren;
	
	private boolean managePullRequests;
	
	private boolean manageCodeComments;
	
	private CodePrivilege codePrivilege = CodePrivilege.NONE;
	
	private boolean manageIssues;
	
	private boolean accessConfidentialIssues;
	
	private boolean scheduleIssues;
	
	@Lob
	@Column(length=65535, nullable=false)
	private IssueFieldSet editableIssueFields = new AllIssueFields();
	
	@Transient
	private List<String> editableIssueLinks = new ArrayList<>();
	
	private boolean manageBuilds;
	
	@Lob
	@Column(length=65535, nullable=false)
	private ArrayList<JobPrivilege> jobPrivileges = new ArrayList<>();
	
	@OneToMany(mappedBy="defaultRole")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Project> defaultProjects = new ArrayList<>();
	
	@OneToMany(mappedBy="role", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<UserAuthorization> userAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="role", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<GroupAuthorization> groupAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy=LinkAuthorization.PROP_ROLE, cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<LinkAuthorization> linkAuthorizations = new ArrayList<>();
	
	@Editable(order=100)
	@RoleName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isOwner() {
		return getId().equals(OWNER_ID);
	}

	@Editable(order=200)
	public boolean isManageProject() {
		return manageProject;
	}

	public void setManageProject(boolean manageProject) {
		this.manageProject = manageProject;
	}
	
	@Editable(order=210, name="Create Child Projects")
	@ShowCondition("isManageProjectDisabled")
	public boolean isCreateChildren() {
		return createChildren;
	}

	public void setCreateChildren(boolean createChildren) {
		this.createChildren = createChildren;
	}

	@SuppressWarnings("unused")
	private static boolean isManageProjectDisabled() {
		return !(boolean)EditContext.get().getInputValue("manageProject");
	}

	@Editable(order=250)
	@ShowCondition("isManageProjectDisabled")
	public boolean isManagePullRequests() {
		return managePullRequests;
	}

	public void setManagePullRequests(boolean managePullRequests) {
		this.managePullRequests = managePullRequests;
	}
	
	@Editable(order=260)
	@ShowCondition("isManageProjectDisabled")
	public boolean isManageCodeComments() {
		return manageCodeComments;
	}

	public void setManageCodeComments(boolean manageCodeComments) {
		this.manageCodeComments = manageCodeComments;
	}

	@Editable(order=300)
	@ShowCondition("isCodePrivilegeVisible")
	@NotNull
	public CodePrivilege getCodePrivilege() {
		return codePrivilege;
	}

	public void setCodePrivilege(CodePrivilege codePrivilege) {
		this.codePrivilege = codePrivilege;
	}

	@SuppressWarnings("unused")
	private static boolean isCodePrivilegeVisible() {
		return !(boolean)EditContext.get().getInputValue("managePullRequests")
				&& !(boolean)EditContext.get().getInputValue("manageCodeComments");
	}
	
	@Editable(order=400)
	@ShowCondition("isManageProjectDisabled")
	public boolean isManageIssues() {
		return manageIssues;
	}

	public void setManageIssues(boolean manageIssues) {
		this.manageIssues = manageIssues;
	}
	
	@SuppressWarnings("unused")
	private static boolean isManageIssuesDisabled() {
		return !(boolean)EditContext.get().getInputValue("manageIssues");
	}
	
	@Editable(order=450, description="This permission enables one to access confidential issues")
	@ShowCondition("isManageIssuesDisabled")
	public boolean isAccessConfidentialIssues() {
		return accessConfidentialIssues;
	}

	public void setAccessConfidentialIssues(boolean accessConfidentialIssues) {
		this.accessConfidentialIssues = accessConfidentialIssues;
	}
	
	@Editable(order=500, description="This permission enables one to schedule issues into milestones")
	@ShowCondition("isManageIssuesDisabled")
	public boolean isScheduleIssues() {
		return scheduleIssues;
	}

	public void setScheduleIssues(boolean scheduleIssues) {
		this.scheduleIssues = scheduleIssues;
	}

	@Editable(order=600, description="Optionally specify custom fields allowed to edit when open new issues")
	@ShowCondition("isManageIssuesDisabled")
	@NotNull
	public IssueFieldSet getEditableIssueFields() {
		return editableIssueFields;
	}

	public void setEditableIssueFields(IssueFieldSet editableIssueFields) {
		this.editableIssueFields = editableIssueFields;
	}

	@SuppressWarnings("unused")
	private static List<String> getIssueFieldChoices() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldNames();
	}
	
	@Editable(order=625, placeholder="None", description="Optionally specify issue links allowed to edit")
	@ShowCondition("isManageIssuesDisabled")
	@ChoiceProvider("getIssueLinkChoices")
	public List<String> getEditableIssueLinks() {
		return editableIssueLinks;
	}

	public void setEditableIssueLinks(List<String> editableIssueLinks) {
		this.editableIssueLinks = editableIssueLinks;
	}
	
	@SuppressWarnings("unused")
	private static Map<String, String> getIssueLinkChoices() {
		Map<String, String> choices = new LinkedHashMap<>();
		for (LinkSpec link: OneDev.getInstance(LinkSpecManager.class).queryAndSort()) {
			if (link.getOpposite() != null)
				choices.put(link.getName(), link.getName() + " - " + link.getOpposite().getName());
			else
				choices.put(link.getName(), link.getName());
		}
		return choices;
	}

	@Editable(order=650)
	@ShowCondition("isManageProjectDisabled")
	public boolean isManageBuilds() {
		return manageBuilds;
	}

	public void setManageBuilds(boolean manageBuilds) {
		this.manageBuilds = manageBuilds;
	}

	@SuppressWarnings("unused")
	private static boolean isManageBuildsDisabled() {
		return !(boolean)EditContext.get().getInputValue("manageBuilds");
	}
	
	@Editable(order=700)
	@ShowCondition("isManageBuildsDisabled")
	public List<JobPrivilege> getJobPrivileges() {
		return jobPrivileges;
	}

	public void setJobPrivileges(List<JobPrivilege> jobPrivileges) {
		this.jobPrivileges = (ArrayList<JobPrivilege>) jobPrivileges;
	}
	
	public Collection<Project> getDefaultProjects() {
		return defaultProjects;
	}

	public void setDefaultProjects(Collection<Project> defaultProjects) {
		this.defaultProjects = defaultProjects;
	}

	public Collection<UserAuthorization> getUserAuthorizations() {
		return userAuthorizations;
	}

	public void setUserAuthorizations(Collection<UserAuthorization> userAuthorizations) {
		this.userAuthorizations = userAuthorizations;
	}

	public Collection<GroupAuthorization> getGroupAuthorizations() {
		return groupAuthorizations;
	}

	public void setGroupAuthorizations(Collection<GroupAuthorization> groupAuthorizations) {
		this.groupAuthorizations = groupAuthorizations;
	}

	public Collection<LinkAuthorization> getLinkAuthorizations() {
		return linkAuthorizations;
	}

	public void setLinkAuthorizations(Collection<LinkAuthorization> linkAuthorizations) {
		this.linkAuthorizations = linkAuthorizations;
	}

	@Override
	public boolean implies(Permission p) {
		for (Permission each: getPermissions()) {
			if (each.implies(p))
				return true;
		}
		return false;
	}
	
	private Collection<Permission> getPermissions() {
		Collection<Permission> permissions = Lists.newArrayList(new AccessProject());
		
		if (SecurityUtils.getUser() != null) {
			if (manageProject) 
				permissions.add(new ManageProject());
			if (createChildren)
				permissions.add(new CreateChildren());
			if (manageCodeComments)
				permissions.add(new ManageCodeComments());
			if (managePullRequests)
				permissions.add(new ManagePullRequests());
			if (codePrivilege == CodePrivilege.READ)
				permissions.add(new ReadCode());
			if (codePrivilege == CodePrivilege.WRITE)
				permissions.add(new WriteCode());
			if (manageIssues) 
				permissions.add(new ManageIssues());
			if (accessConfidentialIssues)
				permissions.add(new AccessConfidentialIssues());
			if (scheduleIssues)
				permissions.add(new ScheduleIssues());
			permissions.add(new EditIssueField(editableIssueFields.getIncludeFields()));
			for (LinkAuthorization linkAuthorization: getLinkAuthorizations()) 
				permissions.add(new EditIssueLink(linkAuthorization.getLink()));
			if (manageBuilds)
				permissions.add(new ManageBuilds());
			for (JobPrivilege jobPrivilege: jobPrivileges) {
				permissions.add(new JobPermission(jobPrivilege.getJobNames(), new AccessBuild()));
				if (jobPrivilege.isManageJob()) 
					permissions.add(new JobPermission(jobPrivilege.getJobNames(), new ManageJob()));
				if (jobPrivilege.isRunJob()) 
					permissions.add(new JobPermission(jobPrivilege.getJobNames(), new RunJob()));
				if (jobPrivilege.isAccessLog())
					permissions.add(new JobPermission(jobPrivilege.getJobNames(), new AccessBuildLog()));
				if (jobPrivilege.getAccessibleReports() != null) { 
					AccessBuildReports accessBuildReports = new AccessBuildReports(jobPrivilege.getAccessibleReports());
					permissions.add(new JobPermission(jobPrivilege.getJobNames(), accessBuildReports));
				}
			}
		} else {
			if (manageProject || managePullRequests || manageCodeComments || codePrivilege != CodePrivilege.NONE)
				permissions.add(new ReadCode());
			if (manageProject || manageBuilds)
				permissions.add(new JobPermission("*", new AccessBuildLog()));
			for (JobPrivilege jobPrivilege: jobPrivileges) {
				permissions.add(new JobPermission(jobPrivilege.getJobNames(), new AccessBuild()));
				if (jobPrivilege.isManageJob() || jobPrivilege.isRunJob() || jobPrivilege.isAccessLog()) 
					permissions.add(new JobPermission(jobPrivilege.getJobNames(), new AccessBuildLog()));
				if (jobPrivilege.getAccessibleReports() != null) { 
					AccessBuildReports accessBuildReports = new AccessBuildReports(jobPrivilege.getAccessibleReports());
					permissions.add(new JobPermission(jobPrivilege.getJobNames(), accessBuildReports));
				}
			}
		}
		
		return permissions;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		if (!isManageProject() && !isManagePullRequests() && !isManageCodeComments() && getCodePrivilege() == CodePrivilege.NONE) {
			if (isManageBuilds()) {
				isValid = false;
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Code read privilege is required to manage builds")
						.addPropertyNode("manageBuilds").addConstraintViolation();
			} else {
				for (JobPrivilege privilege: getJobPrivileges()) {
					if (privilege.isManageJob()) {
						isValid = false;
						context.disableDefaultConstraintViolation();
						context.buildConstraintViolationWithTemplate("Code read privilege is required to manage jobs")
								.addPropertyNode("jobPrivileges").addConstraintViolation();
					} else if (privilege.isRunJob()) {
						isValid = false;
						context.disableDefaultConstraintViolation();
						context.buildConstraintViolationWithTemplate("Code read privilege is required to run jobs")
								.addPropertyNode("jobPrivileges").addConstraintViolation();
					}
				}
			}
		}
		return isValid;
	}
	
}