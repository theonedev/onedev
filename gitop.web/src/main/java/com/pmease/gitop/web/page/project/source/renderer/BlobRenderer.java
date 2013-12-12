package com.pmease.gitop.web.page.project.source.renderer;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.page.project.source.GitBlob;

public interface BlobRenderer {
	Panel render(String componentId, IModel<GitBlob> blob);
}
