package io.onedev.server.rest.resource;

import static io.onedev.server.util.DateUtils.parseISO8601Date;
import static io.onedev.server.util.DateUtils.toLocalDate;
import static java.time.ZoneId.systemDefault;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.hibernate.criterion.Restrictions;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.git.GitContribution;
import io.onedev.server.git.GitContributor;
import io.onedev.server.model.BaseAuthorization;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLabel;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.CodeAnalysisSetting;
import io.onedev.server.model.support.NamedCodeCommentQuery;
import io.onedev.server.model.support.NamedCommitQuery;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.model.support.build.ProjectBuildSetting;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.GitPackConfig;
import io.onedev.server.model.support.code.TagProtection;
import io.onedev.server.model.support.issue.ProjectIssueSetting;
import io.onedev.server.model.support.pack.ProjectPackSetting;
import io.onedev.server.model.support.pullrequest.ProjectPullRequestSetting;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
import io.onedev.server.rest.resource.support.RestConstants;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.IterationService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.UrlService;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;
import io.onedev.server.xodus.CommitInfoService;

@Path("/projects")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ProjectResource {

	private final ProjectService projectService;
	
	private final IterationService iterationService;
	
	private final CommitInfoService commitInfoService;
	
	private final UrlService urlService;

	private final AuditService auditService;
	
	@Inject
	public ProjectResource(ProjectService projectService, IterationService iterationService,
                           CommitInfoService commitInfoService, UrlService urlService, AuditService auditService) {
		this.projectService = projectService;
		this.iterationService = iterationService;
		this.commitInfoService = commitInfoService;
		this.urlService = urlService;
		this.auditService = auditService;
	}
	
	@Api(order=100)
	@Path("/{projectId}")
    @GET
    public ProjectData getProject(@PathParam("projectId") Long projectId) {
    	Project project = projectService.load(projectId);
    	if (!SecurityUtils.canAccessProject(project))
			throw new UnauthorizedException();
     	return ProjectData.from(project);
    }

	@Api(order=125)
	@Path("/ids/{path:.*}")
	@GET
	public Long getProjectId(@PathParam("path") String path) {
		var project = projectService.findByPath(path);
		if (project != null) {
			if (!SecurityUtils.canAccessProject(project))
				throw new NotFoundException("Project not found or inaccessible: " + path);
			return project.getId();
		} else {
			throw new NotFoundException("Project not found or inaccessible: " + path);
		}
	}
	
	@Api(order=150)
	@Path("/{projectId}/clone-url")
    @GET
    public CloneUrl getCloneURL(@PathParam("projectId") Long projectId) {
    	Project project = projectService.load(projectId);
    	if (!SecurityUtils.canAccessProject(project))
			throw new UnauthorizedException();

    	CloneUrl cloneUrl = new CloneUrl();
    	cloneUrl.setHttp(urlService.cloneUrlFor(project, false));
    	cloneUrl.setSsh(urlService.cloneUrlFor(project, true));
    	
    	return cloneUrl;
    }
	
	@Api(order=200)
	@Path("/{projectId}/setting")
    @GET
    public ProjectSetting getSetting(@PathParam("projectId") Long projectId) {
    	Project project = projectService.load(projectId);
    	if (!SecurityUtils.canManageProject(project)) 
			throw new UnauthorizedException();
		return ProjectSetting.from(project);
    }
	
	@Api(order=300)
	@Path("/{projectId}/forks")
    @GET
    public Collection<Project> getForks(@PathParam("projectId") Long projectId) {
    	Project project = projectService.load(projectId);
    	if (!SecurityUtils.canAccessProject(project)) 
			throw new UnauthorizedException();
    	return project.getForks();
    }

	@Api(order=350, description = "A base authorization corresponds to a default role. It can be added/removed via <a href='/~help/api/io.onedev.server.rest.resource.BaseAuthorizationResource'>base authorizations resource</a>")
	@Path("/{projectId}/base-authorizations")
    @GET
    public Collection<BaseAuthorization> getBaseAuthorizations(@PathParam("projectId") Long projectId) {
    	Project project = projectService.load(projectId);
    	if (!SecurityUtils.canManageProject(project)) 
			throw new UnauthorizedException();
    	return project.getBaseAuthorizations();
    }
		
	@Api(order=400)
	@Path("/{projectId}/group-authorizations")
    @GET
    public Collection<GroupAuthorization> getGroupAuthorizations(@PathParam("projectId") Long projectId) {
		var project = projectService.load(projectId);
		if (!SecurityUtils.canManageProject(project))
			throw new UnauthorizedException();
    	return project.getGroupAuthorizations();
    }

	@Api(order=500)
	@Path("/{projectId}/user-authorizations")
    @GET
    public Collection<UserAuthorization> getUserAuthorizations(@PathParam("projectId") Long projectId) {
    	Project project = projectService.load(projectId);
    	if (!SecurityUtils.canManageProject(project)) 
			throw new UnauthorizedException();
    	return project.getUserAuthorizations();
    }

	@Api(order=600, description = "Get list of <a href='/~help/api/io.onedev.server.rest.ProjectLabelResource'>labels</a>")
	@Path("/{projectId}/labels")
	@GET
	public Collection<ProjectLabel> getLabels(@PathParam("projectId") Long projectId) {
		Project project = projectService.load(projectId);
		if (!SecurityUtils.canAccessProject(project))
			throw new UnauthorizedException();
		return project.getLabels();
	}
	
	@Api(order=700)
	@GET
    public List<ProjectData> queryProjects(
    		@QueryParam("query") @Api(description="Syntax of this query is the same as in <a href='/~projects'>projects page</a>", example="\"Name\" is \"projectName\"") String query, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {

		var subject = SecurityUtils.getSubject();
		if (!SecurityUtils.isAdministrator(subject) && count > RestConstants.MAX_PAGE_SIZE)
    		throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

    	ProjectQuery parsedQuery;
		try {
			parsedQuery = ProjectQuery.parse(query);
		} catch (Exception e) {
			throw new NotAcceptableException("Error parsing query", e);
		}
    	
    	return projectService.query(subject, parsedQuery, false, offset, count).stream()
    			.map(ProjectData::from)
    			.collect(Collectors.toList());
    }
	
	@Api(order=750)
	@Path("/{projectId}/iterations")
    @GET
    public List<Iteration> queryIterations(@PathParam("projectId") Long projectId, @QueryParam("name") String name,
										   @QueryParam("startBefore") @Api(exampleProvider="getDateExample", description="ISO 8601 date") String startBefore,
										   @QueryParam("startAfter") @Api(exampleProvider="getDateExample", description="ISO 8601 date") String startAfter,
										   @QueryParam("dueBefore") @Api(exampleProvider="getDateExample", description="ISO 8601 date") String dueBefore,
										   @QueryParam("dueAfter") @Api(exampleProvider="getDateExample", description="ISO 8601 date") String dueAfter,
										   @QueryParam("closed") Boolean closed, @QueryParam("offset") @Api(example="0") int offset,
										   @QueryParam("count") @Api(example="100") int count) {
    	Project project = projectService.load(projectId);
    	if (!SecurityUtils.canAccessProject(project)) 
			throw new UnauthorizedException();

    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);
    	
    	EntityCriteria<Iteration> criteria = EntityCriteria.of(Iteration.class);
    	criteria.add(Restrictions.in(Iteration.PROP_PROJECT, project.getSelfAndAncestors()));
    	if (name != null)
    		criteria.add(Restrictions.ilike(Iteration.PROP_NAME, name.replace('%', '*')));
    	if (startBefore != null)
    		criteria.add(Restrictions.le(Iteration.PROP_START_DAY, toLocalDate(parseISO8601Date(startBefore), systemDefault()).toEpochDay()));
    	if (startAfter != null)
    		criteria.add(Restrictions.ge(Iteration.PROP_START_DAY, toLocalDate(parseISO8601Date(startAfter), systemDefault()).toEpochDay()));
    	if (dueBefore != null)
    		criteria.add(Restrictions.le(Iteration.PROP_DUE_DAY, toLocalDate(parseISO8601Date(dueBefore), systemDefault()).toEpochDay()));
    	if (dueAfter != null)
    		criteria.add(Restrictions.ge(Iteration.PROP_DUE_DAY, toLocalDate(parseISO8601Date(dueAfter), systemDefault()).toEpochDay()));
    	if (closed != null)
    		criteria.add(Restrictions.eq(Iteration.PROP_CLOSED, closed));
    	
    	return iterationService.query(criteria, offset, count);
    }
	
	@Api(order=760, description="Get top contributors on default branch")
	@Path("/{projectId}/top-contributors")
	@GET
    public List<GitContributor> getTopContributors(@PathParam("projectId") Long projectId, 
    		@QueryParam("type") @NotNull GitContribution.Type type, 
    		@QueryParam("sinceDate") @NotEmpty @Api(description="Since date of format <i>yyyy-MM-dd</i>") String since, 
    		@QueryParam("untilDate") @NotEmpty @Api(description="Until date of format <i>yyyy-MM-dd</i>") String until, 
    		@QueryParam("count") int count) {
    	Project project = projectService.load(projectId);
    	if (!SecurityUtils.canAccessProject(project)) 
			throw new UnauthorizedException();
    	
    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);
    	
    	int sinceDay = (int) LocalDate.parse(since).toEpochDay();
    	int untilDay = (int) LocalDate.parse(until).toEpochDay();
    	
    	return commitInfoService.getTopContributors(project.getId(), count, type, sinceDay, untilDay);
    }
	
	@SuppressWarnings("unused")
	private static String getDateExample() {
		return DateUtils.formatISO8601Date(new Date());
	}
	
	@Api(order=800, description="Create new project")
    @POST
    public Long createProject(@NotNull @Valid ProjectData data) {
		var project = new Project();
		data.populate(project, projectService);
		
		var subject = SecurityUtils.getSubject();
		var user = SecurityUtils.getUser(subject);

		checkProjectCreationPermission(subject, project.getParent());
	
		if (project.getParent() != null && project.isSelfOrAncestorOf(project.getParent())) 
			throw new ExplicitException("Can not use current or descendant project as parent");
		
		checkProjectNameDuplication(project);		
		projectService.create(user, project);

		auditService.audit(project, "created project via RESTful API", null, VersionedXmlDoc.fromBean(data).toXML());

    	return project.getId();
    }

	@Api(order=850, description="Update project")
	@Path("/{projectId}")
	@POST
	public Response updateProject(@PathParam("projectId") Long projectId, @NotNull @Valid ProjectData data) {
		Project project = projectService.load(projectId);		
		var oldAuditContent = VersionedXmlDoc.fromBean(ProjectData.from(project)).toXML();
		data.populate(project, projectService);
		
		Project parent = data.getParentId() != null? projectService.load(data.getParentId()) : null;
		Long oldParentId = Project.idOf(project.getParent());

		var subject = SecurityUtils.getSubject();
		if (!Objects.equals(oldParentId, Project.idOf(parent))) 
			checkProjectCreationPermission(subject, parent);

		if (parent != null && project.isSelfOrAncestorOf(parent))
			throw new ExplicitException("Can not use current or descendant project as parent");

		checkProjectNameDuplication(project);

		if (!SecurityUtils.canManageProject(subject, project)) {
			throw new UnauthorizedException();
		} else {
			projectService.update(project);
			auditService.audit(project, "changed project via RESTful API", oldAuditContent, 
					VersionedXmlDoc.fromBean(ProjectData.from(project)).toXML());
		}

		return Response.ok().build();
	}
	
	private void checkProjectCreationPermission(Subject subject, @Nullable Project parent) {
		if (parent != null && !SecurityUtils.canCreateChildren(subject, parent))
			throw new UnauthorizedException("Not authorized to create project under '" + parent.getPath() + "'");
		if (parent == null && !SecurityUtils.canCreateRootProjects(subject))
			throw new UnauthorizedException("Not authorized to create root project");
	}
	
	private void checkProjectNameDuplication(Project project) {
		Project parent = project.getParent();
		Project projectWithSameName = projectService.find(parent, project.getName());
		if (projectWithSameName != null && !projectWithSameName.equals(project)) {
			if (parent != null) {
				throw new ExplicitException("Name '" + project.getName() + "' is already used by another project under '"
						+ parent.getPath() + "'");
			} else {
				throw new ExplicitException("Name '" + project.getName() + "' is already used by another root project");
			}
		}
	}
	
	@Api(order=900, description="Update project settings")
	@Path("/{projectId}/setting")
    @POST
    public Response updateSetting(@PathParam("projectId") Long projectId, @NotNull @Valid ProjectSetting setting) {
    	Project project = projectService.load(projectId);
    	if (!SecurityUtils.canManageProject(project)) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(ProjectSetting.from(project)).toXML();
		setting.populate(project);
		projectService.update(project);
		auditService.audit(project, "changed project settings via RESTful API", oldAuditContent, VersionedXmlDoc.fromBean(setting).toXML());
		
		return Response.ok().build();
    }
	
	@Api(order=1000)
	@Path("/{projectId}")
    @DELETE
    public Response deleteProject(@PathParam("projectId") Long projectId) {
    	Project project = projectService.load(projectId);
    	if (!SecurityUtils.canManageProject(project))
			throw new UnauthorizedException();
    	projectService.delete(project);
		if (project.getParent() != null)
			auditService.audit(project.getParent(), "deleted child project \"" + project.getName() + "\" via RESTful API", VersionedXmlDoc.fromBean(ProjectData.from(project)).toXML(), null);
		else
			auditService.audit(null, "deleted root project \"" + project.getName() + "\" via RESTful API", VersionedXmlDoc.fromBean(ProjectData.from(project)).toXML(), null);
    	return Response.ok().build();
    }
	
	public static class CloneUrl implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private String http;
		
		private String ssh;

		public String getHttp() {
			return http;
		}

		public void setHttp(String http) {
			this.http = http;
		}

		public String getSsh() {
			return ssh;
		}

		public void setSsh(String ssh) {
			this.ssh = ssh;
		}
		
	}

	@EntityCreate(Project.class)
	public static class ProjectData implements Serializable {

		private static final long serialVersionUID = 1L;
		
		@Api(order=50)
		@Id
		private Long id;
		
		@Api(order = 100, description="Represents the parent project of this project. Remove this property if "
			+ "the project is a root project when create/update the project. May be null")
		private Long parentId;

		@Api(order = 150, description="Represents the project from which this project is forked. Remove this property if "
			+ "the project is not a fork when create/update the project. May be null")
		private Long forkedFromId;

		@Api(order = 300)
		private String name;

		@Api(order = 400, description="May be empty")
		private String key;

		@JsonProperty(access = JsonProperty.Access.READ_ONLY)
		@Api(order = 450)
		private String path;

		@Api(order = 500, description="May be empty")
		private String description;

		@JsonProperty(access = JsonProperty.Access.READ_ONLY)
		@Api(order = 550)
		private Date createDate;

		@Api(order = 600)
		private boolean codeManagement = true;
	
		@Api(order = 650)
		private boolean packManagement = true;
		
		@Api(order = 700)
		private boolean issueManagement = true;
	
		@Api(order = 750)
		private boolean timeTracking = false;			
		
		@Api(order = 800, description = "May be empty")
		private String serviceDeskEmailAddress;

		@Api(order = 850)
		private GitPackConfig gitPackConfig;

		@Api(order = 900)
		private CodeAnalysisSetting codeAnalysisSetting;
		
		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getParentId() {
			return parentId;
		}

		public void setParentId(Long parentId) {
			this.parentId = parentId;
		}

		public Long getForkedFromId() {
			return forkedFromId;
		}

		public void setForkedFromId(Long forkedFromId) {
			this.forkedFromId = forkedFromId;
		}

		@NotEmpty
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Date getCreateDate() {
			return createDate;
		}

		public void setCreateDate(Date createDate) {
			this.createDate = createDate;
		}

		public boolean isCodeManagement() {
			return codeManagement;
		}

		public void setCodeManagement(boolean codeManagement) {
			this.codeManagement = codeManagement;
		}

		public boolean isPackManagement() {
			return packManagement;
		}

		public void setPackManagement(boolean packManagement) {
			this.packManagement = packManagement;
		}

		public boolean isIssueManagement() {
			return issueManagement;
		}

		public void setIssueManagement(boolean issueManagement) {
			this.issueManagement = issueManagement;
		}

		public boolean isTimeTracking() {
			return timeTracking;
		}

		public void setTimeTracking(boolean timeTracking) {
			this.timeTracking = timeTracking;
		}

		public String getServiceDeskEmailAddress() {
			return serviceDeskEmailAddress;
		}

		public void setServiceDeskEmailAddress(String serviceDeskEmailAddress) {
			this.serviceDeskEmailAddress = serviceDeskEmailAddress;
		}

		@NotNull
		public GitPackConfig getGitPackConfig() {
			return gitPackConfig;
		}

		public void setGitPackConfig(GitPackConfig gitPackConfig) {
			this.gitPackConfig = gitPackConfig;
		}

		@NotNull
		public CodeAnalysisSetting getCodeAnalysisSetting() {
			return codeAnalysisSetting;
		}

		public void setCodeAnalysisSetting(CodeAnalysisSetting codeAnalysisSetting) {
			this.codeAnalysisSetting = codeAnalysisSetting;
		}

		public void populate(Project project, ProjectService projectService) {
			if (parentId != null)
				project.setParent(projectService.load(getParentId()));
			else
				project.setParent(null);
			if (forkedFromId != null)
				project.setForkedFrom(projectService.load(getForkedFromId()));
			else
				project.setForkedFrom(null);
			project.setName(getName());
			project.setKey(getKey());
			project.setDescription(getDescription());
			project.setCodeManagement(isCodeManagement());
			project.setPackManagement(isPackManagement());
			project.setIssueManagement(isIssueManagement());
			project.setTimeTracking(isTimeTracking());
			project.setServiceDeskEmailAddress(getServiceDeskEmailAddress());
			project.setGitPackConfig(getGitPackConfig());
			project.setCodeAnalysisSetting(getCodeAnalysisSetting());
		}

		public static ProjectData from(Project project) {
			ProjectData data = new ProjectData();
			data.setId(project.getId());
			data.setParentId(Project.idOf(project.getParent()));
			data.setForkedFromId(Project.idOf(project.getForkedFrom()));
			data.setName(project.getName());
			data.setKey(project.getKey());
			data.setPath(project.getPath());
			data.setDescription(project.getDescription());
			data.setCreateDate(project.getCreateDate());
			data.setCodeManagement(project.isCodeManagement());
			data.setPackManagement(project.isPackManagement());
			data.setIssueManagement(project.isIssueManagement());
			data.setTimeTracking(project.isTimeTracking());
			data.setServiceDeskEmailAddress(project.getServiceDeskEmailAddress());
			data.setGitPackConfig(project.getGitPackConfig());
			data.setCodeAnalysisSetting(project.getCodeAnalysisSetting());

			return data;
		}

	}
	
	public static class ProjectSetting implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private ArrayList<BranchProtection> branchProtections = new ArrayList<>();
		
		private ArrayList<TagProtection> tagProtections = new ArrayList<>();
		
		private ProjectIssueSetting issueSetting = new ProjectIssueSetting();
		
		private ProjectBuildSetting buildSetting = new ProjectBuildSetting();
		
		private ProjectPackSetting packSetting = new ProjectPackSetting();
		
		private ProjectPullRequestSetting pullRequestSetting = new ProjectPullRequestSetting();
		
		private ArrayList<NamedCommitQuery> namedCommitQueries = new ArrayList<>();
		
		private ArrayList<NamedCodeCommentQuery> namedCodeCommentQueries = new ArrayList<>();
		
		private ArrayList<WebHook> webHooks = new ArrayList<>();
		
		private ArrayList<ContributedProjectSetting> contributedSettings = new ArrayList<>();

		@Valid
		public ArrayList<BranchProtection> getBranchProtections() {
			return branchProtections;
		}

		public void setBranchProtections(ArrayList<BranchProtection> branchProtections) {
			this.branchProtections = branchProtections;
		}

		@Valid
		public ArrayList<TagProtection> getTagProtections() {
			return tagProtections;
		}

		public void setTagProtections(ArrayList<TagProtection> tagProtections) {
			this.tagProtections = tagProtections;
		}

		@Valid
		public ProjectIssueSetting getIssueSetting() {
			return issueSetting;
		}

		public void setIssueSetting(ProjectIssueSetting issueSetting) {
			this.issueSetting = issueSetting;
		}

		@Valid
		public ProjectBuildSetting getBuildSetting() {
			return buildSetting;
		}

		public void setBuildSetting(ProjectBuildSetting buildSetting) {
			this.buildSetting = buildSetting;
		}

		@Valid
		public ProjectPackSetting getPackSetting() {
			return packSetting;
		}

		public void setPackSetting(ProjectPackSetting packSetting) {
			this.packSetting = packSetting;
		}

		@Valid
		public ProjectPullRequestSetting getPullRequestSetting() {
			return pullRequestSetting;
		}

		public void setPullRequestSetting(ProjectPullRequestSetting pullRequestSetting) {
			this.pullRequestSetting = pullRequestSetting;
		}

		@Valid
		public ArrayList<NamedCommitQuery> getNamedCommitQueries() {
			return namedCommitQueries;
		}

		public void setNamedCommitQueries(ArrayList<NamedCommitQuery> namedCommitQueries) {
			this.namedCommitQueries = namedCommitQueries;
		}

		@Valid
		public ArrayList<NamedCodeCommentQuery> getNamedCodeCommentQueries() {
			return namedCodeCommentQueries;
		}

		public void setNamedCodeCommentQueries(ArrayList<NamedCodeCommentQuery> namedCodeCommentQueries) {
			this.namedCodeCommentQueries = namedCodeCommentQueries;
		}

		@Valid
		public ArrayList<WebHook> getWebHooks() {
			return webHooks;
		}

		public void setWebHooks(ArrayList<WebHook> webHooks) {
			this.webHooks = webHooks;
		}

		@Valid
		public ArrayList<ContributedProjectSetting> getContributedSettings() {
			return contributedSettings;
		}

		public void setContributedSettings(ArrayList<ContributedProjectSetting> contributedSettings) {
			this.contributedSettings = contributedSettings;
		}
		
		public void populate(Project project) {
			project.setBranchProtections(getBranchProtections());
			project.setTagProtections(getTagProtections());
			project.setBuildSetting(getBuildSetting());
			project.setPackSetting(getPackSetting());
			project.setIssueSetting(getIssueSetting());
			project.setNamedCodeCommentQueries(getNamedCodeCommentQueries());
			project.setNamedCommitQueries(getNamedCommitQueries());
			project.setPullRequestSetting(getPullRequestSetting());
			project.setWebHooks(getWebHooks());
			var contributedSettings = new LinkedHashMap<String, ContributedProjectSetting>();
			for (var contributedSetting: getContributedSettings())
				contributedSettings.put(contributedSetting.getClass().getName(), contributedSetting);
			project.setContributedSettings(contributedSettings);
		}

		public static ProjectSetting from(Project project) {
			ProjectSetting setting = new ProjectSetting();
			setting.setBranchProtections(project.getBranchProtections());
			setting.setTagProtections(project.getTagProtections());
			setting.setBuildSetting(project.getBuildSetting());
			setting.setPackSetting(project.getPackSetting());
			setting.setIssueSetting(project.getIssueSetting());
			setting.setNamedCodeCommentQueries(project.getNamedCodeCommentQueries());
			setting.setNamedCommitQueries(project.getNamedCommitQueries());
			setting.setPullRequestSetting(project.getPullRequestSetting());
			setting.setWebHooks(project.getWebHooks());
			setting.getContributedSettings().addAll(project.getContributedSettings().values());

			return setting;
		}

	}
	
}
