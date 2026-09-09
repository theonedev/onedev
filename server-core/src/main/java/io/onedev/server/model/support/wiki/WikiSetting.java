package io.onedev.server.model.support.wiki;

import io.onedev.server.annotation.Editable;

import javax.validation.Valid;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

@Editable
public class WikiSetting implements Serializable {
	private static final long serialVersionUID = 1L;
	private WikiFolder folder;

	@Editable(name="Wiki Folder", placeholder="Inherit from parent", rootPlaceholder="Use folder 'wiki'", description="""
			Specify the repository folder to store wiki pages. Leave empty to inherit from parent.
			If you do not want to store wiki pages in the project repository, the specified folder
			can be a Git submodule""")
	@Valid
	public WikiFolder getFolder() {
		return folder;
	}

	public void setFolder(@Nullable WikiFolder folder) {
		this.folder = folder;
	}
	
}
