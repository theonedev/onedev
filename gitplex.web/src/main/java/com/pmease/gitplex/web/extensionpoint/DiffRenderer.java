package com.pmease.gitplex.web.extensionpoint;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public interface DiffRenderer {
	Panel render(String panelId, IModel<MediaRenderInfo> originalMediaModel, IModel<MediaRenderInfo> revisedMediaModel);
}
