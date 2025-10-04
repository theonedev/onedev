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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.Permission;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.RoleName;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.support.role.AllIssueFields;
import io.onedev.server.model.support.role.CodePrivilege;
import io.onedev.server.model.support.role.IssueFieldSet;
import io.onedev.server.model.support.role.JobPrivilege;
import io.onedev.server.model.support.role.PackPrivilege;
import io.onedev.server.security.permission.AccessBuild;
import io.onedev.server.security.permission.AccessBuildLog;
import io.onedev.server.security.permission.AccessBuildPipeline;
import io.onedev.server.security.permission.AccessBuildReports;
import io.onedev.server.security.permission.AccessConfidentialIssues;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.AccessTimeTracking;
import io.onedev.server.security.permission.BasePermission;
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
import io.onedev.server.security.permission.ReadPack;
import io.onedev.server.security.permission.RunJob;
import io.onedev.server.security.permission.ScheduleIssues;
import io.onedev.server.security.permission.UploadCache;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.security.permission.WritePack;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.facade.RoleFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.util.WicketUtils;

/**
 * @author robin
 *
 */
@Entity
@Table(indexes={@Index(columnList="name")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Role extends AbstractEntity implements BasePermission {

	private static final long serialVersionUID = 1L;

	public static final String PROP_NAME = "name";
	
	public static final Long OWNER_ID = 1L;
	
	@Column(nullable=false, unique=true)
	private String name;

	private String description;
	
	private boolean manageProject;
	
	private boolean createChildren;
	
	private boolean managePullRequests;
	
	private boolean manageCodeComments;
	
	private CodePrivilege codePrivilege = CodePrivilege.NONE;
	
	private PackPrivilege packPrivilege = PackPrivilege.NONE;
	
	private boolean manageIssues;
	
	private boolean accessConfidentialIssues;

	private boolean accessTimeTracking = true;
	
	private boolean scheduleIssues;
	
	@Lob
	@Column(length=65535, nullable=false)
	private IssueFieldSet editableIssueFields = new AllIssueFields();
	
	@Transient
	private List<String> editableIssueLinks = new ArrayList<>();
	
	private boolean manageBuilds;
	
	private boolean uploadCache;
	
	@Lob
	@Column(length=65535, nullable=false)
	private ArrayList<JobPrivilege> jobPrivileges = new ArrayList<>();
	
	@OneToMany(mappedBy="role", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<BaseAuthorization> baseAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="role", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<UserAuthorization> userAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="role", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<GroupAuthorization> groupAuthorizations = new ArrayList<>();

	@OneToMany(mappedBy="role", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<AccessTokenAuthorization> accessTokenAuthorizations = new ArrayList<>();

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
	
	@Editable(order=110)
	@Multiline
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isOwner() {
		return getId().equals(OWNER_ID);
	}

	@Editable(order=200, name="Project Management", description="Administrative permission over a project")
	public boolean isManageProject() {
		return manageProject;
	}

	public void setManageProject(boolean manageProject) {
		this.manageProject = manageProject;
	}
	
	@Editable(order=210, name="Create Child Projects", description="Create child projects under a project")
	@DependsOn(property="manageProject", value="false")
	public boolean isCreateChildren() {
		return createChildren;
	}

	public void setCreateChildren(boolean createChildren) {
		this.createChildren = createChildren;
	}

	@Editable(order=250, name="Pull Request Management", description="Pull request administrative permission inside a project, "
			+ "including batch operations over multiple pull requests")
	@DependsOn(property="manageProject", value="false")
	public boolean isManagePullRequests() {
		return managePullRequests;
	}

	public void setManagePullRequests(boolean managePullRequests) {
		this.managePullRequests = managePullRequests;
	}
	
	@Editable(order=260, name="Code Comment Management", description="Code comment administrative permission inside a project, "
			+ "including batch operations over multiple code comments")
	@DependsOn(property="manageProject", value="false")
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

	@Editable(order=350, name="Package Privilege")
	@NotNull
	public PackPrivilege getPackPrivilege() {
		return packPrivilege;
	}

	public void setPackPrivilege(PackPrivilege packPrivilege) {
		this.packPrivilege = packPrivilege;
	}

	@Editable(order=400, name="Issue Management", description="Issue administrative permission inside a project, including batch "
			+ "operations over multiple issues")
	@DependsOn(property="manageProject", value="false")
	public boolean isManageIssues() {
		return manageIssues;
	}

	public void setManageIssues(boolean manageIssues) {
		this.manageIssues = manageIssues;
	}
		
	@Editable(order=450, description="This permission enables one to access confidential issues")
	@DependsOn(property="manageIssues", value="false")
	public boolean isAccessConfidentialIssues() {
		return accessConfidentialIssues;
	}

	public void setAccessConfidentialIssues(boolean accessConfidentialIssues) {
		this.accessConfidentialIssues = accessConfidentialIssues;
	}

	@Editable(order=475, description = "Whether or not to be able to access time tracking info of issues")
	@ShowCondition("isSubscriptionActive")
	public boolean isAccessTimeTracking() {
		return accessTimeTracking;
	}

	public void setAccessTimeTracking(boolean accessTimeTracking) {
		this.accessTimeTracking = accessTimeTracking;
	}

	@Editable(order=500, description = "This permission enables one to schedule issues into iterations")
	@DependsOn(property="manageIssues", value="false")
	public boolean isScheduleIssues() {
		return scheduleIssues;
	}

	public void setScheduleIssues(boolean scheduleIssues) {
		this.scheduleIssues = scheduleIssues;
	}

	@Editable(order=600, description="Optionally specify custom fields allowed to edit when open new issues")
	@DependsOn(property="manageIssues", value="false")
	@NotNull
	public IssueFieldSet getEditableIssueFields() {
		return editableIssueFields;
	}

	public void setEditableIssueFields(IssueFieldSet editableIssueFields) {
		this.editableIssueFields = editableIssueFields;
	}

	@SuppressWarnings("unused")
	private static List<String> getIssueFieldChoices() {
		return OneDev.getInstance(SettingService.class).getIssueSetting().getFieldNames();
	}
	
	@Editable(order=625, placeholder="None", description="Optionally specify issue links allowed to edit")
	@DependsOn(property="manageIssues", value="false")
	@ChoiceProvider(value="getIssueLinkChoices", displayNames = "getIssueLinkDisplayNames")
	public List<String> getEditableIssueLinks() {
		return editableIssueLinks;
	}

	public void setEditableIssueLinks(List<String> editableIssueLinks) {
		this.editableIssueLinks = editableIssueLinks;
	}
	
	private static Map<String, String> getIssueLinkDisplayNames() {
		Map<String, String> choices = new LinkedHashMap<>();
		for (LinkSpec link: OneDev.getInstance(LinkSpecService.class).queryAndSort()) {
			choices.put(link.getName(), link.getDisplayName());
		}
		return choices;
	}

	@SuppressWarnings("unused")
	private static List<String> getIssueLinkChoices() {
		return new ArrayList<>(getIssueLinkDisplayNames().keySet());
	}

	@Editable(order=650, name="Build Management", description="Build administrative permission for all jobs inside a project, "
			+ "including batch operations over multiple builds")
	@DependsOn(property="manageProject", value="false")
	public boolean isManageBuilds() {
		return manageBuilds;
	}

	public void setManageBuilds(boolean manageBuilds) {
		this.manageBuilds = manageBuilds;
	}

	@Editable(order=675, description = "Enable to allow to upload build cache generated during CI/CD job. " +
			"Uploaded cache can be used by subsequent builds of the project as long as cache key matches")
	@DependsOn(property="manageBuilds", value="false")
	public boolean isUploadCache() {
		return uploadCache;
	}

	public void setUploadCache(boolean uploadCache) {
		this.uploadCache = uploadCache;
	}

	@Editable(order=700)
	@DependsOn(property="manageBuilds", value="false")
	public List<JobPrivilege> getJobPrivileges() {
		return jobPrivileges;
	}

	public void setJobPrivileges(List<JobPrivilege> jobPrivileges) {
		this.jobPrivileges = (ArrayList<JobPrivilege>) jobPrivileges;
	}

	public Collection<BaseAuthorization> getBaseAuthorizations() {
		return baseAuthorizations;
	}

	public void setBaseAuthorizations(Collection<BaseAuthorization> baseAuthorizations) {
		this.baseAuthorizations = baseAuthorizations;
	}

	public Collection<LinkAuthorization> getLinkAuthorizations() {
		return linkAuthorizations;
	}

	public void setLinkAuthorizations(Collection<LinkAuthorization> linkAuthorizations) {
		this.linkAuthorizations = linkAuthorizations;
	}

	public RoleFacade getFacade() {
		return new RoleFacade(getId(), getName());
	}
	
	@Override
	public boolean implies(Permission permission) {
		return getPermissions().stream().anyMatch(it -> it.implies(permission));
	}
	
	private Collection<BasePermission> getPermissions() {
		Collection<BasePermission> permissions = Lists.newArrayList(new AccessProject());
		
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
		if (packPrivilege == PackPrivilege.READ)
			permissions.add(new ReadPack());
		if (packPrivilege == PackPrivilege.WRITE)
			permissions.add(new WritePack());
		if (manageIssues) 
			permissions.add(new ManageIssues());
		if (accessConfidentialIssues)
			permissions.add(new AccessConfidentialIssues());
		if (accessTimeTracking)
			permissions.add(new AccessTimeTracking());
		if (scheduleIssues)
			permissions.add(new ScheduleIssues());
		permissions.add(new EditIssueField(editableIssueFields.getIncludeFields()));
		for (LinkAuthorization linkAuthorization: getLinkAuthorizations()) 
			permissions.add(new EditIssueLink(linkAuthorization.getLink()));
		if (manageBuilds)
			permissions.add(new ManageBuilds());
		if (uploadCache)
			permissions.add(new UploadCache());
		for (var jobPrivilege: jobPrivileges) {
			permissions.add(new JobPermission(jobPrivilege.getJobNames(), new AccessBuild()));
			if (jobPrivilege.isManageJob()) 
				permissions.add(new JobPermission(jobPrivilege.getJobNames(), new ManageJob()));
			if (jobPrivilege.isRunJob()) 
				permissions.add(new JobPermission(jobPrivilege.getJobNames(), new RunJob()));
			if (jobPrivilege.isAccessLog())
				permissions.add(new JobPermission(jobPrivilege.getJobNames(), new AccessBuildLog()));
			if (jobPrivilege.isAccessPipeline())
				permissions.add(new JobPermission(jobPrivilege.getJobNames(), new AccessBuildPipeline()));
			if (jobPrivilege.getAccessibleReports() != null) { 
				AccessBuildReports accessBuildReports = new AccessBuildReports(jobPrivilege.getAccessibleReports());
				permissions.add(new JobPermission(jobPrivilege.getJobNames(), accessBuildReports));
			}
		}
		return permissions;
	}
	
	@SuppressWarnings("unused")
	private static boolean isSubscriptionActive() {
		return WicketUtils.isSubscriptionActive();
	}

	@Override
	public boolean isApplicable(@Nullable UserFacade user) {
		return getPermissions().stream().allMatch(it -> it.isApplicable(user));
	}
	
}