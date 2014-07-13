package com.pmease.gitop.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"repository", "name"})
})
public class Branch extends AbstractEntity {
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private Repository repository;

	@Column(nullable=false)
	private String name;
	
	@ManyToOne
	private User updater;
	
    @OneToMany(mappedBy="target")
    private Collection<PullRequest> incomingRequests = new ArrayList<PullRequest>();

    @OneToMany(mappedBy="source")
    private Collection<PullRequest> outgoingRequests = new ArrayList<PullRequest>();
    
    private String headCommit;

    public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get user who updates this branch ref last time.
	 * 
	 * @return
	 * 			<tt>null</tt> if updater is unknown
	 */
	@Nullable
	public User getUpdater() {
		return updater;
	}

	public void setUpdater(User updater) {
		this.updater = updater;
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
    
    /**
     * Get head commit this branch pointing to.
     * 
     * @return
     * 			head commit of this branch
     */
    public String getHeadCommit() {
    	return headCommit;
    }

    public void setHeadCommit(String headCommit) {
    	this.headCommit = headCommit;
    }
    
    public String getHeadRef() {
    	return Git.REFS_HEADS + name; 
    }
    
    public String getFullName() {
    	return getRepository().getFullName() + ":" + getName();
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
    	return getFullName();
	}

}
