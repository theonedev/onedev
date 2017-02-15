package com.gitplex.server.web.component.diff;

import javax.annotation.Nullable;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.launcher.loader.ExtensionPoint;
import com.gitplex.server.git.BlobChange;

@ExtensionPoint
public interface DiffRenderer {
	@Nullable Panel render(String panelId, MediaType mediaType, BlobChange change);
}
