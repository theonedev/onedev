package com.pmease.gitop.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.common.base.Objects;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"project", "name"})
})
public class Branch extends AbstractEntity {
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private Project project;

	@Column(nullable=false)
	private String name;

	@OneToMany(mappedBy="to", cascade=CascadeType.REMOVE)
	private Collection<BranchSync> syncFroms = new ArrayList<BranchSync>();

	@OneToMany(mappedBy="from", cascade=CascadeType.REMOVE)
	private Collection<BranchSync> syncTos = new ArrayList<BranchSync>();

    @OneToMany(mappedBy="target", cascade=CascadeType.REMOVE)
    private Collection<PullRequest> ingoingRequests = new ArrayList<PullRequest>();

    @OneToMany(mappedBy="source", cascade=CascadeType.REMOVE)
    private Collection<PullRequest> outgoingRequests = new ArrayList<PullRequest>();
    
    private transient String headCommit;

    public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Collection<BranchSync> getSyncFroms() {
		return syncFroms;
	}

	public void setSyncFroms(Collection<BranchSync> syncFroms) {
		this.syncFroms = syncFroms;
	}

	public Collection<BranchSync> getSyncTos() {
		return syncTos;
	}

	public void setSyncTos(Collection<BranchSync> syncTos) {
		this.syncTos = syncTos;
	}

	public Collection<PullRequest> getIngoingRequests() {
        return ingoingRequests;
    }

    public void setIngoingRequests(Collection<PullRequest> ingoingRequests) {
        this.ingoingRequests = ingoingRequests;
    }

    public Collection<PullRequest> getOutgoingRequests() {
        return outgoingRequests;
    }

    public void setOutgoingRequests(Collection<PullRequest> outgoingRequests) {
        this.outgoingRequests = outgoingRequests;
    }
    
    public String getHeadCommit() {
    	if (headCommit == null) {
	    	Git git = getProject().code();
	    	headCommit = git.resolveRef(getHeadRef(), true);
    	} 
    	return headCommit;
    }
    
    public String getHeadRef() {
    	return "refs/heads/" + name; 
    }

    @Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("name", getName())
				.add("project", getProject())
				.toString();
	}
}
