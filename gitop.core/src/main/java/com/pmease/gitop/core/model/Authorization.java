package com.pmease.gitop.core.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitop.core.permission.operation.GeneralOperation;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"team", "project"})
})
public class Authorization extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private Team team;	

	@ManyToOne
	@JoinColumn(nullable=false)
	private Project project;
	
	private GeneralOperation repoPermission = GeneralOperation.READ;
	
	public GeneralOperation getRepoPermission() {
		return repoPermission;
	}

	public void setRepoPermission(GeneralOperation repoPermission) {
		this.repoPermission = repoPermission;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

}
