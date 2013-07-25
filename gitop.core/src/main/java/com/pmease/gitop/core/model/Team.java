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

import org.hibernate.annotations.FetchMode;

import com.pmease.commons.persistence.AbstractEntity;

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
		@UniqueConstraint(columnNames={"owner", "name"})
})
@SuppressWarnings("serial")
public class Team extends AbstractEntity {

	@Column(nullable=false)
	private String name;
	
	private String description;
	
	private List<BranchPermission> branchPermissions = new ArrayList<BranchPermission>();
	
	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	@org.hibernate.annotations.ForeignKey(name="FK_TEAM_OWNER")
	private User owner;

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

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public List<BranchPermission> getBranchPermissions() {
		return branchPermissions;
	}

	public void setBranchPermissions(List<BranchPermission> branchPermissions) {
		this.branchPermissions = branchPermissions;
	}

	/**
	 * Check whether or not a specified branch operation is permitted. 
	 * <p>
	 * Supplied repository name, branch name and the operation will be matched against permission list 
	 * to find the first matching entry. If found, the <code>allow</code> flag of that entry will be returned 
	 * to indicate whether or not the operation is permitted. If no permission entry matches supplied 
	 * parameters, a null value will be returned to indicate that this team can not determine whether 
	 * or not the operation should be permitted.
	 * 
	 * @param repositoryName
	 * 			name of the repository name to operate against
	 * @param branchName
	 *			name of the branch to operate against 		
	 * @param branchOperation
	 * 			operation to be checked
	 * @return
	 * 			true if the operation matches a permissive permission entry,  false if the operation 
	 * 			matches a prohibitory permission entry, null if no permission entry can be matched. 
	 * 			In case of returning null, caller should determine the permission through other means, 
	 * 			such as continue to check other teams, or resort to default permission 
	 */
	public Boolean permits(String repositoryName, String branchName, BranchOperation branchOperation) {
		Boolean permits = null;
		
		for (BranchPermission permission: getBranchPermissions()) {
			permits = permission.permits(repositoryName, branchName, branchOperation);
			if (permits != null)
				return permits;
		}
		
		return permits;
	}
	
}
