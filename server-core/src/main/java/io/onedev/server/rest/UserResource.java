package io.onedev.server.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

import com.google.common.collect.Sets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.BuildQuerySetting;
import io.onedev.server.model.CodeCommentQuerySetting;
import io.onedev.server.model.CommitQuerySetting;
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.Membership;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestQuerySetting;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.NamedProjectQuery;
import io.onedev.server.model.support.SsoInfo;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.jersey.InvalidParamException;
import io.onedev.server.rest.support.RestConstants;
import io.onedev.server.security.SecurityUtils;

@Api(order=5000)
@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class UserResource {

	private final UserManager userManager;
	
	private final SshKeyManager sshKeyManager;
	
	private final PasswordService passwordService;
	
	@Inject
	public UserResource(UserManager userManager, SshKeyManager sshKeyManager, PasswordService passwordService) {
		this.userManager = userManager;
		this.sshKeyManager = sshKeyManager;
		this.passwordService = passwordService;
	}

	@Api(order=100)
	@Path("/{userId}")
    @GET
    public User getBasicInfo(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		return user;
    }

	@Api(order=200)
	@Path("/me")
    @GET
    public User getMyBasicInfo() {
		User user = SecurityUtils.getUser();
		if (user == null)
			throw new UnauthenticatedException();
		return user;
    }
	
	@Api(order=300)
	@Path("/{userId}/authorizations")
    @GET
    public Collection<UserAuthorization> getAuthorizations(@PathParam("userId") Long userId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	return userManager.load(userId).getAuthorizations();
    }
	
	@Api(order=400)
	@Path("/{userId}/memberships")
    @GET
    public Collection<Membership> getMemberships(@PathParam("userId") Long userId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	return userManager.load(userId).getMemberships();
    }
	
	@Api(order=500)
	@Path("/{userId}/sso-info")
    @GET
    public SsoInfo getSsoInfo(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getSsoInfo();
    }
	
	@Api(order=600)
	@Path("/{userId}/pull-request-reviews")
    @GET
    public Collection<PullRequestReview> getPullRequestReviews(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getPullRequestReviews();
    }
	
	@Api(order=700)
	@Path("/{userId}/issue-votes")
    @GET
    public Collection<IssueVote> getIssueVotes(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getIssueVotes();
    }
	
	@Api(order=800)
	@Path("/{userId}/issue-watches")
    @GET
    public Collection<IssueWatch> getIssueWatches(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getIssueWatches();
    }
	
	@Api(order=900)
	@Path("/{userId}/project-build-query-settings")
    @GET
    public Collection<BuildQuerySetting> getProjectBuildQuerySettings(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getProjectBuildQuerySettings();
    }
	
	@Api(order=1000)
	@Path("/{userId}/project-code-comment-query-settings")
    @GET
    public Collection<CodeCommentQuerySetting> getProjectCodeCommentQuerySettings(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getProjectCodeCommentQuerySettings();
    }
	
	@Api(order=1100)
	@Path("/{userId}/project-commit-query-settings")
    @GET
    public Collection<CommitQuerySetting> getProjectCommitQuerySettings(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getProjectCommitQuerySettings();
    }
	
	@Api(order=1200)
	@Path("/{userId}/project-issue-query-settings")
    @GET
    public Collection<IssueQuerySetting> getProjecIssueQuerySettings(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getProjectIssueQuerySettings();
    }
	
	@Api(order=1300)
	@Path("/{userId}/project-pull-request-query-settings")
    @GET
    public Collection<PullRequestQuerySetting> getProjecPullRequestQuerySettings(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getProjectPullRequestQuerySettings();
    }
	
	@Api(order=1400)
	@Path("/{userId}/pull-request-assignments")
    @GET
    public Collection<PullRequestAssignment> getPullRequestAssignments(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getPullRequestAssignments();
    }
	
	@Api(order=1500)
	@Path("/{userId}/pull-request-watches")
    @GET
    public Collection<PullRequestWatch> getPullRequestWatches(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getPullRequestWatches();
    }
	
	@Api(order=1600)
	@Path("/{userId}/ssh-keys")
    @GET
    public Collection<SshKey> getSshKeys(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getSshKeys();
    }
	
	@Api(order=1700)
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
	
	@Api(order=1800)
	@GET
    public List<User> queryBasicInfo(@QueryParam("name") String name, @QueryParam("fullName") String fullName, 
    		@QueryParam("email") String email, @QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		
    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

		EntityCriteria<User> criteria = EntityCriteria.of(User.class);
		criteria.add(Restrictions.gt("id", 0L));
		if (name != null) 
			criteria.add(Restrictions.ilike("name", name.replace('*', '%'), MatchMode.EXACT));
		if (fullName != null) 		
			criteria.add(Restrictions.ilike("fullName", fullName.replace('*', '%'), MatchMode.EXACT));
		if (email != null) 		
			criteria.add(Restrictions.ilike("email", email.replace('*', '%'), MatchMode.EXACT));
		
    	return userManager.query(criteria, offset, count);
    }
	
	@Api(order=1900, description="Update user of specified id in request body, or create new if id property not provided")
    @POST
    public Long createOrUpdate(@NotNull User user) {
    	if (user.isNew()) {
    		if (!SecurityUtils.isAdministrator()) {
    			throw new UnauthorizedException();
    		} else {
    			user.setPassword("impossible_password");
    			checkEmails(user);
    			userManager.save(user);
    		}
    	} else {
        	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) { 
				throw new UnauthorizedException();
        	} else {
        		checkEmails(user);
	    		userManager.save(user, (String) user.getCustomData());
	    	}
    	}
    	return user.getId();
    }
	
	private void checkEmails(User user) {
		Set<String> emails = Sets.newHashSet(user.getEmail());
		if (user.getGitEmail() != null)
			emails.add(user.getGitEmail());
		emails.addAll(user.getAlternateEmails());
		for (String email: emails) {
			User userWithSameEmail = userManager.findByEmail(email);
			if (userWithSameEmail != null && !userWithSameEmail.equals(user)) 
				throw new ExplicitException("Email '" + email + "' already used by another user.");
		}
	}
	
	@Api(order=2000)
	@Path("/{userId}/password")
    @POST
    public Response setPassword(@PathParam("userId") Long userId, @NotEmpty String password) {
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
	
	@Api(order=2100)
	@Path("/{userId}/sso-info")
    @POST
    public Response setSsoInfo(@PathParam("userId") Long userId, @NotNull SsoInfo ssoInfo) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	User user = userManager.load(userId);
    	user.setSsoInfo(ssoInfo);
    	userManager.save(user);
    	return Response.ok().build();
    }
	
	@Api(order=2100)
	@Path("/{userId}/queries-and-watches")
    @POST
    public Response setQueriesAndWatches(@PathParam("userId") Long userId, @NotNull QueriesAndWatches queriesAndWatches) {
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
	
	@Api(order=2200)
	@Path("/{userId}/ssh-keys")
	@POST
	public Long addSshKey(@PathParam("userId") Long userId, @NotNull String content) {
		User user = SecurityUtils.getUser();
		
		SshKey sshKey = new SshKey();
		sshKey.setContent(content);
		sshKey.setCreatedAt(new Date());
		sshKey.setOwner(user);
		sshKey.digest();
        
		sshKeyManager.save(sshKey);
		return sshKey.getId();
	}
	
	@Api(order=2300)
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
