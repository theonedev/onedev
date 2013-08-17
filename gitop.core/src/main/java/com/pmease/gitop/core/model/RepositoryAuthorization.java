package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.FetchMode;

import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.model.permission.BranchPermission;
import com.pmease.gitop.core.model.permission.object.ProtectedBranches;
import com.pmease.gitop.core.model.permission.object.ProtectedObject;
import com.pmease.gitop.core.model.permission.operation.PrivilegedOperation;
import com.pmease.gitop.core.model.permission.operation.Read;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"subject", "object"})
})
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class RepositoryAuthorization extends AbstractEntity {

	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	private Team team;	

	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	private Repository repository;
	
	private PrivilegedOperation operation = new Read();
	
	private List<BranchPermission> branchPermissions = new ArrayList<BranchPermission>();

	public PrivilegedOperation getOperation() {
		return operation;
	}

	public void setOperation(PrivilegedOperation operation) {
		this.operation = operation;
	}

	public List<BranchPermission> getBranchPermissions() {
		return branchPermissions;
	}

	public void setBranchPermissions(List<BranchPermission> branchPermissions) {
		this.branchPermissions = branchPermissions;
	}

	public PrivilegedOperation operationOf(ProtectedObject object) {
		for (BranchPermission each: getBranchPermissions()) {
			if (new ProtectedBranches(repository, each.getBranchNames()).has(object))
				return each.getBranchOperation();
		}
		
		if (getRepository().has(object))
			return getOperation();
		else
			return null;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}
	
	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
