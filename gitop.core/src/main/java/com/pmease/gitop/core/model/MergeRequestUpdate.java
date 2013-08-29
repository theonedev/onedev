package com.pmease.gitop.core.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.FetchMode;

import com.pmease.commons.persistence.AbstractEntity;

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

	public String getBranchName() {
		return "refs/updates/" + getId();
	}
	
	@Override
	public int compareTo(MergeRequestUpdate update) {
		return getDate().compareTo(update.getDate());
	}

}
