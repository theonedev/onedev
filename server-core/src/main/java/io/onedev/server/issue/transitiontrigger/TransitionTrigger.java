package io.onedev.server.issue.transitiontrigger;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import io.onedev.server.util.Usage;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class TransitionTrigger implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public void onRenameState(String oldName, String newName) {
	}
	
	public boolean onDeleteState(String stateName) {
		return false;
	}

	public void onRenameField(String oldName, String newName) {
	}
	
	public boolean onDeleteField(String fieldName) {
		return false;
	}
	
	public boolean onEditFieldValues(String fieldName, ValueSetEdit valueSetEdit) {
		return false;
	}
	
	public void onRenameRole(String oldName, String newName) {
	}

	public Usage onDeleteRole(String roleName) {
		return new Usage();
	}
	
	public Usage onDeleteBranch(String branchName) {
		return new Usage();
	}
	
	public Collection<String> getUndefinedStates() {
		return new HashSet<>();
	}	
	
	public void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
	}
	
	public Collection<String> getUndefinedFields() {
		return new HashSet<>();
	}	
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		return new HashSet<>();
	}
	
}
