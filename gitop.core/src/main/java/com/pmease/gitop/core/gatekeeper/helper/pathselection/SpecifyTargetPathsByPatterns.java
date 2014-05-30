package com.pmease.gitop.core.gatekeeper.helper.pathselection;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.Editable;
import com.pmease.commons.editable.OmitName;
import com.pmease.gitop.core.gatekeeper.IfTouchSpecifiedFilePatterns;
import com.pmease.gitop.model.gatekeeper.GateKeeper;

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
