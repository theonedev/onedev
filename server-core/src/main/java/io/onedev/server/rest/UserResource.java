package io.onedev.server.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Sets;
import io.onedev.server.model.support.AccessToken;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.BuildQueryPersonalization;
import io.onedev.server.model.CodeCommentQueryPersonalization;
import io.onedev.server.model.CommitQueryPersonalization;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.IssueQueryPersonalization;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.Membership;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestQueryPersonalization;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.NamedProjectQuery;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.annotation.UserName;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Api(order=5000)
@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class UserResource {

	private final UserManager userManager;
	
	private final SshKeyManager sshKeyManager;
	
	private final PasswordService passwordService;
	
	private final EmailAddressManager emailAddressManager;
	
	@Inject
	public UserResource(UserManager userManager, SshKeyManager sshKeyManager, 
			PasswordService passwordService, EmailAddressManager emailAddressManager) {
		this.userManager = userManager;
		this.sshKeyManager = sshKeyManager;
		this.passwordService = passwordService;
		this.emailAddressManager = emailAddressManager;
	}

	@Api(order=100)
	@Path("/{userId}")
    @GET
    public User getProfile(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		return user;
    }

	@Api(order=200)
	@Path("/me")
    @GET
    public User getMyProfile() {
		User user = SecurityUtils.getUser();
		if (user == null)
			throw new UnauthenticatedException();
		return user;
    }
	
	@Api(order=250)
	@Path("/{userId}/access-tokens")
    @GET
    public List<AccessToken> getAccessTokens(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		return user.getAccessTokens();
    }

	@Api(order=275)
	@Path("/{userId}/email-addresses")
    @GET
    public Collection<EmailAddress> getEmailAddresses(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		return user.getEmailAddresses();
    }
	
	@Api(order=300)
	@Path("/{userId}/authorizations")
    @GET
    public Collection<UserAuthorization> getAuthorizations(@PathParam("userId") Long userId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	return userManager.load(userId).getProjectAuthorizations();
    }
	
	@Api(order=400)
	@Path("/{userId}/memberships")
    @GET
    public Collection<Membership> getMemberships(@PathParam("userId") Long userId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	return userManager.load(userId).getMemberships();
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
	@Path("/{userId}/project-build-query-personalizations")
    @GET
    public Collection<BuildQueryPersonalization> getProjectBuildQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getBuildQueryPersonalizations();
    }
	
	@Api(order=1000)
	@Path("/{userId}/project-code-comment-query-personalizations")
    @GET
    public Collection<CodeCommentQueryPersonalization> getProjectCodeCommentQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getCodeCommentQueryPersonalizations();
    }
	
	@Api(order=1100)
	@Path("/{userId}/project-commit-query-personalizations")
    @GET
    public Collection<CommitQueryPersonalization> getProjectCommitQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getCommitQueryPersonalizations();
    }
	
	@Api(order=1200)
	@Path("/{userId}/project-issue-query-personalizations")
    @GET
    public Collection<IssueQueryPersonalization> getProjecIssueQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getIssueQueryPersonalizations();
    }
	
	@Api(order=1300)
	@Path("/{userId}/project-pull-request-query-personalizations")
    @GET
    public Collection<PullRequestQueryPersonalization> getProjecPullRequestQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return user.getPullRequestQueryPersonalizations();
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
		queriesAndWatches.buildQueries = user.getUserBuildQueries();
		queriesAndWatches.issueQueries = user.getUserIssueQueries();
		queriesAndWatches.projectQueries = user.getUserProjectQueries();
		queriesAndWatches.pullRequestQueries = user.getUserPullRequestQueries();
		return queriesAndWatches;
    }
	
	@Api(order=1800)
	@GET
    public List<User> queryProfile(
    		@QueryParam("term") @Api(description="Any string in login name, full name or email address") String term, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();

    	return userManager.query(term, offset, count);
    }
	
	@Api(order=1900, description="Create new user")
    @POST
    public Long create(@NotNull @Valid UserCreateData data) {
		if (SecurityUtils.isAdministrator()) {
			if (userManager.findByName(data.getName()) != null)
				throw new ExplicitException("Login name is already used by another user");
			if (emailAddressManager.findByValue(data.getEmailAddress()) != null)
				throw new ExplicitException("Email address is already used by another user");
			
			User user = new User();
			user.setName(data.getName());
			user.setFullName(data.getFullName());
			user.setPassword(passwordService.encryptPassword(data.getPassword()));
			user.setGuest(data.isGuest());
			userManager.create(user);
			
			EmailAddress emailAddress = new EmailAddress();
			emailAddress.setGit(true);
			emailAddress.setPrimary(true);
			emailAddress.setOwner(user);
			emailAddress.setValue(data.getEmailAddress());
			emailAddress.setVerificationCode(null);
			emailAddressManager.create(emailAddress);
			
			return user.getId();
		} else {
			throw new UnauthenticatedException();
		}
    }

	@Api(order=1910, description="Create access token. This operation returns value of created access token")
	@Path("/{userId}/access-tokens")
	@POST
	public String createAccessToken(@PathParam("userId") Long userId, @NotNull @Valid AccessTokenData accessTokenData) {
		User user = userManager.load(userId);
		if (SecurityUtils.isAdministrator() || user.equals(SecurityUtils.getUser())) {
			AccessToken accessToken = new AccessToken();
			accessToken.setDescription(accessTokenData.getDescription());
			accessToken.setExpireDate(accessTokenData.getExpireDate());
			user.getAccessTokens().add(accessToken);
			userManager.update(user, null);
			return accessToken.getValue();
		} else {
			throw new UnauthenticatedException();
		}
	}
	
	@Api(order=1950, description="Update user profile")
	@Path("/{userId}")
    @POST
    public Response updateProfile(@PathParam("userId") Long userId, @NotNull @Valid ProfileUpdateData data) {
		User user = userManager.load(userId);
		if (SecurityUtils.isAdministrator() || user.equals(SecurityUtils.getUser())) { 
			User existingUser = userManager.findByName(data.getName());
			if (existingUser != null && !existingUser.equals(user))
				throw new ExplicitException("Login name is already used by another user");
			
			String oldName = user.getName();
			user.setName(data.getName());
			user.setFullName(data.getFullName());
			userManager.update(user, oldName);
			return Response.ok().build();
		} else { 
			throw new UnauthenticatedException();
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
			if (user.getSsoConnector() != null) {
				throw new ExplicitException("The user is currently authenticated via SSO provider '" 
						+ user.getSsoConnector() + "', please change password there instead");
			} else {
				throw new ExplicitException("The user is currently authenticated via external system, "
						+ "please change password there instead");
			}
		} else {
	    	user.setPassword(passwordService.encryptPassword(password));
	    	userManager.update(user, null);
	    	return Response.ok().build();
		}
    }
	
	@Api(order=2050)
	@Path("/{userId}/guest")
	@POST
	public Response setGuest(@PathParam("userId") Long userId, boolean guest) {
		User user = userManager.load(userId);
		if (!SecurityUtils.isAdministrator()) {
			throw new UnauthorizedException();
		} else if (user.isRoot()) {
			throw new ExplicitException("Can not change guest status of root user");
		} else if (user.equals(SecurityUtils.getUser())) {
			throw new ExplicitException("Can not change guest status of yourself");
		} else {
			userManager.setAsGuest(Sets.newHashSet(user), guest);
			return Response.ok().build();
		}
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
		user.setBuildQueries(queriesAndWatches.buildQueries);
		user.setIssueQueries(queriesAndWatches.issueQueries);
		user.setIssueQueryWatches(queriesAndWatches.issueQueryWatches);
		user.setProjectQueries(queriesAndWatches.projectQueries);
		user.setPullRequestQueries(queriesAndWatches.pullRequestQueries);
		userManager.update(user, null);
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
		sshKey.fingerprint();
        
		sshKeyManager.create(sshKey);
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

	@Api(order=2400, description="Delete access token by value")
	@Path("/{userId}/access-tokens/{accessTokenValue}")
	@DELETE
	public Response deleteAccessToken(@PathParam("userId") Long userId, @PathParam("accessTokenValue") @NotEmpty String accessTokenValue) {
		User user = userManager.load(userId);
		if (SecurityUtils.isAdministrator() || user.equals(SecurityUtils.getUser())) {
			var found = false;
			for (var it = user.getAccessTokens().iterator(); it.hasNext();) {
				if (it.next().getValue().equals(accessTokenValue)) {
					it.remove();
					found = true;
				}
			}
			if (found) {
				userManager.update(user, null);
				return Response.ok().build();
			} else {
				return Response.status(NOT_FOUND.getStatusCode(), "No access token found with specified value").build();
			}
		} else {
			throw new UnauthenticatedException();
		}
	}

	@EntityCreate(User.class)
	public static class UserCreateData implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private String name;
		
		private String password;
		
		private String fullName;
		
		private String emailAddress;
		
		private boolean guest;

		@Api(order=100, description="Login name of the user")
		@UserName
		@NotEmpty
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Api(order=150)
		@NotEmpty
		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		@Api(order=200)
		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		@Api(order=300)
		@Email
		@NotEmpty
		public String getEmailAddress() {
			return emailAddress;
		}

		public void setEmailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
		}

		@Api(order=400)
		public boolean isGuest() {
			return guest;
		}

		public void setGuest(boolean guest) {
			this.guest = guest;
		}
	}
	
	public static class ProfileUpdateData implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private String name;
		
		private String fullName;
		
		@Api(order=100, description="Login name of the user")
		@UserName
		@NotEmpty
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Api(order=200)
		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

	}
	
	public static class AccessTokenData implements Serializable {
		
		private static final long serialVersionUID = 1L;

		@Api(order=100, description = "Description of access token. Maybe null")
		private String description;

		@Api(order=200, description = "Expiration date of access token. Null to never expire")
		private Date expireDate;

		@Nullable
		public String getDescription() {
			return description;
		}

		public void setDescription(@Nullable String description) {
			this.description = description;
		}

		@Nullable
		public Date getExpireDate() {
			return expireDate;
		}

		public void setExpireDate(@Nullable Date expireDate) {
			this.expireDate = expireDate;
		}
	}
	
	public static class QueriesAndWatches implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		ArrayList<NamedProjectQuery> projectQueries;
		
		ArrayList<NamedIssueQuery> issueQueries;

		LinkedHashMap<String, Boolean> issueQueryWatches;
		
		ArrayList<NamedPullRequestQuery> pullRequestQueries;

		LinkedHashMap<String, Boolean> pullRequestQueryWatches;
		
		ArrayList<NamedBuildQuery> buildQueries;

		LinkedHashSet<String> buildQuerySubscriptions;
	}
	
}
