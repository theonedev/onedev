package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.Permission;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.role.CodePrivilege;
import io.onedev.server.model.support.role.JobPrivilege;
import io.onedev.server.security.permission.AccessBuild;
import io.onedev.server.security.permission.AccessBuildLog;
import io.onedev.server.security.permission.AccessBuildReports;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.EditIssueField;
import io.onedev.server.security.permission.JobPermission;
import io.onedev.server.security.permission.ManageIssues;
import io.onedev.server.security.permission.ManageJob;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.security.permission.RunJob;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.SecurityUtils;
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
@Editable
public class Role extends AbstractEntity implements Permission {

	private static final long serialVersionUID = 1L;

	@Column(nullable=false, unique=true)
	private String name;
	
	private boolean manageProject;
	
	private CodePrivilege codePrivilege = CodePrivilege.NONE;
	
	private boolean manageIssues;
	
	@Lob
	@Column(length=65535, nullable=false)
	private ArrayList<String> editableIssueFields = new ArrayList<>();
	
	@Lob
	@Column(length=65535, nullable=false)
	private ArrayList<JobPrivilege> jobPrivileges = new ArrayList<>();
	
	@OneToMany(mappedBy="role", cascade=CascadeType.REMOVE)
	private Collection<UserAuthorization> userAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="role", cascade=CascadeType.REMOVE)
	private Collection<GroupAuthorization> groupAuthorizations = new ArrayList<>();
	
	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="This permission is required to edit project settings. "
			+ "It implies all other project permissions")
	public boolean isManageProject() {
		return manageProject;
	}

	public void setManageProject(boolean manageProject) {
		this.manageProject = manageProject;
	}
	
	@SuppressWarnings("unused")
	private static boolean isManageProjectDisabled() {
		return !(boolean)EditContext.get().getInputValue("manageProject");
	}

	@Editable(order=300)
	@ShowCondition("isManageProjectDisabled")
	@NotNull
	public CodePrivilege getCodePrivilege() {
		return codePrivilege;
	}

	public void setCodePrivilege(CodePrivilege codePrivilege) {
		this.codePrivilege = codePrivilege;
	}

	@Editable(order=400, description="This permission is required to manage issue milestones and boards. "
			+ "It implies all other issue permissions")
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
	
	@Editable(order=600, description="Optionally specify custom fields allowed to edit when open new issues")
	@ChoiceProvider("getIssueFieldChoices")
	public List<String> getEditableIssueFields() {
		return editableIssueFields;
	}

	public void setEditableIssueFields(List<String> editableIssueFields) {
		this.editableIssueFields = (ArrayList<String>)editableIssueFields;
	}

	@SuppressWarnings("unused")
	private static List<String> getIssueFieldChoices() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldNames();
	}
	
	@Editable(order=700)
	@ShowCondition("isManageProjectDisabled")
	public List<JobPrivilege> getJobPrivileges() {
		return jobPrivileges;
	}

	public void setJobPrivileges(List<JobPrivilege> jobPrivileges) {
		this.jobPrivileges = (ArrayList<JobPrivilege>) jobPrivileges;
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

	@Override
	public boolean implies(Permission p) {
		for (Permission each: getPermissions()) {
			if (each.implies(p))
				return true;
		}
		return false;
	}
	
	private Collection<Permission> getPermissions() {
		Collection<Permission> permissions = new ArrayList<>();
		if (SecurityUtils.getUser() != null) {
			if (manageProject) {
				permissions.add(new ManageProject());
			} else {
				if (codePrivilege == CodePrivilege.READ)
					permissions.add(new ReadCode());
				else if (codePrivilege == CodePrivilege.WRITE)
					permissions.add(new WriteCode());
				
				if (manageIssues) {
					permissions.add(new ManageIssues());
				} else if (!editableIssueFields.isEmpty()) {
					for (String issueField: editableIssueFields)
						permissions.add(new EditIssueField(issueField));
				} 
				permissions.add(new AccessProject());
				
				for (JobPrivilege jobPrivilege: jobPrivileges) {
					if (jobPrivilege.isManageJob()) {
						permissions.add(new JobPermission(jobPrivilege.getJobNames(), new ManageJob()));
					} else if (jobPrivilege.isRunJob()) {
						permissions.add(new JobPermission(jobPrivilege.getJobNames(), new RunJob()));
					} else if (jobPrivilege.isAccessLog()) {
						permissions.add(new JobPermission(jobPrivilege.getJobNames(), new AccessBuildLog()));
					} else if (jobPrivilege.getAccessibleReports() != null) {
						AccessBuildReports accessBuildReports = new AccessBuildReports(jobPrivilege.getAccessibleReports());
						permissions.add(new JobPermission(jobPrivilege.getJobNames(), accessBuildReports));
					} else {
						permissions.add(new JobPermission(jobPrivilege.getJobNames(), new AccessBuild()));
					}
				}
			}
		} else {
			if (manageProject) {
				permissions.add(new ReadCode());
				permissions.add(new AccessProject());
				permissions.add(new JobPermission("*", new AccessBuildLog()));
			} else {
				if (codePrivilege != CodePrivilege.NONE)
					permissions.add(new ReadCode());
				
				permissions.add(new AccessProject());
				
				for (JobPrivilege jobPrivilege: jobPrivileges) {
					if (jobPrivilege.isManageJob() || jobPrivilege.isRunJob() || jobPrivilege.isAccessLog()) {
						permissions.add(new JobPermission(jobPrivilege.getJobNames(), new AccessBuildLog()));
					} else if (jobPrivilege.getAccessibleReports() != null) {
						AccessBuildReports accessBuildReports = new AccessBuildReports(jobPrivilege.getAccessibleReports());
						permissions.add(new JobPermission(jobPrivilege.getJobNames(), accessBuildReports));
					} else {
						permissions.add(new JobPermission(jobPrivilege.getJobNames(), new AccessBuild()));
					}
				}
			}			
		}
		
		return permissions;
	}
	
}