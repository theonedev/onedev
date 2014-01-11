package com.pmease.gitop.core.gatekeeper.helper.pathselection;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.editable.DirectoryChoice;
import com.pmease.gitop.core.gatekeeper.IfTouchSpecifiedDirectories;
import com.pmease.gitop.model.gatekeeper.GateKeeper;

@SuppressWarnings("serial")
@Editable(name="Selected Directories", order=100)
public class SpecifyTargetPathsByDirectories implements TargetPathSelection {

	private List<String> directories = new ArrayList<>();
	
	@Editable(name="Directories", description="Use comma to separate multiple directories.")
	@DirectoryChoice
	@NotNull
	@Size(min=1, message="At least one directory has to be specified.")
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
