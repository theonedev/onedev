package io.onedev.server.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
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
import io.onedev.server.rest.jersey.InvalidParamException;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;

@Path("/projects")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ProjectResource {

	private final ProjectManager projectManager;
	
	private final MilestoneManager milestoneManager;
	
	@Inject
	public ProjectResource(ProjectManager projectManager, MilestoneManager milestoneManager) {
		this.projectManager = projectManager;
		this.milestoneManager = milestoneManager;
	}

	@Path("/{projectId}")
    @GET
    public Project get(@PathParam("projectId") Long projectId) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canAccess(project))
			throw new UnauthorizedException();
    	return project;
    }
	
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
		return setting;
    }
	
	@Path("/{projectId}/forks")
    @GET
    public Collection<Project> getForks(@PathParam("projectId") Long projectId) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canAccess(project)) 
			throw new UnauthorizedException();
    	return project.getForks();
    }
	
	@Path("/{projectId}/group-authorizations")
    @GET
    public Collection<GroupAuthorization> getGroupAuthorizations(@PathParam("projectId") Long projectId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return projectManager.load(projectId).getGroupAuthorizations();
    }
	
	@Path("/{projectId}/user-authorizations")
    @GET
    public Collection<UserAuthorization> getUserAuthorizations(@PathParam("projectId") Long projectId) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canManage(project)) 
			throw new UnauthorizedException();
    	return project.getUserAuthorizations();
    }
	
	@Path("/{projectId}/milestones")
    @GET
    public List<Milestone> queryMilestones(@PathParam("projectId") Long projectId, @QueryParam("name") String name, 
    		@QueryParam("dueBefore") Date dueBefore, @QueryParam("dueAfter") Date dueAfter, 
    		@QueryParam("closed") Boolean closed, @QueryParam("offset") int offset, 
    		@QueryParam("count") int count) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canAccess(project)) 
			throw new UnauthorizedException();

    	if (count > RestConstants.PAGE_SIZE)
    		throw new InvalidParamException("Count should be less than " + RestConstants.PAGE_SIZE);
    	
    	EntityCriteria<Milestone> criteria = EntityCriteria.of(Milestone.class);
    	criteria.add(Restrictions.eq(Milestone.PROP_PROJECT, project));
    	if (name != null)
    		criteria.add(Restrictions.ilike(Milestone.PROP_NAME, name.replace('%', '*')));
    	if (dueBefore != null)
    		criteria.add(Restrictions.le(Milestone.PROP_DUE_DATE, dueBefore));
    	if (dueAfter != null)
    		criteria.add(Restrictions.ge(Milestone.PROP_DUE_DATE, dueAfter));
    	if (closed != null)
    		criteria.add(Restrictions.eq(Milestone.PROP_CLOSED, closed));
    	
    	return milestoneManager.query(criteria, offset, count);
    }
	
	@GET
    public List<Project> query(@QueryParam("query") String query, @QueryParam("offset") int offset, 
    		@QueryParam("count") int count) {
		
    	if (count > RestConstants.PAGE_SIZE)
    		throw new InvalidParamException("Count should be less than " + RestConstants.PAGE_SIZE);

    	ProjectQuery parsedQuery;
		try {
			parsedQuery = ProjectQuery.parse(query);
		} catch (Exception e) {
			throw new InvalidParamException("Error parsing query", e);
		}
    	
    	return projectManager.query(parsedQuery, offset, count);
    }
	
    @POST
    public Long save(@NotNull Project project) {
    	if (project.isNew()) {
    		if (SecurityUtils.getUser() == null)
    			throw new UnauthenticatedException();
    		else 
    			projectManager.create(project);
    	} else {
	    	if (!SecurityUtils.canManage(project))
				throw new UnauthorizedException();
	    	else
	    		projectManager.save(project, (String) project.getCustomData());
    	}
    	return project.getId();
    }
	
	@Path("/{projectId}/setting")
    @POST
    public Response saveSetting(@PathParam("projectId") Long projectId, @NotNull ProjectSetting setting) {
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

		ArrayList<BranchProtection> branchProtections;
		
		ArrayList<TagProtection> tagProtections;
		
		ProjectIssueSetting issueSetting;
		
		ProjectBuildSetting buildSetting;
		
		ProjectPullRequestSetting pullRequestSetting;
		
		ArrayList<NamedCommitQuery> namedCommitQueries;
		
		ArrayList<NamedCodeCommentQuery> namedCodeCommentQueries;
		
		ArrayList<WebHook> webHooks;
		
	}
}
