package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
public class MergeRequestUpdate extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private MergeRequest request;
	
	@Column(nullable=false)
	private String subject;
	
	private Date date = new Date();
	
	@Column(nullable=false)
	private String commitHash;

	@OneToMany(mappedBy="update", cascade=CascadeType.REMOVE)
	private Collection<Vote> votes = new ArrayList<Vote>();
	
	public MergeRequest getRequest() {
		return request;
	}

	public void setRequest(MergeRequest request) {
		this.request = request;
	}

	public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    public Collection<Vote> getVotes() {
		return votes;
	}

	public void setVotes(Collection<Vote> votes) {
		this.votes = votes;
	}

	public String getRefName() {
		return "refs/updates/" + getId();
	}
	
	/**
	 * List votes against this update and all subsequent updates.
	 * <p>
	 * @return
	 * 			list of found votes, ordered by associated updates reversely
	 */
	public List<Vote> listVotesOnwards() {
		List<Vote> votes = new ArrayList<Vote>();
		
		List<MergeRequestUpdate> updates = getRequest().getEffectiveUpdates();
		for (Iterator<MergeRequestUpdate> it = updates.iterator(); it.hasNext();) {
			MergeRequestUpdate update = it.next();
			votes.addAll(update.getVotes());
			if (update.equals(this)) {
				break;
			}
		}
		
		return votes;
	}

}
