package io.onedev.server.model;

import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.entityreference.WorkspaceReference;
import io.onedev.server.logging.WorkspaceLoggingSupport;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.search.entity.SortField;
import io.onedev.server.web.util.TextUtils;
import io.onedev.server.workspace.WorkspaceService;

@Entity
@Table(
		indexes={
				@Index(columnList="o_user_id"),
				@Index(columnList="o_project_id"),
				@Index(columnList=Workspace.PROP_SPEC),
				@Index(columnList= Workspace.PROP_STATUS),
				@Index(columnList= Workspace.PROP_BRANCH),
				@Index(columnList= Workspace.PROP_CREATE_DATE),
				@Index(columnList= Workspace.PROP_ACTIVE_DATE),
				@Index(columnList=AbstractEntity.PROP_NUMBER),
				@Index(columnList="o_numberScope_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_numberScope_id", AbstractEntity.PROP_NUMBER})}
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Workspace extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	private static ThreadLocal<Stack<Workspace>> stack = ThreadLocal.withInitial(Stack::new);

	public static final String WORKSPACES_DIR = "workspaces";

	public static final String LOG_FILE = "workspace.log";

	public static final String WORK_DIR = "work";

	public static final int MAX_BRANCH_LEN = 255;

	public static final int MAX_ERROR_MESSAGE_LEN = 2048;

	public static final String PROP_USER = "user";

	public static final String PROP_PROJECT = "project";

	public static final String PROP_SPEC = "specName";

	public static final String PROP_BRANCH = "branch";

	public static final String PROP_STATUS = "status";

	public static final String NAME_USER = "User";

	public static final String NAME_PROJECT = "Project";

	public static final String NAME_BRANCH = "Branch";

	public static final String NAME_SPEC = "Spec";

	public static final String NAME_STATUS = "Status";

	public static final String NAME_CREATE_DATE = "Create Date";

	public static final String PROP_CREATE_DATE = "createDate";

	public static final String NAME_ACTIVE_DATE = "Active Date";

	public static final String PROP_ACTIVE_DATE = "activeDate";

	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_NUMBER, NAME_PROJECT, NAME_BRANCH, NAME_SPEC, NAME_CREATE_DATE, NAME_ACTIVE_DATE);

	public static final Map<String, SortField<Workspace>> SORT_FIELDS = new LinkedHashMap<>();
	static {
		SORT_FIELDS.put(NAME_NUMBER, new SortField<>(PROP_NUMBER, DESCENDING));
		SORT_FIELDS.put(NAME_USER, new SortField<>(PROP_USER));
		SORT_FIELDS.put(NAME_PROJECT, new SortField<>(PROP_PROJECT));
		SORT_FIELDS.put(NAME_BRANCH, new SortField<>(PROP_BRANCH));
		SORT_FIELDS.put(NAME_SPEC, new SortField<>(PROP_SPEC));
		SORT_FIELDS.put(NAME_STATUS, new SortField<>(PROP_STATUS, ASCENDING));
		SORT_FIELDS.put(NAME_CREATE_DATE, new SortField<>(PROP_CREATE_DATE, DESCENDING));
		SORT_FIELDS.put(NAME_ACTIVE_DATE, new SortField<>(PROP_ACTIVE_DATE, DESCENDING));
	}

	public enum Status {
		PENDING, ACTIVE, ERROR;

		@Override
		public String toString() {
			return TextUtils.getDisplayValue(this);
		}

	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project numberScope;

	private long number;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;

	@Column(nullable=false)
	private String specName;

	@Column(nullable=false, length=MAX_BRANCH_LEN)
	private String branch;

	@Column(nullable=false)
	private Status status = Status.PENDING;

	@Column(nullable=false)
	private Date createDate = new Date();

	private Date activeDate;

	private Date errorDate;

	private transient Optional<WorkspaceSpec> specOptional;

	@JsonIgnore
	@Column(nullable=false)
	private String token;

	public Project getNumberScope() {
		return numberScope;
	}

	public void setNumberScope(Project numberScope) {
		this.numberScope = numberScope;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getSpecName() {
		return specName;
	}

	public void setSpecName(String specName) {
		this.specName = specName;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Nullable
	public Date getActiveDate() {
		return activeDate;
	}

	public void setActiveDate(Date activeDate) {
		this.activeDate = activeDate;
	}

	@Nullable
	public Date getErrorDate() {
		return errorDate;
	}

	public void setErrorDate(Date errorDate) {
		this.errorDate = errorDate;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public static String getProjectRelativeDirPath(Long workspaceNumber) {
		return WORKSPACES_DIR + "/s" + String.format("%03d", workspaceNumber % 1000) + "/" + workspaceNumber;
	}

	public File getDir() {
		return getWorkspaceService().getWorkspaceDir(getProject().getId(), getNumber());
	}

	public static File getLogFile(Long projectId, Long workspaceNumber) {
		File workspaceDir = getWorkspaceService().getWorkspaceDir(projectId, workspaceNumber);
		return new File(workspaceDir, LOG_FILE);
	}

	public static File getWorkDir(Long projectId, Long workspaceNumber) {
		File workspaceDir = getWorkspaceService().getWorkspaceDir(projectId, workspaceNumber);
		return new File(workspaceDir, WORK_DIR);
	}

	private static WorkspaceService getWorkspaceService() {
		return OneDev.getInstance(WorkspaceService.class);
	}

	public static String getLogLockName(Long projectId, Long workspaceNumber) {
		return "workspace-log:" + projectId + ":" + workspaceNumber;
	}

	public WorkspaceLoggingSupport getLoggingSupport() {
		return new WorkspaceLoggingSupport(this);
	}

	@Nullable
	public WorkspaceSpec getSpec() {
		if (specOptional == null) {
			specOptional = getProject().getHierarchyWorkspaceSpecs().stream()
				.filter(it -> it.getName().equals(getSpecName()))
				.findFirst();
		}
		return specOptional.orElse(null);
	}

	public Collection<String> getMaskSecrets() {
		var maskSecrets = new HashSet<String>();
		maskSecrets.add(getToken());
		maskSecrets.add(OneDev.getInstance(ClusterService.class).getCredential());

		var spec = getSpec();
		if (spec == null)
			return maskSecrets;
		
		for (var envVar: spec.getEnvVars()) {
			if (envVar.isSecret()) 
				maskSecrets.add(envVar.getSecretValue());
		}

		for (var registryLogin: spec.getRegistryLogins()) 
			maskSecrets.add(registryLogin.getPassword());
		for (var cacheConfig: spec.getCacheConfigs()) {
			if (cacheConfig.getUploadAccessToken() != null)
				maskSecrets.add(cacheConfig.getUploadAccessToken());
		}

		return maskSecrets;
	}

	public static void push(@Nullable Workspace workspace) {
		stack.get().push(workspace);
	}

	public static void pop() {
		stack.get().pop();
	}

	@Nullable
	public static Workspace get() {
		if (!stack.get().isEmpty()) 
			return stack.get().peek();
		else 
			return null;
	}

	public WorkspaceReference getReference() {
		return new WorkspaceReference(getProject(), getNumber());
	}

	public static String getLogChangeObservable(Long workspaceId) {
		return "workspace-log:" + workspaceId;
	}

	public String getStatusChangeObservable() {
		return "workspace-status:" + getId();
	}

}
