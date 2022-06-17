package io.onedev.server.web.component.diff.revision;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class DiffOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private DiffViewMode viewMode = DiffViewMode.UNIFIED;
	
	private WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;

	@Editable(order=100)
	@NotNull
	public DiffViewMode getViewMode() {
		return viewMode;
	}

	public void setViewMode(DiffViewMode viewMode) {
		this.viewMode = viewMode;
	}

	@Editable(order=200)
	@NotNull
	public WhitespaceOption getWhitespaceOption() {
		return whitespaceOption;
	}

	public void setWhitespaceOption(WhitespaceOption whitespaceOption) {
		this.whitespaceOption = whitespaceOption;
	}
	
}
