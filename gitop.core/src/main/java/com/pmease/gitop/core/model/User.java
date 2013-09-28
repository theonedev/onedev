package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.apache.shiro.authz.Permission;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Password;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.core.permission.object.ProtectedObject;
import com.pmease.gitop.core.permission.object.UserBelonging;
import com.pmease.gitop.core.permission.operation.RepositoryOperation;

/**
 * This class represents either a project or an user in the system. 
 * <p>
 * In Gitop, users and projects are the same thing. 
 * If necessary, you can always treat an user account as a project account. 
 * {@link Repository} and {@link Team} are always created under a specific account.  
 *  
 * @author robin
 *
 */
@SuppressWarnings("serial")
@Entity
@Editable
public class User extends AbstractUser implements ProtectedObject {

	@Column(nullable=false)
	private String email;
	
	private String fullName;
	
	@OneToMany(mappedBy="user")
	private Collection<TeamMembership> teamMemberships = new ArrayList<TeamMembership>();
	
	@OneToMany(mappedBy="user")
	private Collection<RoleMembership> roleMemberships = new ArrayList<RoleMembership>();
	
	@OneToMany(mappedBy="submitter")
	private Collection<MergeRequest> mergeRequests = new ArrayList<MergeRequest>();
	
	@OneToMany(mappedBy="owner")
	private Collection<Repository> repositories = new ArrayList<Repository>();

	@OneToMany(mappedBy="owner")
	private Collection<Team> teams = new ArrayList<Team>();
	
	@OneToMany(mappedBy="voter")
	private Collection<Vote> votes = new ArrayList<Vote>();
	
	@OneToMany(mappedBy="voter")
	private Collection<VoteInvitation> voteVitations = new ArrayList<VoteInvitation>();

	@OneToMany(mappedBy="individual")
	private Collection<RepositoryAuthorizationByIndividual> repositoryAuthorizations = 
			new ArrayList<RepositoryAuthorizationByIndividual>();

	@OneToMany(mappedBy="individual")
	private Collection<UserAuthorizationByIndividual> userAuthorizations = 
			new ArrayList<UserAuthorizationByIndividual>();

	@OneToMany(mappedBy="user")
	private Collection<UserAuthorizationByIndividual> authorizationsByIndividual = 
			new ArrayList<UserAuthorizationByIndividual>();

	@Editable(order=100)
	@NotEmpty
	@Override
	public String getName() {
		return super.getName();
	}

	@Editable(order=200)
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Editable(order=300)
	@NotEmpty
	@Email
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Editable(name="Password", order=400)
	@Password(confirmative=true)
	@NotEmpty
	@Override
	public String getPasswordHash() {
		return super.getPasswordHash();
	}

	public Collection<TeamMembership> getTeamMemberships() {
		return teamMemberships;
	}

	public void setTeamMemberships(Collection<TeamMembership> teamMemberships) {
		this.teamMemberships = teamMemberships;
	}

	public Collection<RoleMembership> getRoleMemberships() {
		return roleMemberships;
	}

	public void setRoleMemberships(Collection<RoleMembership> roleMemberships) {
		this.roleMemberships = roleMemberships;
	}

	public Collection<Repository> getRepositories() {
		return repositories;
	}

	public void setRepositories(Collection<Repository> repositories) {
		this.repositories = repositories;
	}

	public Collection<Team> getTeams() {
		return teams;
	}

	public void setTeams(Collection<Team> teams) {
		this.teams = teams;
	}

	public Collection<MergeRequest> getMergeRequests() {
		return mergeRequests;
	}

	public void setMergeRequests(Collection<MergeRequest> mergeRequests) {
		this.mergeRequests = mergeRequests;
	}

	public Collection<Vote> getVotes() {
		return votes;
	}

	public void setVotes(Collection<Vote> votes) {
		this.votes = votes;
	}

	public Collection<VoteInvitation> getVoteInvitations() {
		return voteVitations;
	}

	public void setVoteInvitations(Collection<VoteInvitation> voteInvitations) {
		this.voteVitations = voteInvitations;
	}

