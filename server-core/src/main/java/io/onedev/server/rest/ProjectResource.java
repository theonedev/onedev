package io.onedev.server.rest;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.GitContribution;
import io.onedev.server.git.GitContributor;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.NamedCodeCommentQuery;
import io.onedev.server.model.support.NamedCommitQuery;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.model.support.build.ProjectBuildSetting;
import io.onedev.server.model.support.issue.ProjectIssueSetting;
import io.onedev.server.model.support.pullrequest.ProjectPullRequestSetting;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.jersey.InvalidParamException;
import io.onedev.server.rest.support.RestConstants;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Day;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;

@Api(order=1000)
@Path("/projects")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ProjectResource {

	private final ProjectManager projectManager;
	
	private final MilestoneManager milestoneManager;
	
	private final CommitInfoManager commitInfoManager;
	
	@Inject
	public ProjectResource(ProjectManager projectManager, MilestoneManager milestoneManager, 
			CommitInfoManager commitInfoManager) {
		this.projectManager = projectManager;
		this.milestoneManager = milestoneManager;
		this.commitInfoManager = commitInfoManager;
	}

	@Api(order=100)
	@Path("/{projectId}")
    @GET
    public Project getBasicInfo(@PathParam("projectId") Long projectId) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canAccess(project))
			throw new UnauthorizedException();
    	return project;
    }
	
	@Api(order=200)
	@Path("/{projectId}/setting")
    @GET
    public ProjectSetting getSetting(@PathParam("projectId") Long projectId) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canManage(project)) 
			throw new UnauthorizedException();
		ProjectSetting setting = new ProjectSetting();
		setting.branchProtections = project.getBranchProtections();
		setting.tagProtections = project.getTagProtections();
		setting.buildSetting = project.getBuildSetting();
		setting.issueSetting = project.getIssueSetting();
		setting.namedCodeCommentQueries = project.getNamedCodeCommentQueries();
		setting.namedCommitQueries = project.getNamedCommitQueries();
		setting.pullRequestSetting = project.getPullRequestSetting();
		setting.webHooks = project.getWebHooks();
		setting.contributedSettings = project.getContributedSettings();
		return setting;
    }
	
	@Api(order=300)
	@Path("/{projectId}/forks")
    @GET
    public Collection<Project> getForks(@PathParam("projectId") Long projectId) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canAccess(project)) 
			throw new UnauthorizedException();
    	return project.getForks();
    }
	
	@Api(order=400)
	@Path("/{projectId}/group-authorizations")
    @GET
    public Collection<GroupAuthorization> getGroupAuthorizations(@PathParam("projectId") Long projectId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return projectManager.load(projectId).getGroupAuthorizations();
    }
	
	@Api(order=500)
	@Path("/{projectId}/user-authorizations")
    @GET
    public Collection<UserAuthorization> getUserAuthorizations(@PathParam("projectId") Long projectId) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canManage(project)) 
			throw new UnauthorizedException();
    	return project.getUserAuthorizations();
    }
	
	@Api(order=700)
	@GET
    public List<Project> queryBasicInfo(
    		@QueryParam("query") @Api(description="Syntax of this query is the same as query box in <a href='/projects'>projects page</a>", example="\"Name\" is \"projectName\"") String query, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		
    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

    	ProjectQuery parsedQuery;
		try {
			parsedQuery = ProjectQuery.parse(query);
		} catch (Exception e) {
			throw new InvalidParamException("Error parsing query", e);
		}
    	
    	return projectManager.query(parsedQuery, offset, count);
    }
	
	@Api(order=750)
	@Path("/{projectId}/milestones")
    @GET
    public List<Milestone> queryMilestones(@PathParam("projectId") Long projectId, @QueryParam("name") String name, 
    		@QueryParam("dueBefore") @Api(exampleProvider="getDateExample", description="ISO 8601 date") String dueBefore, 
    		@QueryParam("dueAfter") @Api(exampleProvider="getDateExample", description="ISO 8601 date") String dueAfter, 
    		@QueryParam("closed") Boolean closed, @QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canAccess(project)) 
			throw new UnauthorizedException();

    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);
    	
    	EntityCriteria<Milestone> criteria = EntityCriteria.of(Milestone.class);
    	criteria.add(Restrictions.eq(Milestone.PROP_PROJECT, project));
    	if (name != null)
    		criteria.add(Restrictions.ilike(Milestone.PROP_NAME, name.replace('%', '*')));
    	if (dueBefore != null)
    		criteria.add(Restrictions.le(Milestone.PROP_DUE_DATE, DateUtils.parseISO8601Date(dueBefore)));
    	if (dueAfter != null)
    		criteria.add(Restrictions.ge(Milestone.PROP_DUE_DATE, DateUtils.parseISO8601Date(dueAfter)));
    	if (closed != null)
    		criteria.add(Restrictions.eq(Milestone.PROP_CLOSED, closed));
    	
    	return milestoneManager.query(criteria, offset, count);
    }
	
	@Api(order=760, description="Get top contributors on default branch")
	@Path("/{projectId}/top-contributors")
	@GET
    public List<GitContributor> getTopContributors(@PathParam("projectId") Long projectId, 
    		@QueryParam("type") @NotNull GitContribution.Type type, 
    		@QueryParam("sinceDate") @NotEmpty @Api(description="Since date of format <i>yyyy-MM-dd</i>") String since, 
    		@QueryParam("untilDate") @NotEmpty @Api(description="Until date of format <i>yyyy-MM-dd</i>") String until, 
    		@QueryParam("count") int count) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canAccess(project)) 
			throw new UnauthorizedException();
    	
    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);
    	
    	Day sinceDay = new Day(LocalDate.parse(since));
    	Day untilDay = new Day(LocalDate.parse(until));
    	
    	return commitInfoManager.getTopContributors(project, count, type, sinceDay.getValue(), untilDay.getValue());
    }
	
	@SuppressWarnings("unused")
	private static String getDateExample() {
		return DateUtils.formatISO8601Date(new Date());
	}
	
	@Api(order=800, description="Update project of specified id in request body, or create new if id property not provided")
    @POST
    public Long createOrUpdate(@NotNull Project project) {
    	if (project.isNew()) 
    		projectManager.create(project);
    	else if (!SecurityUtils.canManage(project))
			throw new UnauthorizedException();
    	else
    		projectManager.save(project, (String) project.getCustomData());
    	
    	return project.getId();
    }
	
	@Api(order=900, description="Update project setting")
	@Path("/{projectId}/setting")
    @POST
    public Response updateSetting(@PathParam("projectId") Long projectId, @NotNull ProjectSetting setting) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canManage(project)) 
			throw new UnauthorizedException();
		project.setBranchProtections(setting.branchProtections);
		project.setTagProtections(setting.tagProtections);
		project.setBuildSetting(setting.buildSetting);
		project.setIssueSetting(setting.issueSetting);
		project.setNamedCodeCommentQueries(setting.namedCodeCommentQueries);
		project.setNamedCommitQueries(setting.namedCommitQueries);
		project.setPullRequestSetting(setting.pullRequestSetting);
		project.setWebHooks(setting.webHooks);
		projectManager.save(project);
		return Response.ok().build();
    }
	
	@Api(order=1000)
	@Path("/{projectId}")
    @DELETE
    public Response delete(@PathParam("projectId") Long projectId) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canManage(project))
			throw new UnauthorizedException();
    	projectManager.delete(project);
    	return Response.ok().build();
    }
	
	public static class ProjectSetting implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private ArrayList<BranchProtection> branchProtections = new ArrayList<>();
		
		private ArrayList<TagProtection> tagProtections = new ArrayList<>();
		
		private ProjectIssueSetting issueSetting = new ProjectIssueSetting();
		
		private ProjectBuildSetting buildSetting = new ProjectBuildSetting();
		
		private ProjectPullRequestSetting pullRequestSetting = new ProjectPullRequestSetting();
		
		private ArrayList<NamedCommitQuery> namedCommitQueries = new ArrayList<>();
		
		private ArrayList<NamedCodeCommentQuery> namedCodeCommentQueries = new ArrayList<>();
		
		private ArrayList<WebHook> webHooks = new ArrayList<>();
		
		private LinkedHashMap<String, ContributedProjectSetting> contributedSettings = new LinkedHashMap<>();

		public ArrayList<BranchProtection> getBranchProtections() {
			return branchProtections;
		}

		public void setBranchProtections(ArrayList<BranchProtection> branchProtections) {
			this.branchProtections = branchProtections;
		}

		public ArrayList<TagProtection> getTagProtections() {
			return tagProtections;
		}

		public void setTagProtections(ArrayList<TagProtection> tagProtections) {
			this.tagProtections = tagProtections;
		}

		public ProjectIssueSetting getIssueSetting() {
			return issueSetting;
		}

		public void setIssueSetting(ProjectIssueSetting issueSetting) {
			this.issueSetting = issueSetting;
		}

		public ProjectBuildSetting getBuildSetting() {
			return buildSetting;
		}

		public void setBuildSetting(ProjectBuildSetting buildSetting) {
			this.buildSetting = buildSetting;
		}

		public ProjectPullRequestSetting getPullRequestSetting() {
			return pullRequestSetting;
		}

		public void setPullRequestSetting(ProjectPullRequestSetting pullRequestSetting) {
			this.pullRequestSetting = pullRequestSetting;
		}

		public ArrayList<NamedCommitQuery> getNamedCommitQueries() {
			return namedCommitQueries;
		}

		public void setNamedCommitQueries(ArrayList<NamedCommitQuery> namedCommitQueries) {
			this.namedCommitQueries = namedCommitQueries;
		}

		public ArrayList<NamedCodeCommentQuery> getNamedCodeCommentQueries() {
			return namedCodeCommentQueries;
		}

		public void setNamedCodeCommentQueries(ArrayList<NamedCodeCommentQuery> namedCodeCommentQueries) {
			this.namedCodeCommentQueries = namedCodeCommentQueries;
		}

		public ArrayList<WebHook> getWebHooks() {
			return webHooks;
		}

		public void setWebHooks(ArrayList<WebHook> webHooks) {
			this.webHooks = webHooks;
		}

		public LinkedHashMap<String, ContributedProjectSetting> getContributedSettings() {
			return contributedSettings;
		}

		public void setContributedSettings(LinkedHashMap<String, ContributedProjectSetting> contributedSettings) {
			this.contributedSettings = contributedSettings;
		}
		
	}
	
}
