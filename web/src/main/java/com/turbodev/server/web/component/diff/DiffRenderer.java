package com.turbodev.server.web.component.diff;

import javax.annotation.Nullable;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.panel.Panel;

import com.turbodev.launcher.loader.ExtensionPoint;
import com.turbodev.server.git.BlobChange;
import com.turbodev.server.web.component.diff.revision.DiffViewMode;

@ExtensionPoint
public interface DiffRenderer {
	
	@Nullable Panel render(String panelId, MediaType mediaType, BlobChange change, DiffViewMode viewMode);
	
}