	public Collection<VoteInvitation> getVoteVitations() {
		return voteVitations;
	}

	public void setVoteVitations(Collection<VoteInvitation> voteVitations) {
		this.voteVitations = voteVitations;
	}

	public Collection<RepositoryAuthorizationByIndividual> getRepositoryAuthorizations() {
		return repositoryAuthorizations;
	}

	public void setRepositoryAuthorizations(
			Collection<RepositoryAuthorizationByIndividual> repositoryAuthorizations) {
		this.repositoryAuthorizations = repositoryAuthorizations;
	}

	public Collection<UserAuthorizationByIndividual> getUserAuthorizations() {
		return userAuthorizations;
	}

	public void setUserAuthorizations(
			Collection<UserAuthorizationByIndividual> userAuthorizations) {
		this.userAuthorizations = userAuthorizations;
	}

	public Collection<UserAuthorizationByIndividual> getAuthorizationsByIndividual() {
		return authorizationsByIndividual;
	}

	public void setAuthorizationsByIndividual(
			Collection<UserAuthorizationByIndividual> authorizationsByIndividual) {
		this.authorizationsByIndividual = authorizationsByIndividual;
	}

	@Override
	public boolean has(ProtectedObject object) {
		if (object instanceof User) {
			User user = (User) object;
			return user.equals(this);
		} else if (object instanceof UserBelonging) {
			UserBelonging userBelonging = (UserBelonging) object;
			return userBelonging.getUser().equals(this);
		} else {
			return false;
		}
	}
	
	public Vote.Result checkVoteSince(MergeRequestUpdate update) {
		if (update.getRequest().getSubmitter().equals(this))
			return Vote.Result.ACCEPT;
		
		for (Vote vote: update.listVotesOnwards()) {
			if (vote.getVoter().equals(this)) {
				return vote.getResult();
			}
		}
		
		return null;
	}

	public static User getCurrent() {
		Long userId = getCurrentId();
		if (userId != 0L) {
			return AppLoader.getInstance(UserManager.class).load(userId);
		} else {
			User user = new User();
			user.setId(userId);
			user.setName("Anonymous");
			return user;
		}
	}

	@Override
	public boolean implies(Permission permission) {
		// Root user can do anything
		if (isRoot()) 
			return true;
		
		if (permission instanceof ObjectPermission) {
			ObjectPermission objectPermission = (ObjectPermission) permission;
			if (!isAnonymous()) {
				// One can do anything against its belongings
				if (has(objectPermission.getObject()))
					return true;
				
				for (RepositoryAuthorizationByIndividual authorization: getRepositoryAuthorizations()) {
					ObjectPermission repositoryPermission = new ObjectPermission(
							authorization.getRepository(), authorization.getAuthorizedOperation());
					if (repositoryPermission.implies(objectPermission))
						return true;
				}
				
				for (UserAuthorizationByIndividual each: getUserAuthorizations()) {
					ObjectPermission userPermission = new ObjectPermission(each.getUser(), each.getAuthorizedOperation());
					if (userPermission.implies(objectPermission))
						return true;
				}
				
				for (Team team: getTeams()) {
					if (team.implies(objectPermission))
						return true;
				}
	
				for (RoleMembership each: getRoleMemberships()) {
					if (each.getRole().implies(objectPermission))
						return true;
				}
				
				for (Repository each: Gitop.getInstance(RepositoryManager.class).query(null)) {
					ObjectPermission repositoryPermission = new ObjectPermission(each, each.getDefaultAuthorizedOperation());
					if (repositoryPermission.implies(objectPermission))
						return true;
				}
			} 
			
			// check if is public access
			for (Repository each: Gitop.getInstance(RepositoryManager.class).findPublic()) {
				ObjectPermission repositoryPermission = new ObjectPermission(each, RepositoryOperation.READ);
				if (repositoryPermission.implies(objectPermission))
					return true;
			}
		} 
		return false;
	}
	
	public boolean isRoot() {
		return Gitop.getInstance(UserManager.class).getRootUser().equals(this);
	}

	public boolean isAnonymous() {
		return getId() == 0L;
	}
	
}
