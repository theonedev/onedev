package com.gitplex.server.web.component.depotfile.filelist;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.server.web.page.base.BaseDependentResourceReference;

public class FileListResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public FileListResourceReference() {
		super(FileListResourceReference.class, "file-list.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(FileListResourceReference.class, "file-list.css")));
		return dependencies;
	}

}
