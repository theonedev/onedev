package com.pmease.gitplex.web.extensionpoint;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.git.RevAwareChange;
import com.pmease.gitplex.core.model.Repository;

public interface DiffRenderer {
	Panel render(String panelId, IModel<Repository> repoModel, RevAwareChange diffInfo);
}
