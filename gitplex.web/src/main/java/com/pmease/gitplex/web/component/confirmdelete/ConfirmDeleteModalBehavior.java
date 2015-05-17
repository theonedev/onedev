package com.pmease.gitplex.web.component.confirmdelete;

import com.pmease.commons.wicket.behavior.modal.ModalBehavior;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteModalBehavior extends ModalBehavior {

	public ConfirmDeleteModalBehavior(ConfirmDeleteModal modalPanel) {
		super(modalPanel);
	}

	protected abstract String getWarningMessage();
	
	protected abstract String getConfirmInput();
}
