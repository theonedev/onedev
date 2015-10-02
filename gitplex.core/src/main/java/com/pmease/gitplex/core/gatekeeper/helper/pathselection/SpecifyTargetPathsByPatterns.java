package com.pmease.gitplex.core.gatekeeper.helper.pathselection;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.OmitName;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.gatekeeper.IfTouchSpecifiedFilePatterns;

@SuppressWarnings("serial")
@Editable(name="Specify Directory or File Patterns", order=200)
public class SpecifyTargetPathsByPatterns implements TargetPathSelection {

	private String filePatterns;
	
	@Editable(name="File Patterns", description="Specify file patterns to match. Below is some examples:"
			+ "<ul>"
			+ "<li><i>src/*</i>: matches all files directly under src."
			+ "<li><i>src/**</i>: matches all files under src recursively."
			+ "<li><i>**</i>: matches all files."
			+ "<li><i>**/*.c, **/*.java</i>: matches all C and Java files."
			+ "<li><i>-src/**, **</i>: matches all files except those under src."
			+ "</ul>")
	@OmitName
	@NotEmpty
	public String getFilePatterns() {
		return filePatterns;
	}

	public void setFilePatterns(String filePatterns) {
		this.filePatterns = filePatterns;
	}

	@Override
	public GateKeeper getGateKeeper() {
		IfTouchSpecifiedFilePatterns gateKeeper = new IfTouchSpecifiedFilePatterns();
		gateKeeper.setFilePatterns(filePatterns);
		return gateKeeper;
	}

}
