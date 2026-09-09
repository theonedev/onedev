package io.onedev.server.model.support.wiki;

import io.onedev.server.annotation.Editable;

import javax.validation.Valid;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

@Editable
public class WikiSetting implements Serializable {
	private static final long serialVersionUID = 1L;
	private WikiFolder folder;

	@Editable(name="Wiki Folder", placeholder="Inherit from parent", rootPlaceholder="Use folder 'wiki'",
			description="Repository root or specified folder containing Markdown wiki pages. Leave empty to inherit from parent (defaults to wiki)")
	@Valid
	public WikiFolder getFolder() {
		return folder;
	}

	public void setFolder(@Nullable WikiFolder folder) {
		this.folder = folder;
	}
	
}
