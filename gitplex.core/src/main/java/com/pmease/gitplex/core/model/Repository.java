package com.pmease.gitplex.core.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
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
import javax.validation.constraints.NotNull;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.gatekeeper.AndGateKeeper;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.permission.object.ProtectedObject;
import com.pmease.gitplex.core.permission.object.UserBelonging;
import com.pmease.gitplex.core.validation.RepositoryName;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.FileUtils;

@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"owner", "name"}), 
		@UniqueConstraint(columnNames={"owner", "forkedFrom"})
})
@SuppressWarnings("serial")
@Editable
public class Repository extends AbstractEntity implements UserBelonging {

	public static final String REFS_GITPLEX = "refs/gitplex/";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private User owner;

	private boolean forkable = true;
	
	@ManyToOne
	@JoinColumn(nullable=true)
	private Repository forkedFrom;

	@Column(nullable=false)
	private String name;
	
	private String description;
	
	private transient String defaultBranch;

	@Lob
	@Column(nullable=false)
	private ArrayList<GateKeeper> gateKeepers = new ArrayList<>();
	
	@Lob
	@Column(nullable=false)
	private IntegrationSetting integrationSetting = new IntegrationSetting();
	
	@Column(nullable=false)
	private Date createdAt = new Date();

	@OneToMany(mappedBy="repository", cascade=CascadeType.REMOVE)
	private Collection<Authorization> authorizations = new ArrayList<>();

	@OneToMany(mappedBy="repository", cascade=CascadeType.REMOVE)
	private Collection<CommitComment> commitComments = new ArrayList<>();

	@OneToMany(mappedBy="repository", cascade=CascadeType.REMOVE)
    private Collection<Branch> branches = new ArrayList<>();

    @OneToMany(mappedBy="forkedFrom")
	private Collection<Repository> forks = new ArrayList<>();
    
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	@Editable(order=100, description=
			"Specify name of the repository. It will be used to identify the repository when accessing via Git.")
	@RepositoryName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="Specify description of the repository.")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    @Editable(order=450, description="Whether or not this repository can be forked.")
    public boolean isForkable() {
        return forkable;
    }

    public void setForkable(boolean forkable) {
        this.forkable = forkable;
    }

    @NotNull
	@Valid
	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}

	public void setGateKeepers(ArrayList<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	@NotNull
	@Valid
	public IntegrationSetting getIntegrationSetting() {
		return integrationSetting;
	}

	public void setIntegrationSetting(IntegrationSetting integrationSetting) {
		this.integrationSetting = integrationSetting;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
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

    public Collection<CommitComment> getCommitComments() {
		return commitComments;
	}

	public void setCommitComments(Collection<CommitComment> commitComments) {
		this.commitComments = commitComments;
	}

	public Repository getForkedFrom() {
		return forkedFrom;
	}

	public void setForkedFrom(Repository forkedFrom) {
		this.forkedFrom = forkedFrom;
	}

	public Collection<Repository> getForks() {
		return forks;
	}

	public void setForks(Collection<Repository> forks) {
		this.forks = forks;
	}

	/**
	 * Get branches for this repository from database. The result might be 
	 * different from actual branches in repository. To get actual 
	 * branches in repository, call {@link BranchManager#listBranches(Repository)} 
	 * instead.
	 * 
	 * @return
	 *         collection of branches available in database for this repository 
	 */
	public Collection<Branch> getBranches() {
        return branches;
    }

    public void setBranches(Collection<Branch> branches) {
        this.branches = branches;
    }

    @Override
	public boolean has(ProtectedObject object) {
		if (object instanceof Repository) {
			Repository repository = (Repository) object;
			return repository.getId().equals(getId());
		} else {
			return false;
		}
	}

	public String getFullName() {
		return getOwner().getName() + "/" + getName();
	}
	
	@Override
	public String toString() {
		return getFullName();
	}
	
	public Git git() {
		return new Git(AppLoader.getInstance(StorageManager.class).getStorage(this));
	}
	
	/**
	 * Whether or not specified git represents a valid repository git. This can be used to tell 
	 * apart a Gitop repository git from some other Git repositories.
	 * 
	 * @return
	 * 			<tt>true</tt> if valid; <tt>false</tt> otherwise
	 */
	public static boolean isValid(Git git) {
        File updateHook = new File(git.repoDir(), "hooks/update");
        if (!updateHook.exists()) 
        	return false;
        
        try {
			String content = FileUtils.readFileToString(updateHook);
			return content.contains("GITOP_USER_ID");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public GateKeeper getGateKeeper() {
		AndGateKeeper andGateKeeper = new AndGateKeeper();
		for (GateKeeper each: getGateKeepers())
			andGateKeeper.getGateKeepers().add(each);
		return andGateKeeper;
	}

	/**
	 * Find fork root of this repository. 
	 * 
	 * @return
	 * 			fork root of this repository, or <tt>null</tt> if the repository is not 
	 * 			forked from any other repository  
	 */
	public @Nullable Repository findForkRoot() {
		if (forkedFrom != null) {
			Repository forkedRoot = forkedFrom.findForkRoot();
			if (forkedRoot != null)
				return forkedRoot;
			else
				return forkedFrom;
		} else {
			return null;
		}
	}
	
	/**
	 * Find all descendant repositories forking from current repository.
	 * 
	 * @return
	 * 			all descendant repositories forking from current repository
	 */
	public List<Repository> findForkDescendants() {
		List<Repository> descendants = new ArrayList<>();
		for (Repository fork: getForks()) { 
			descendants.add(fork);
			descendants.addAll(fork.findForkDescendants());
		}
		
		return descendants;
	}
	
	/**
	 * Find all comparable repositories of current repository. Comparable repositories can 
	 * be connected via forks, and can be compared/pulled. 
	 * 
	 * @return
	 * 			comparable repositories of current repository, with current repository also 
	 * 			included in the collection
	 */
	public List<Repository> findComparables() {
		List<Repository> comparables = new ArrayList<Repository>();
		Repository forkRoot = findForkRoot();
		if (forkRoot != null) {
			comparables.add(forkRoot);
			comparables.addAll(forkRoot.findForkDescendants());
		} else {
			comparables.add(this);
			comparables.addAll(findForkDescendants());
		}
		return comparables;
	}

	public String getUrl() {
		return GitPlex.getInstance().getServerUrl() + "/" + getFullName();
	}
	
	public String getDefaultBranch() {
		if (defaultBranch == null)
			defaultBranch = git().resolveDefaultBranch();
		return defaultBranch;
	}
	
	public String defaultBranchIfNull(@Nullable String revision) {
		if (revision != null)
			return revision;
		else
			return getDefaultBranch();
	}
	
}
