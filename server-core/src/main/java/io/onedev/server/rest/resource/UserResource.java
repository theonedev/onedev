package io.onedev.server.rest.resource;

import static io.onedev.server.security.SecurityUtils.getAuthUser;
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
import javax.ws.rs.NotAcceptableException;
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
import io.onedev.server.SubscriptionService;
import io.onedev.server.annotation.Password;
import io.onedev.server.annotation.UserName;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.SshKeyService;
import io.onedev.server.service.UserService;
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

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class UserResource {

	private final UserService userService;
	
	private final SshKeyService sshKeyService;
	
	private final PasswordService passwordService;
	
	private final EmailAddressService emailAddressService;

	private final SubscriptionService subscriptionService;

	private final AuditService auditService;
	
	@Inject
	public UserResource(UserService userService, SshKeyService sshKeyService,
                        PasswordService passwordService, EmailAddressService emailAddressService,
                        SubscriptionService subscriptionService, AuditService auditService) {
		this.userService = userService;
		this.sshKeyService = sshKeyService;
		this.passwordService = passwordService;
		this.emailAddressService = emailAddressService;
		this.subscriptionService = subscriptionService;
		this.auditService = auditService;
	}

	private UserData getData(User user) {
		var data = new UserData();
		data.setDisabled(user.isDisabled());
		data.setServiceAccount(user.isServiceAccount());
		data.setName(user.getName());
		data.setFullName(user.getFullName());
		if (!user.isServiceAccount()) 
			data.setNotifyOwnEvents(user.isNotifyOwnEvents());
		return data;
	}

	@Api(order=100)
	@Path("/{userId}")
    @GET
    public UserData getUser(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
		return getData(user);
    }

	@Api(order=200)
	@Path("/me")
    @GET
    public UserData getMe() {
		User user = getAuthUser();
		if (user == null)
			throw new UnauthorizedException();
		return getData(user);
    }
	
	@Api(order=250)
	@Path("/{userId}/access-tokens")
    @GET
    public Collection<AccessToken> getAccessTokens(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
		return user.getAccessTokens();
    }

	@Api(order=275)
	@Path("/{userId}/email-addresses")
    @GET
    public Collection<EmailAddress> getEmailAddresses(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
		return user.getEmailAddresses();
    }
	
	@Api(order=300)
	@Path("/{userId}/authorizations")
    @GET
    public Collection<UserAuthorization> getAuthorizations(@PathParam("userId") Long userId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	return userService.load(userId).getProjectAuthorizations();
    }
	
	@Api(order=400)
	@Path("/{userId}/memberships")
    @GET
    public Collection<Membership> getMemberships(@PathParam("userId") Long userId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	return userService.load(userId).getMemberships();
    }
	
	@Api(order=600)
	@Path("/{userId}/pull-request-reviews")
    @GET
    public Collection<PullRequestReview> getPullRequestReviews(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getPullRequestReviews();
    }
	
	@Api(order=700)
	@Path("/{userId}/issue-votes")
    @GET
    public Collection<IssueVote> getIssueVotes(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getIssueVotes();
    }
	
	@Api(order=800)
	@Path("/{userId}/issue-watches")
    @GET
    public Collection<IssueWatch> getIssueWatches(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getIssueWatches();
    }
	
	@Api(order=900)
	@Path("/{userId}/project-build-query-personalizations")
    @GET
    public Collection<BuildQueryPersonalization> getProjectBuildQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getBuildQueryPersonalizations();
    }
	
	@Api(order=1000)
	@Path("/{userId}/project-code-comment-query-personalizations")
    @GET
    public Collection<CodeCommentQueryPersonalization> getProjectCodeCommentQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getCodeCommentQueryPersonalizations();
    }
	
	@Api(order=1100)
	@Path("/{userId}/project-commit-query-personalizations")
    @GET
    public Collection<CommitQueryPersonalization> getProjectCommitQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getCommitQueryPersonalizations();
    }
	
	@Api(order=1200)
	@Path("/{userId}/project-issue-query-personalizations")
    @GET
    public Collection<IssueQueryPersonalization> getProjecIssueQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getIssueQueryPersonalizations();
    }
	
	@Api(order=1300)
	@Path("/{userId}/project-pull-request-query-personalizations")
    @GET
    public Collection<PullRequestQueryPersonalization> getProjecPullRequestQueryPersonalizations(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getPullRequestQueryPersonalizations();
    }
	
	@Api(order=1400)
	@Path("/{userId}/pull-request-assignments")
    @GET
    public Collection<PullRequestAssignment> getPullRequestAssignments(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getPullRequestAssignments();
    }
	
	@Api(order=1500)
	@Path("/{userId}/pull-request-watches")
    @GET
    public Collection<PullRequestWatch> getPullRequestWatches(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getPullRequestWatches();
    }
	
	@Api(order=1600)
	@Path("/{userId}/ssh-keys")
    @GET
    public Collection<SshKey> getSshKeys(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return user.getSshKeys();
    }

	private QueriesAndWatches getQueriesAndWatches(User user) {
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
	
	@Api(order=1700)
	@Path("/{userId}/queries-and-watches")
    @GET
    public QueriesAndWatches getQueriesAndWatches(@PathParam("userId") Long userId) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
		return getQueriesAndWatches(user);
    }
	
	@Api(order=1800)
	@GET
    public List<UserData> queryUsers(
    		@QueryParam("term") @Api(description="Any string in login name, full name or email address") String term, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();

    	return userService.query(term, offset, count).stream().map(this::getData).collect(toList());
    }
	
	@Api(order=1850)
	@Path("/ids/{name}")
	@GET
	public Long getUserId(@PathParam("name") @Api(description = "Login name of user") String name) {
		if (SecurityUtils.getAuthUser() == null)
			throw new UnauthenticatedException();

		var user = userService.findByName(name);
		if (user != null)
			return user.getId();
		else 
			throw new NotFoundException();
	}
	
	@Api(order=1900, description="Create new user")
    @POST
    public Long createUser(@NotNull @Valid UserCreateData data) {
		if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();

		if (userService.findByName(data.getName()) != null)
			throw new ExplicitException("Login name is already used by another user");
		if (!data.isServiceAccount() && emailAddressService.findByValue(data.getEmailAddress()) != null)
			throw new ExplicitException("Email address is already used by another user");
		
		User user = new User();
		user.setServiceAccount(data.isServiceAccount());
		user.setName(data.getName());
		user.setFullName(data.getFullName());
		if (data.isServiceAccount()) {
			userService.create(user);
		} else {
			user.setNotifyOwnEvents(data.isNotifyOwnEvents());
			user.setPassword(passwordService.encryptPassword(data.getPassword()));
			userService.create(user);
			EmailAddress emailAddress = new EmailAddress();
			emailAddress.setGit(true);
			emailAddress.setPrimary(true);
			emailAddress.setOwner(user);
			emailAddress.setValue(data.getEmailAddress());
			emailAddress.setVerificationCode(null);
			emailAddressService.create(emailAddress);
		}
		
		var newAuditContent = VersionedXmlDoc.fromBean(user).toXML();
		auditService.audit(null, "created account \"" + user.getName() + "\" via RESTful API", null, newAuditContent);

		return user.getId();
    }
	
	@Api(order=1950, name="Update user")
	@Path("/{userId}")
    @POST
    public Response updateUser(@PathParam("userId") Long userId, @NotNull @Valid UserUpdateData data) {
		User user = userService.load(userId);
		if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();
			
		User existingUser = userService.findByName(data.getName());
		if (existingUser != null && !existingUser.equals(user))
			throw new ExplicitException("Login name is already used by another user");

		var oldData = new UserUpdateData();
		oldData.setName(user.getName());
		oldData.setFullName(user.getFullName());
		oldData.setNotifyOwnEvents(user.isNotifyOwnEvents());

		var oldAuditContent = VersionedXmlDoc.fromBean(oldData).toXML();			

		String oldName = user.getName();
		user.setName(data.getName());
		user.setFullName(data.getFullName());
		if (!user.isServiceAccount())
			user.setNotifyOwnEvents(data.isNotifyOwnEvents());
		userService.update(user, oldName);

		if (!getAuthUser().equals(user)) {
			var newAuditContent = VersionedXmlDoc.fromBean(data).toXML();
			auditService.audit(null, "changed account \"" + user.getName() + "\" via RESTful API", oldAuditContent, newAuditContent);
		}
		
		return Response.ok().build();
    }

	@Api(order=1960, description="Disable user")
	@Path("/{userId}/disable")
    @POST
    public Response disableUser(@PathParam("userId") Long userId) {
		if (!subscriptionService.isSubscriptionActive())
			throw new NotAcceptableException("This operation requires active subscription");
		if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		
		if (userId <= User.ROOT_ID)		
			throw new BadRequestException("Should only disable normal users");
		var user = userService.load(userId);
		userService.disable(user);

		auditService.audit(null, "disabled account \"" + user.getName() + "\" via RESTful API", null, null);

		return Response.ok().build();
    }

	@Api(order=1970, description="Enable user")
	@Path("/{userId}/enable")
    @POST
    public Response enableUser(@PathParam("userId") Long userId) {
		if (!subscriptionService.isSubscriptionActive())
			throw new NotAcceptableException("This operation requires active subscription");
		if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		if (userId <= User.ROOT_ID)		
			throw new BadRequestException("Should only enable normal users");
		var user = userService.load(userId);
		userService.enable(user);

		auditService.audit(null, "enabled account \"" + user.getName() + "\" via RESTful API", null, null);

		return Response.ok().build();
    }

	@Api(order=1980, description="Convert to service account")
	@Path("/{userId}/convert-to-service-account")
    @POST
    public Response convertToServiceAccount(@PathParam("userId") Long userId) {
		if (!subscriptionService.isSubscriptionActive())
			throw new NotAcceptableException("This operation requires active subscription");
		if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		if (userId <= User.ROOT_ID)		
			throw new BadRequestException("Should only convert normal users to service accounts");
		var user = userService.load(userId);
		userService.convertToServiceAccount(user);

		auditService.audit(null, "converted user \"" + user.getName() + "\" to service account via RESTful API", null, null);

		return Response.ok().build();
    }
	
	@Api(order=2000)
	@Path("/{userId}/password")
    @POST
    public Response setPassword(@PathParam("userId") Long userId, @Password(checkPolicy=true) @NotEmpty String password) {
    	User user = userService.load(userId);
		if (SecurityUtils.isAdministrator()) {
			user.setPassword(passwordService.encryptPassword(password));
			userService.update(user, null);
			if (!getAuthUser().equals(user)) 
				auditService.audit(null, "changed password of account \"" + user.getName() + "\" via RESTful API", null, null);
			return Response.ok().build();
		} else if (user.isDisabled()) {
			throw new ExplicitException("Can not set password for disabled user");
		} else if (user.isServiceAccount()) {
			throw new ExplicitException("Can not set password for service account");
		} else if (user.equals(getAuthUser())) {
			if (user.getPassword() == null) {
				throw new ExplicitException("The user is currently authenticated via external system, "
						+ "please change password there instead");
			} else {
				user.setPassword(passwordService.encryptPassword(password));
				userService.update(user, null);
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
		if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();

		User user = userService.load(userId);		
		if (user.isDisabled()) {
			throw new ExplicitException("Can not reset two factor authentication for disabled user");
		} else if (user.isServiceAccount()) {
			throw new ExplicitException("Can not reset two factor authentication for service account");
		} else {
			user.setTwoFactorAuthentication(null);
			userService.update(user, null);
			auditService.audit(null, "reset two factor authentication of account \"" + user.getName() + "\" via RESTful API", null, null);
			return Response.ok().build();
		}
	}
	
	@Api(order=2100)
	@Path("/{userId}/queries-and-watches")
    @POST
    public Response setQueriesAndWatches(@PathParam("userId") Long userId, @NotNull QueriesAndWatches queriesAndWatches) {
    	User user = userService.load(userId);
    	if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser())) 
			throw new UnauthorizedException();

		if (user.isDisabled()) 
			throw new ExplicitException("Can not set queries and watches for disabled user");
		else if (user.isServiceAccount()) 
			throw new ExplicitException("Can not set queries and watches for service account");

		var oldAuditContent = VersionedXmlDoc.fromBean(getQueriesAndWatches(user)).toXML();

		user.setBuildQuerySubscriptions(queriesAndWatches.buildQuerySubscriptions);
		user.setIssueQueryWatches(queriesAndWatches.issueQueryWatches);
		user.setPullRequestQueryWatches(queriesAndWatches.pullRequestQueryWatches);
		user.setBuildQueries(queriesAndWatches.buildQueries);
		user.setIssueQueries(queriesAndWatches.issueQueries);
		user.setIssueQueryWatches(queriesAndWatches.issueQueryWatches);
		user.setProjectQueries(queriesAndWatches.projectQueries);
		user.setPullRequestQueries(queriesAndWatches.pullRequestQueries);
		userService.update(user, null);

		if (!getAuthUser().equals(user)) {
			var newAuditContent = VersionedXmlDoc.fromBean(queriesAndWatches).toXML();
			auditService.audit(null, "changed queries and watches of account \"" + user.getName() + "\" via RESTful API", oldAuditContent, newAuditContent);
		}

		return Response.ok().build();
    }
	
	@Api(order=2200)
	@Path("/{userId}/ssh-keys")
	@POST
	public Long addSshKey(@PathParam("userId") Long userId, @NotNull String content) {
		User user = userService.load(userId);
		if (!SecurityUtils.isAdministrator() && !user.equals(getAuthUser()))
			throw new UnauthorizedException();
		
		if (user.isDisabled())
			throw new ExplicitException("Can not add ssh key for disabled user");

		SshKey sshKey = new SshKey();
		sshKey.setContent(content);
		sshKey.setCreatedAt(new Date());
		sshKey.setOwner(user);
		sshKey.generateFingerprint();
        
		sshKeyService.create(sshKey);

		if (!getAuthUser().equals(user)) {
			var newAuditContent = VersionedXmlDoc.fromBean(sshKey).toXML();
			auditService.audit(null, "added ssh key to account \"" + user.getName() + "\" via RESTful API", null, newAuditContent);
		}

		return sshKey.getId();
	}
	
	@Api(order=2300)
	@Path("/{userId}")
    @DELETE
    public Response deleteUser(@PathParam("userId") Long userId) {
    	if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();

    	User user = userService.load(userId);
    	if (user.isRoot())
			throw new ExplicitException("Root user can not be deleted");
    	else if (user.equals(getAuthUser()))
    		throw new ExplicitException("Can not delete yourself");
    	else
    		userService.delete(user);

		var oldAuditContent = VersionedXmlDoc.fromBean(getData(user)).toXML();
		auditService.audit(null, "deleted account \"" + user.getName() + "\" via RESTful API", oldAuditContent, null);

    	return Response.ok().build();
    }

	public static class UserData implements Serializable {

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

		@Password(checkPolicy=true)
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

	public static class UserUpdateData implements Serializable {

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
