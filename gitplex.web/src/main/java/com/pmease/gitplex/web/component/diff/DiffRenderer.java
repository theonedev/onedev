package com.pmease.gitplex.web.component.diff;

import javax.annotation.Nullable;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.git.BlobChange;
import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface DiffRenderer {
	@Nullable Panel render(String panelId, MediaType mediaType, BlobChange change);
}
