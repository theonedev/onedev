package io.onedev.server.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedProjectQuery;
import io.onedev.server.model.support.SsoInfo;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.jersey.InvalidParamException;
import io.onedev.server.security.SecurityUtils;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class UserResource {

	private final UserManager userManager;
	
	private final PasswordService passwordService;
	
	@Inject
	public UserResource(UserManager userManager, PasswordService passwordService) {
		this.userManager = userManager;
		this.passwordService = passwordService;
	}

	@Path("/{userId}")
    @GET
    public User get(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		return user;
    }

	@Path("/{userId}/sso-info")
    @GET
    public SsoInfo getSsoInfo(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getSsoInfo();
    }
	
	@Path("/{userId}/queries-and-watches")
    @GET
    public QueriesAndWatches getQueriesAndWatches(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		QueriesAndWatches queriesAndWatches = new QueriesAndWatches();
		queriesAndWatches.buildQuerySubscriptions = user.getBuildQuerySubscriptions();
		queriesAndWatches.issueQueryWatches = user.getIssueQueryWatches();
		queriesAndWatches.pullRequestQueryWatches = user.getPullRequestQueryWatches();
		queriesAndWatches.userBuildQueries = user.getUserBuildQueries();
		queriesAndWatches.userBuildQuerySubscriptions = user.getUserBuildQuerySubscriptions();
		queriesAndWatches.userIssueQueries = user.getUserIssueQueries();
		queriesAndWatches.userIssueQueryWatches = user.getUserIssueQueryWatches();
		queriesAndWatches.userProjectQueries = user.getUserProjectQueries();
		queriesAndWatches.userPullRequestQueries = user.getUserPullRequestQueries();
		queriesAndWatches.userPullRequestQueryWatches = user.getUserPullRequestQueryWatches();
		return queriesAndWatches;
    }
	
	@GET
    public List<User> query(@QueryParam("name") String name, @QueryParam("fullName") String fullName, 
    		@QueryParam("email") String email, @QueryParam("offset") Integer offset, 
    		@QueryParam("count") Integer count) {
		
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		
    	if (offset == null)
    		offset = 0;
    	
    	if (count == null) 
    		count = RestConstants.PAGE_SIZE;
    	else if (count > RestConstants.PAGE_SIZE)
    		throw new InvalidParamException("Count should be less than " + RestConstants.PAGE_SIZE);

		EntityCriteria<User> criteria = EntityCriteria.of(User.class);
		criteria.add(Restrictions.not(Restrictions.eq("id", User.SYSTEM_ID)));
		if (name != null) 
			criteria.add(Restrictions.ilike("name", name.replace('*', '%'), MatchMode.EXACT));
		if (fullName != null) 		
			criteria.add(Restrictions.ilike("fullName", fullName.replace('*', '%'), MatchMode.EXACT));
		if (email != null) 		
			criteria.add(Restrictions.ilike("email", email.replace('*', '%'), MatchMode.EXACT));
		
    	return userManager.query(criteria, offset, count);
    }
	
    @POST
    public Long save(@NotNull User user) {
    	if (user.isNew()) {
    		if (!SecurityUtils.isAdministrator()) {
    			throw new UnauthenticatedException();
    		} else {
    			user.setPassword("12345");
    			userManager.save(user);
    		}
    	} else {
        	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
				throw new UnauthorizedException();
	    	else
	    		userManager.save(user, (String) user.getCustomData());
    	}
    	return user.getId();
    }
	
	@Path("/{userId}/password")
    @POST
    public Response savePassword(@PathParam("userId") Long userId, @NotEmpty String password) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) { 
			throw new UnauthorizedException();
    	} else if (user.getPassword().equals(User.EXTERNAL_MANAGED)) {
			if (user.getSsoInfo().getConnector() != null) {
				throw new ExplicitException("The user is currently authenticated via SSO provider '" 
						+ user.getSsoInfo().getConnector() + "', please change password there instead");
			} else {
				throw new ExplicitException("The user is currently authenticated via external system, "
						+ "please change password there instead");
			}
		} else {
	    	user.setPassword(passwordService.encryptPassword(password));
	    	userManager.save(user);
	    	return Response.ok().build();
		}
    }
	
	@Path("/{userId}/sso-info")
    @POST
    public Response saveSsoInfo(@PathParam("userId") Long userId, @NotNull SsoInfo ssoInfo) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	User user = userManager.load(userId);
    	user.setSsoInfo(ssoInfo);
    	userManager.save(user);
    	return Response.ok().build();
    }
	
	@Path("/{userId}/queries-and-watches")
    @POST
    public Response saveQueriesAndWatches(@PathParam("userId") Long userId, @NotNull QueriesAndWatches queriesAndWatches) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		user.setBuildQuerySubscriptions(queriesAndWatches.buildQuerySubscriptions);
		user.setIssueQueryWatches(queriesAndWatches.issueQueryWatches);
		user.setPullRequestQueryWatches(queriesAndWatches.pullRequestQueryWatches);
		user.setUserBuildQueries(queriesAndWatches.userBuildQueries);
		user.setUserBuildQuerySubscriptions(queriesAndWatches.userBuildQuerySubscriptions);
		user.setUserIssueQueries(queriesAndWatches.userIssueQueries);
		user.setUserIssueQueryWatches(queriesAndWatches.userIssueQueryWatches);
		user.setUserProjectQueries(queriesAndWatches.userProjectQueries);
		user.setUserPullRequestQueries(queriesAndWatches.userPullRequestQueries);
		user.setUserPullRequestQueryWatches(queriesAndWatches.userPullRequestQueryWatches);
		userManager.save(user);
		return Response.ok().build();
    }
	
	@Path("/{userId}")
    @DELETE
    public Response delete(@PathParam("userId") Long userId) {
    	if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	User user = userManager.load(userId);
    	if (user.isRoot())
			throw new ExplicitException("Root user can not be deleted");
    	else if (user.equals(SecurityUtils.getUser()))
    		throw new ExplicitException("Can not delete yourself");
    	else
    		userManager.delete(user);
    	return Response.ok().build();
    }
	
	public static class QueriesAndWatches implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		ArrayList<NamedProjectQuery> userProjectQueries;
		
		ArrayList<NamedIssueQuery> userIssueQueries;

		LinkedHashMap<String, Boolean> userIssueQueryWatches;
		
		LinkedHashMap<String, Boolean> issueQueryWatches;
		
		ArrayList<NamedPullRequestQuery> userPullRequestQueries;

		LinkedHashMap<String, Boolean> userPullRequestQueryWatches;

		LinkedHashMap<String, Boolean> pullRequestQueryWatches;
		
		ArrayList<NamedBuildQuery> userBuildQueries;

		LinkedHashSet<String> userBuildQuerySubscriptions;

		LinkedHashSet<String> buildQuerySubscriptions;
	}
	
}
