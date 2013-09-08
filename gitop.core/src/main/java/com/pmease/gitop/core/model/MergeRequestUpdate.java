package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.pmease.commons.persistence.AbstractEntity;

@SuppressWarnings("serial")
@Entity
public class MergeRequestUpdate extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private MergeRequest request;
	
	private Date date;

	@OneToMany(mappedBy="update")
	private Collection<Vote> votes = new ArrayList<Vote>();
	
	public MergeRequest getRequest() {
		return request;
	}

	public void setRequest(MergeRequest request) {
		this.request = request;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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
