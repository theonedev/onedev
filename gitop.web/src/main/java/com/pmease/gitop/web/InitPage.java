package com.pmease.gitop.web;

import org.apache.wicket.RestartResponseException;

import com.pmease.commons.util.init.InitStage;
import com.pmease.gitop.core.Gitop;

@SuppressWarnings("serial")
public class InitPage extends BasePage {

	public InitPage() {
		InitStage initStage = Gitop.getInstance().getInitStage();
		if (initStage == null)
			throw new RestartResponseException(HomePage.class);
	}
	
	@Override
	protected String getTitle() {
		return "Initialization";
	}

}
