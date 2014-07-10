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
	private User creator;
	
    @OneToMany(mappedBy="target")
    private Collection<PullRequest> incomingRequests = new ArrayList<PullRequest>();

    @OneToMany(mappedBy="source")
    private Collection<PullRequest> outgoingRequests = new ArrayList<PullRequest>();
    
    private transient String headCommit;

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
	 * Get creator of the branch. 
	 * 
	 * @return
	 * 			<tt>null</tt> if creator is unknown
	 */
	@Nullable
	public User getCreator() {
		return creator;
	}

	public void setCreator(@Nullable User creator) {
		this.creator = creator;
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
    
    public String getHeadCommit() {
    	if (headCommit == null) {
	    	Git git = getRepository().git();
	    	headCommit = git.parseRevision(getHeadRef(), true);
    	} 
    	return headCommit;
    }
    
    public String getHeadRef() {
    	return Git.REFS_HEADS + name; 
    }
    
    public String getPathName() {
    	return getRepository().getPathName() + ":" + getName();
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
    public static String getName(String refName) {
		if (refName.startsWith(Git.REFS_HEADS)) 
			return refName.substring(Git.REFS_HEADS.length());
		else
			return null;
    }
    
    @Override
	public String toString() {
    	return getPathName();
	}

}
