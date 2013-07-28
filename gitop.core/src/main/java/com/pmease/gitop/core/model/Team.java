package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.shiro.authz.Permission;
import org.hibernate.annotations.FetchMode;

import com.google.common.base.Optional;
import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.model.projectpermission.OperationOfRepositorySet;
import com.pmease.gitop.core.model.projectpermission.PrivilegedOperation;
import com.pmease.gitop.core.model.projectpermission.ProjectOperation;
import com.pmease.gitop.core.model.projectpermission.ProjectPermission;
import com.pmease.gitop.core.model.projectpermission.PullFromProject;
import com.pmease.gitop.core.model.projectpermission.WholeProjectOperation;

/**
 * Every user can define its teams to authorize permissions to his/her repositories. 
 * Other users can join the the team to gain access to these repositories.
 *  
 * @author robin
 *
 */
@Entity
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"project", "name"})
})
@SuppressWarnings("serial")
public class Team extends AbstractEntity implements Permission {

	@Column(nullable=false)
	private String name;
	
	private String description;
	
	private Optional<? extends WholeProjectOperation> authorizedWholeProjectAction = Optional.of(new PullFromProject());
	
	private List<OperationOfRepositorySet> authorizedRepositoryActions = new ArrayList<OperationOfRepositorySet>();
	
	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	@org.hibernate.annotations.ForeignKey(name="FK_TEAM_PROJ")
	private User project;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getProject() {
		return project;
	}

	public void setProject(User project) {
		this.project = project;
	}

	public Optional<? extends WholeProjectOperation> getAuthorizedWholeProjectAction() {
		return authorizedWholeProjectAction;
	}

	public void setAuthorizedWholeProjectAction(
			Optional<? extends WholeProjectOperation> authorizedWholeProjectAction) {
		this.authorizedWholeProjectAction = authorizedWholeProjectAction;
	}

	public List<OperationOfRepositorySet> getAuthorizedRepositoryActions() {
		return authorizedRepositoryActions;
	}

	public void setAuthorizedRepositoryActions(
			List<OperationOfRepositorySet> authorizedRepositoryActions) {
		this.authorizedRepositoryActions = authorizedRepositoryActions;
	}

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof ProjectPermission) {
			ProjectPermission projectPermission = (ProjectPermission) permission;
			if (projectPermission.getProject().getId().equals(getProject().getId())) {
				if (getAuthorizedWholeProjectAction().isPresent()) {
					ProjectOperation projectAction = getAuthorizedWholeProjectAction().get();
					if (projectAction.can(projectPermission.getOperation()))
						return true;
				}
				for (PrivilegedOperation action: getAuthorizedRepositoryActions()) {
					if (action.can(projectPermission.getOperation()))
						return true;
				}
				
				return false;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
