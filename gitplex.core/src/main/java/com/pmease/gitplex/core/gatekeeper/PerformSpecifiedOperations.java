package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidatorContext;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.pmease.commons.validation.ClassValidating;
import com.pmease.commons.validation.Validatable;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.gatekeeper.checkresult.GateCheckResult;

@Editable(order=200, icon="fa-wrench", description=
		"This gatekeeper will be passed if one of specified operations is performed")
@ClassValidating
public class PerformSpecifiedOperations extends AbstractGateKeeper implements Validatable {

	private static final long serialVersionUID = 1L;
	
	private boolean createRef;
	
	private boolean updateRef;
	
	private boolean deleteRef;

	@Editable(order=100, description="Creates a ref such as branch, tag, etc.")
	public boolean isCreateRef() {
		return createRef;
	}

	public void setCreateRef(boolean createRef) {
		this.createRef = createRef;
	}

	@Editable(order=200, description="Updates a ref such as branch, tag, etc.")
	public boolean isUpdateRef() {
		return updateRef;
	}

	public void setUpdateRef(boolean updateRef) {
		this.updateRef = updateRef;
	}

	@Editable(order=300, description="Deletes a ref such as branch, tag, etc.")
	public boolean isDeleteRef() {
		return deleteRef;
	}

	public void setDeleteRef(boolean deleteRef) {
		this.deleteRef = deleteRef;
	}

	private GateCheckResult checkUpdate() {
		if (updateRef) {
			return passed(Lists.newArrayList("Trying to update a ref"));
		} 
		List<String> reasons = new ArrayList<>();
		if (deleteRef) {
			reasons.add("Trying to delete a ref");
		}
		if (createRef) {
			reasons.add("Trying to create a ref");
		}
		return failed(reasons);
	}
	
	@Override
	public GateCheckResult doCheckRequest(PullRequest request) {
		return checkUpdate();
	}

	@Override
	protected GateCheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return checkUpdate();
	}

	@Override
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		if (oldCommit.equals(ObjectId.zeroId())) {
			if (createRef) {
				return passed(Lists.newArrayList("Trying to create a ref"));
			}
			List<String> reasons = new ArrayList<>();
			if (updateRef) {
				reasons.add("Trying to update a ref");
			}
			if (deleteRef) {
				reasons.add("Trying to delete a ref");
			}
			return failed(reasons);
		} else if (newCommit.equals(ObjectId.zeroId())) {
			if (deleteRef) {
				return passed(Lists.newArrayList("Trying to delete a ref"));
			}
			List<String> reasons = new ArrayList<>();
			if (createRef) {
				reasons.add("Trying to create a ref");
			}
			if (updateRef) {
				reasons.add("Trying to update a ref");
			}
			return failed(reasons);
		} else {
			return checkUpdate();
		}
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (!createRef && !updateRef && !deleteRef) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Please select at least one operation").addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}

}
