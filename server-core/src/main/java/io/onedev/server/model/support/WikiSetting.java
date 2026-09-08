package io.onedev.server.model.support;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Path;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

@Editable
public class WikiSetting implements Serializable {
	private static final long serialVersionUID = 1L;
	private String folder;

	@Editable(name="Wiki Folder", placeholder="Inherit from parent", rootPlaceholder="wiki",
			description="Repository folder containing Markdown wiki pages. Leave empty to inherit from parent (defaults to wiki). "
					+ "Use Home.md for the home page and _Sidebar.md for the sidebar")
	@Path(Path.Type.RELATIVE)
	@Nullable
	public String getFolder() {
		return folder;
	}

	public void setFolder(@Nullable String folder) {
		this.folder = folder;
	}
}
