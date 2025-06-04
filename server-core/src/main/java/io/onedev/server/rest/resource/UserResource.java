package io.onedev.server.rest.resource;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.annotation.UserName;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.AccessToken;
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

	private BasicSetting getBasicSetting(User user) {
		var basicSetting = new BasicSetting();
		basicSetting.setDisabled(user.isDisabled());
		basicSetting.setServiceAccount(user.isServiceAccount());
		basicSetting.setName(user.getName());
		basicSetting.setFullName(user.getFullName());
		if (!user.isServiceAccount()) 
			basicSetting.setNotifyOwnEvents(user.isNotifyOwnEvents());
		return basicSetting;
	}

	@Api(order=100, name="Get Basic Settings")
	@Path("/{userId}")
    @GET
    public BasicSetting getBasicSetting(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
		return getBasicSetting(user);
    }

	@Api(order=200, name="Get Basic Settings of Current User")
	@Path("/me")
    @GET
    public BasicSetting getMyBasicSetting() {
		User user = SecurityUtils.getAuthUser();
		if (user == null)
			throw new UnauthorizedException();
		return getBasicSetting(user);
    }
	
	@Api(order=250)
	@Path("/{userId}/access-tokens")
    @GET
    public Collection<AccessToken> getAccessTokens(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
		return user.getAccessTokens();
    }

	@Api(order=275)
	@Path("/{userId}/email-addresses")
    @GET
    public Collection<EmailAddress> getEmailAddresses(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
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
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getPullRequestReviews();
    }
	
	@Api(order=700)
	@Path("/{userId}/issue-votes")
    @GET
    public Collection<IssueVote> getIssueVotes(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getIssueVotes();
    }
	
	@Api(order=800)
	@Path("/{userId}/issue-watches")
    @GET
    public Collection<IssueWatch> getIssueWatches(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getIssueWatches();
    }
	
	@Api(order=900)
	@Path("/{userId}/project-build-query-personalizations")
    @GET
    public Collection<BuildQueryPersonalization> getProjectBuildQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getBuildQueryPersonalizations();
    }
	
	@Api(order=1000)
	@Path("/{userId}/project-code-comment-query-personalizations")
    @GET
    public Collection<CodeCommentQueryPersonalization> getProjectCodeCommentQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getCodeCommentQueryPersonalizations();
    }
	
	@Api(order=1100)
	@Path("/{userId}/project-commit-query-personalizations")
    @GET
    public Collection<CommitQueryPersonalization> getProjectCommitQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getCommitQueryPersonalizations();
    }
	
	@Api(order=1200)
	@Path("/{userId}/project-issue-query-personalizations")
    @GET
    public Collection<IssueQueryPersonalization> getProjecIssueQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getIssueQueryPersonalizations();
    }
	
	@Api(order=1300)
	@Path("/{userId}/project-pull-request-query-personalizations")
    @GET
    public Collection<PullRequestQueryPersonalization> getProjecPullRequestQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getPullRequestQueryPersonalizations();
    }
	
	@Api(order=1400)
	@Path("/{userId}/pull-request-assignments")
    @GET
    public Collection<PullRequestAssignment> getPullRequestAssignments(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getPullRequestAssignments();
    }
	
	@Api(order=1500)
	@Path("/{userId}/pull-request-watches")
    @GET
    public Collection<PullRequestWatch> getPullRequestWatches(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getPullRequestWatches();
    }
	
	@Api(order=1600)
	@Path("/{userId}/ssh-keys")
    @GET
    public Collection<SshKey> getSshKeys(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getSshKeys();
    }
	
	@Api(order=1700)
	@Path("/{userId}/queries-and-watches")
    @GET
    public QueriesAndWatches getQueriesAndWatches(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
		QueriesAndWatches queriesAndWatches = new QueriesAndWatches();
		queriesAndWatches.buildQuerySubscriptions = user.getBuildQuerySubscriptions();
		queriesAndWatches.issueQueryWatches = user.getIssueQueryWatches();
		queriesAndWatches.pullRequestQueryWatches = user.getPullRequestQueryWatches();
		queriesAndWatches.buildQueries = user.getBuildQueries();
		queriesAndWatches.issueQueries = user.getIssueQueries();
		queriesAndWatches.projectQueries = user.getProjectQueries();
		queriesAndWatches.pullRequestQueries = user.getPullRequestQueries();
		return queriesAndWatches;
    }
	
	@Api(order=1800, name="Query Basic Settings")
	@GET
    public List<BasicSetting> queryBasicSetting(
    		@QueryParam("term") @Api(description="Any string in login name, full name or email address") String term, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();

    	return userManager.query(term, offset, count).stream().map(this::getBasicSetting).collect(toList());
    }
	
	@Api(order=1850)
	@Path("/ids/{name}")
	@GET
	public Long getId(@PathParam("name") @Api(description = "Login name of user") String name) {
		var user = userManager.findByName(name);
		if (user != null)
			return user.getId();
		else 
			throw new NotFoundException();
	}
	
	@Api(order=1900, description="Create new user")
    @POST
    public Long create(@NotNull @Valid UserCreateData data) {
		if (SecurityUtils.isAdministrator()) {
			if (userManager.findByName(data.getName()) != null)
				throw new ExplicitException("Login name is already used by another user");
			if (!data.isServiceAccount() && emailAddressManager.findByValue(data.getEmailAddress()) != null)
				throw new ExplicitException("Email address is already used by another user");
			
			User user = new User();
			user.setServiceAccount(data.isServiceAccount());
			user.setName(data.getName());
			user.setFullName(data.getFullName());
			if (data.isServiceAccount()) {
				userManager.create(user);
			} else {
				user.setNotifyOwnEvents(data.isNotifyOwnEvents());
				user.setPassword(passwordService.encryptPassword(data.getPassword()));
				userManager.create(user);
				EmailAddress emailAddress = new EmailAddress();
				emailAddress.setGit(true);
				emailAddress.setPrimary(true);
				emailAddress.setOwner(user);
				emailAddress.setValue(data.getEmailAddress());
				emailAddress.setVerificationCode(null);
				emailAddressManager.create(emailAddress);
			}
			return user.getId();
		} else {
			throw new UnauthenticatedException();
		}
    }
	
	@Api(order=1950, name="Update Basic Settings")
	@Path("/{userId}")
    @POST
    public Response updateBasicSetting(@PathParam("userId") Long userId, @NotNull @Valid BasicSettingUpdateData data) {
		User user = userManager.load(userId);
		if (SecurityUtils.isAdministrator() || user.equals(SecurityUtils.getAuthUser())) { 
			User existingUser = userManager.findByName(data.getName());
			if (existingUser != null && !existingUser.equals(user))
				throw new ExplicitException("Login name is already used by another user");
			
			String oldName = user.getName();
			user.setName(data.getName());
			user.setFullName(data.getFullName());
			if (!user.isServiceAccount())
				user.setNotifyOwnEvents(data.isNotifyOwnEvents());
			userManager.update(user, oldName);
			return Response.ok().build();
		} else { 
			throw new UnauthenticatedException();
		}
    }

	@Api(order=1960, description="Disable user")
	@Path("/{userId}/disable")
    @POST
    public Response disable(@PathParam("userId") Long userId) {
		if (SecurityUtils.isAdministrator()) { 	
			if (userId <= User.ROOT_ID)		
				throw new BadRequestException("Should only disable normal users");
			var user = userManager.load(userId);
			userManager.disable(user);
			return Response.ok().build();
		} else { 
			throw new UnauthenticatedException();
		}
    }

	@Api(order=1970, description="Enable user")
	@Path("/{userId}/enable")
    @POST
    public Response enable(@PathParam("userId") Long userId) {
		if (SecurityUtils.isAdministrator()) { 	
			if (userId <= User.ROOT_ID)		
				throw new BadRequestException("Should only enable normal users");
			var user = userManager.load(userId);
			userManager.enable(user);
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
		if (SecurityUtils.isAdministrator()) {
			user.setPassword(passwordService.encryptPassword(password));
			userManager.update(user, null);
			return Response.ok().build();
		} else if (user.isDisabled()) {
			throw new ExplicitException("Can not set password for disabled user");
		} else if (user.isServiceAccount()) {
			throw new ExplicitException("Can not set password for service account");
		} else if (user.equals(SecurityUtils.getAuthUser())) {
			if (user.getPassword() == null) {
				throw new ExplicitException("The user is currently authenticated via external system, "
						+ "please change password there instead");
			} else {
				user.setPassword(passwordService.encryptPassword(password));
				userManager.update(user, null);
				return Response.ok().build();
			}			
    	} else {
			throw new UnauthorizedException();
		}
    }

	@Api(order=2025)
	@Path("/{userId}/two-factor-authentication")
	@DELETE
	public Response resetTwoFactorAuthentication(@PathParam("userId") Long userId) {
		User user = userManager.load(userId);
		if (!SecurityUtils.isAdministrator()) {
			throw new UnauthorizedException();
		} else if (user.isDisabled()) {
			throw new ExplicitException("Can not reset two factor authentication for disabled user");
		} else if (user.isServiceAccount()) {
			throw new ExplicitException("Can not reset two factor authentication for service account");
		} else {
			user.setTwoFactorAuthentication(null);
			userManager.update(user, null);
			return Response.ok().build();
		}
	}
	
	@Api(order=2100)
	@Path("/{userId}/queries-and-watches")
    @POST
    public Response setQueriesAndWatches(@PathParam("userId") Long userId, @NotNull QueriesAndWatches queriesAndWatches) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser())) 
			throw new UnauthorizedException();
		else if (user.isDisabled()) 
			throw new ExplicitException("Can not set queries and watches for disabled user");
		else if (user.isServiceAccount()) 
			throw new ExplicitException("Can not set queries and watches for service account");
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
		User user = userManager.load(userId);
		if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getAuthUser()))
			throw new UnauthorizedException();
		else if (user.isDisabled())
			throw new ExplicitException("Can not add ssh key for disabled user");

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
    	else if (user.equals(SecurityUtils.getAuthUser()))
    		throw new ExplicitException("Can not delete yourself");
    	else
    		userManager.delete(user);
    	return Response.ok().build();
    }

	@EntityCreate(User.class)
	public static class UserCreateData implements Serializable {

		private static final long serialVersionUID = 1L;
		
		@Api(order=50, exampleProvider="getServiceAccountExample", description="Create user as service account")
		private boolean serviceAccount;

		@Api(order=100, description="Login name of the user")
		private String name;
		
		@Api(order=150, description = "Password of the user. Only required if not created as service account")
		private String password;
		
		private String fullName;
		
		@Api(order=300, description = "Email address of the user. Only required if not created as service account")
		private String emailAddress;

		@Api(order=400, description = "Whether or not to notify user on own events. Only required if not created as service account")
		private boolean notifyOwnEvents;

		public boolean isServiceAccount() {
			return serviceAccount;
		}

		public void setServiceAccount(boolean serviceAccount) {
			this.serviceAccount = serviceAccount;
		}

		@SuppressWarnings("unused")
		private static boolean getServiceAccountExample() {
			return false;
		}

		@UserName
		@NotEmpty
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

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

		@Email
		@NotEmpty
		public String getEmailAddress() {
			return emailAddress;
		}

		public void setEmailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
		}

		public boolean isNotifyOwnEvents() {
			return notifyOwnEvents;
		}

		public void setNotifyOwnEvents(boolean notifyOwnEvents) {
			this.notifyOwnEvents = notifyOwnEvents;
		}
	}

	public static class BasicSetting implements Serializable {
		private static final long serialVersionUID = 1L;
		
		@Api(order=10, description="Whether or not the user is disabled")
		private boolean disabled;

		@Api(order=50, description="Whether or not the user is a service account")
		private boolean serviceAccount;

		@Api(order=100, description="Login name of the user")
		private String name;
		
		@Api(order=200)
		private String fullName;

		@Api(order=300, description = "Whether or not to notify user on own events. Only meaningful for non service account")
		private boolean notifyOwnEvents;

		public boolean isDisabled() {
			return disabled;
		}

		public void setDisabled(boolean disabled) {
			this.disabled = disabled;
		}

		public boolean isServiceAccount() {
			return serviceAccount;
		}

		public void setServiceAccount(boolean serviceAccount) {
			this.serviceAccount = serviceAccount;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public boolean isNotifyOwnEvents() {
			return notifyOwnEvents;
		}

		public void setNotifyOwnEvents(boolean notifyOwnEvents) {
			this.notifyOwnEvents = notifyOwnEvents;
		}
	}

	public static class BasicSettingUpdateData implements Serializable {

		private static final long serialVersionUID = 1L;
		
		@Api(order=100, description="Login name of the user")
		private String name;
		
		@Api(order=200)
		private String fullName;

		@Api(order=300, description = "Whether or not to notify user on own events. Only required for non service account")
		private boolean notifyOwnEvents;
		
		@UserName
		@NotEmpty
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public boolean isNotifyOwnEvents() {
			return notifyOwnEvents;
		}

		public void setNotifyOwnEvents(boolean notifyOwnEvents) {
			this.notifyOwnEvents = notifyOwnEvents;
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
