package io.onedev.server.web.page.project.setting.issueworkflow.fields;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
public class ShowConditionListWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<ShowConditionOuterWrapper> outerWrappers = new ArrayList<>();

	@Editable
	@Size(min=1, message="At least one condition needs to be specified")
	@OmitName
	public List<ShowConditionOuterWrapper> getOuterWrappers() {
		return outerWrappers;
	}

	public void setOuterWrappers(List<ShowConditionOuterWrapper> outerWrappers) {
		this.outerWrappers = outerWrappers;
	}

}
