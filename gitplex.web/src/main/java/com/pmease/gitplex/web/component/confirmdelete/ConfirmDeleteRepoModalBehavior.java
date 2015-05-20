package com.pmease.gitplex.web.component.confirmdelete;

import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteRepoModalBehavior extends ConfirmDeleteModalBehavior {

	public ConfirmDeleteRepoModalBehavior(ConfirmDeleteRepoModal modalPanel) {
		super(modalPanel);
	}

	@Override
	protected String getWarningMessage() {
		return "Everything inside this repository will be deleted and can not be recovered, "
				+ "please input repository name below to confirm deletion.";
	}

	@Override
	protected String getConfirmInput() {
		return getRepository().getName();
	}

	protected abstract Repository getRepository();
	
}
