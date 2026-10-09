package io.onedev.server.model.support.wiki;

import io.onedev.server.annotation.Editable;

import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

@Editable
public class WikiSetting implements Serializable {
	private static final long serialVersionUID = 1L;
	private WikiFolder folder;

	@Editable(name="Wiki Folder", placeholder="Inherit from parent", topPlaceholder="Use folder 'wiki'", description="""
			Specify the repository folder to store wiki pages.
			If you do not want to store wiki pages in the project repository, the specified folder
			can be a Git submodule. To exclude files from the wiki Pages panel, add a <code>.wikiignore</code>
			file in the wiki root folder with Git ignore patterns relative to that folder""")
	@Valid
	public WikiFolder getFolder() {
		return folder;
	}

	public void setFolder(@Nullable WikiFolder folder) {
		this.folder = folder;
	}
	
}
