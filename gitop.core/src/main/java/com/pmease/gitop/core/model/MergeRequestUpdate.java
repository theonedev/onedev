package com.pmease.gitop.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.FetchMode;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.FindChangedFilesCommand;
import com.pmease.commons.git.Git;
import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.RepositoryManager;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"request", "commit"})
})
public class MergeRequestUpdate extends AbstractEntity implements Comparable<MergeRequestUpdate> {

	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
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
	
	@Override
	public int compareTo(MergeRequestUpdate update) {
		return getId().compareTo(update.getId());
	}

	public Collection<Vote> findVotesOnwards() {
		List<MergeRequestUpdate> updates = new ArrayList<MergeRequestUpdate>(getRequest().getUpdates());
		Collections.sort(updates);
		int index = 0;
		for (int i=0; i<updates.size(); i++) {
			if (updates.get(i).getId().equals(getId())) {
				index = i;
				break;
			}
		}
		
		Collection<Vote> votes = new ArrayList<Vote>();
		
		for (int i=index; i<updates.size(); i++) {
			votes.addAll(updates.get(i).getVotes());
		}
		
		return votes;
	}

	public Collection<String> findTouchedFiles() {
		RepositoryManager repositoryManager = Gitop.getInstance(RepositoryManager.class);
		File repoDir = repositoryManager.locateStorage(getRequest().getDestination().getRepository());
		List<MergeRequestUpdate> updates = new ArrayList<MergeRequestUpdate>(getRequest().getUpdates());
		Collections.sort(updates);
		int index = -1;
		for (int i=0; i<updates.size(); i++) {
			if (updates.get(i).equals(this)) {
				index = i;
				break;
			}
		}

		Preconditions.checkState(index != -1);
		
		if (index == 0)
			return new ArrayList<String>();
		
		FindChangedFilesCommand command = new Git(repoDir).findChangedFiles();
		command.toRev(getRefName());
		command.fromRev(updates.get(index-1).getRefName());
		return command.call();
	}
	
}
