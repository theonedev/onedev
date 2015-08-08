package com.pmease.gitplex.core.gatekeeper.helper.pathselection;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.OmitName;
import com.pmease.gitplex.core.editable.PathChoice;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.gatekeeper.IfTouchSpecifiedDirectories;

@SuppressWarnings("serial")
@Editable(name="Specify Directory Paths", order=100)
public class SpecifyTargetPathsByDirectories implements TargetPathSelection {

	private List<String> directories = new ArrayList<>();
	
	@Editable(name="Directories", description="Use comma to separate multiple directories.")
	@PathChoice
	@NotNull
	@Size(min=1, message="At least one directory has to be specified.")
	@OmitName
	public List<String> getDirectories() {
		return directories;
	}

	public void setDirectories(List<String> directories) {
		this.directories = directories;
	}

	@Override
	public GateKeeper getGateKeeper() {
		IfTouchSpecifiedDirectories gateKeeper = new IfTouchSpecifiedDirectories();
		gateKeeper.setDirectories(directories);
		return gateKeeper;
	}

}
