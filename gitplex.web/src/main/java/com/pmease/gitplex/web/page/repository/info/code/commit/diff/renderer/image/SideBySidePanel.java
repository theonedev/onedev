package com.pmease.gitplex.web.page.repository.info.code.commit.diff.renderer.image;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.google.common.base.Strings;
import com.pmease.gitplex.web.page.repository.info.code.blob.renderer.FileBlobImage;
import com.pmease.gitplex.web.page.repository.info.code.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public class SideBySidePanel extends AbstractImageDiffPanel {
	
	public SideBySidePanel(String id, IModel<FileHeader> model,
			IModel<String> sinceModel,
			IModel<String> untilModel) {
		super(id, model, sinceModel, untilModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		FileHeader file = getFile();
		String until = getUntil();
		String since = getSince();
		
		if (file.getChangeType() == ChangeType.ADD || Strings.isNullOrEmpty(since)) {
			add(new WebMarkupContainer("old").setVisibilityAllowed(false));
		} else {
			add(new FileBlobImage("old", 
					getRepository(), 
					since,
					file.getOldPath()));
		}
		
		if (file.getChangeType() == ChangeType.DELETE) {
			add(new WebMarkupContainer("new").setVisibilityAllowed(false));
		} else {
			add(new FileBlobImage("new",
					getRepository(),
					until,
					file.getNewPath()));
		}
	}
}
