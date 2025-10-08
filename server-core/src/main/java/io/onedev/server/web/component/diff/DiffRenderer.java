package io.onedev.server.web.component.diff;

import org.jspecify.annotations.Nullable;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.server.git.BlobChange;
import io.onedev.server.web.component.diff.revision.DiffViewMode;

@ExtensionPoint
public interface DiffRenderer {
	
	@Nullable Panel render(String panelId, MediaType mediaType, BlobChange change, DiffViewMode viewMode);
	
}
