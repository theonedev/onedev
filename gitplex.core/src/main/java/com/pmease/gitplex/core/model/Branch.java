package com.pmease.gitplex.core.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"repository", "name"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Branch extends AbstractEntity {

	private static final String FQN_SEPARATOR = ":";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Repository repository;

	@ManyToOne(fetch=FetchType.LAZY)
    private User lastUpdater;

	@Column(nullable=false)
	private String name;
	
	private boolean isDefault;
	
    @OneToMany(mappedBy="target")
    private Collection<PullRequest> incomingRequests = new ArrayList<>();

    @OneToMany(mappedBy="source")
    private Collection<PullRequest> outgoingRequests = new ArrayList<>();
    
    @OneToMany(mappedBy="branch", cascade=CascadeType.REMOVE)
    private Collection<BranchWatch> watches = new ArrayList<>();
    
    private String headCommitHash;

    public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public User getLastUpdater() {
		return lastUpdater;
	}

	public void setLastUpdater(User lastUpdater) {
		this.lastUpdater = lastUpdater;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Collection<PullRequest> getIncomingRequests() {
        return incomingRequests;
    }

    public void setIncomingRequests(Collection<PullRequest> incomingRequests) {
        this.incomingRequests = incomingRequests;
    }

    public Collection<PullRequest> getOutgoingRequests() {
        return outgoingRequests;
    }

    public void setOutgoingRequests(Collection<PullRequest> outgoingRequests) {
        this.outgoingRequests = outgoingRequests;
    }
    
	public Collection<BranchWatch> getWatches() {
		return watches;
	}

	public void setWatches(Collection<BranchWatch> watches) {
		this.watches = watches;
	}

	/**
     * Get head commit this branch pointing to.
     * 
     * @return
     * 			head commit of this branch
     */
    public String getHeadCommitHash() {
    	return headCommitHash;
    }

    public void setHeadCommitHash(String headCommitHash) {
    	this.headCommitHash = headCommitHash;
    }
    
    public Commit getHeadCommit() {
    	return getRepository().getCommit(getHeadCommitHash());
    }
    
    public String getHeadRef() {
    	return Git.REFS_HEADS + name; 
    }
    
    public String getFQN() {
    	return getRepository().getFQN() + FQN_SEPARATOR + getName();
    }
    
    public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
     * Convert a git reference name to branch name.
     * 
     * @param refName
     *			name of the git reference 	
     * @return
     * 			name of the branch, or <tt>null</tt> if specified ref
     * 			does not represent a branch
     */ 
    public static @Nullable String parseName(String refName) {
		if (refName.startsWith(Git.REFS_HEADS)) 
			return refName.substring(Git.REFS_HEADS.length());
		else
			return null;
    }
    
    @Override
	public String toString() {
    	return getFQN();
	}

    public static String getNameByFQN(String branchFQN) {
    	return StringUtils.substringAfterLast(branchFQN, FQN_SEPARATOR);
    }
    
    public static String getRepositoryFQNByFQN(String branchFQN) {
    	return StringUtils.substringBeforeLast(branchFQN, FQN_SEPARATOR);
    }
}
