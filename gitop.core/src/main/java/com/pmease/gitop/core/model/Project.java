package com.pmease.gitop.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.ListBranchesCommand;
import com.pmease.commons.git.ListTagsCommand;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.util.GeneralException;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.gatekeeper.AlwaysAccept;
import com.pmease.gitop.core.gatekeeper.GateKeeper;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.StorageManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.core.permission.object.ProtectedObject;
import com.pmease.gitop.core.permission.object.UserBelonging;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.core.validation.ProjectName;

@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"owner", "name"})
})
@SuppressWarnings("serial")
@Editable
public class Project extends AbstractEntity implements UserBelonging {
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private User owner;
	
	private boolean forkable;
	
	@Column()
	private String defaultBranchName;
	
	@ManyToOne
	@JoinColumn(nullable=true)
	private Project forkedFrom;

	@Column(nullable=false)
	private String name;
	
	private String description;

	@Column(nullable=false)
	@Lob
	private GateKeeper gateKeeper = new AlwaysAccept();
	
	@Column(nullable=false)
	private Date createdAt = new Date();

	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<Authorization> authorizations = new ArrayList<Authorization>();

    @OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
    private Collection<Branch> branches = new ArrayList<Branch>();

    @OneToMany(mappedBy="forkedFrom", cascade=CascadeType.REMOVE)
	private Collection<Project> forks = new ArrayList<Project>();

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	@Editable(order=100, description=
			"Specify name of the project. It will be used to identify the project when accessing via Git.")
	@ProjectName
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="Specify description of the project.")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    @Editable(order=450, description="Whether or not this project can be forked.")
    public boolean isForkable() {
        return forkable;
    }

    public void setForkable(boolean forkable) {
        this.forkable = forkable;
    }

    @Editable(
			name="Accept Merge Requests If", order=500,
			description="Optionally define gate keeper to accept merge requests under certain condition.")
	@Valid
	public GateKeeper getGateKeeper() {
		return gateKeeper;
	}

	public void setGateKeeper(GateKeeper gateKeeper) {
		this.gateKeeper = gateKeeper;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getDefaultBranchName() {
        return defaultBranchName;
    }

    public void setDefaultBranchName(String defaultBranchName) {
        this.defaultBranchName = defaultBranchName;
    }

    @Override
	public User getUser() {
		return getOwner();
	}

	public Collection<Authorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<Authorization> authorizations) {
		this.authorizations = authorizations;
	}

    public Project getForkedFrom() {
		return forkedFrom;
	}

	public void setForkedFrom(Project forkedFrom) {
		this.forkedFrom = forkedFrom;
	}

	public Collection<Project> getForks() {
		return forks;
	}

	public void setForks(Collection<Project> forks) {
		this.forks = forks;
	}

	/**
	 * Get branches for this project from database. The result might be 
	 * different from actual branches in repository. To get actual 
	 * branches in repository, call {@link BranchManager#listBranches(Project)} 
	 * instead.
	 * 
	 * @return
	 *         collection of branches available in database for this project 
	 */
	public Collection<Branch> getBranches() {
        return branches;
    }

    public void setBranches(Collection<Branch> branches) {
        this.branches = branches;
    }

    @Override
	public boolean has(ProtectedObject object) {
		if (object instanceof Project) {
			Project project = (Project) object;
			return project.getId().equals(getId());
		} else {
			return false;
		}
	}

	public Collection<User> listAuthorizedUsers(GeneralOperation operation) {
		Set<User> authorizedUsers = new HashSet<User>();
		for (User user: Gitop.getInstance(UserManager.class).query()) {
			if (user.asSubject().isPermitted(new ObjectPermission(this, operation)))
				authorizedUsers.add(user);
		}
		return authorizedUsers;
	}
	
	public Collection<String> listBranches() {
        File codeDir = Gitop.getInstance(StorageManager.class).getStorage(this).ofCode();
        ListBranchesCommand cmd = new ListBranchesCommand(new Git(codeDir));
        
        return cmd.call();
	}
	
	public Collection<String> listTags() {
        File codeDir = Gitop.getInstance(StorageManager.class).getStorage(this).ofCode();
        ListTagsCommand cmd = new ListTagsCommand(new Git(codeDir));
        
        return cmd.call();
	}
	
	public void createBranch(String branchName, String commitHash) {
        File codeDir = Gitop.getInstance(StorageManager.class).getStorage(this).ofCode();
        Git git = new Git(codeDir);
        if (git.listBranches().call().contains(branchName))
        	throw new GeneralException("Branch %s already exists.", branchName);
        
        git.updateRef().refName("refs/heads/" + branchName).revision(commitHash).call();
	}
	
	public void deleteBranch(String branchName) {
        File codeDir = Gitop.getInstance(StorageManager.class).getStorage(this).ofCode();
        Git git = new Git(codeDir);
        git.deleteRef().refName("refs/heads/" + branchName).call();
	}

	public void createTag(String tagName, String commitHash) {
        File codeDir = Gitop.getInstance(StorageManager.class).getStorage(this).ofCode();
        Git git = new Git(codeDir);
        if (git.listTags().call().contains(tagName))
        	throw new GeneralException("Tag %s already exists.", tagName);
        
        git.updateRef().refName("refs/tags/" + tagName).revision(commitHash).call();
	}
	
	public void deleteTag(String tagName) {
        File codeDir = Gitop.getInstance(StorageManager.class).getStorage(this).ofCode();
        Git git = new Git(codeDir);
        git.deleteRef().refName("refs/tags/" + tagName).call();
	}

	@Override
	public String toString() {
		return getOwner() + "/" + getName();
	}
	
}
