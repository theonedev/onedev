package com.pmease.gitplex.web.page.repository.info.code.commit.diff.renderer.image;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.google.common.base.Strings;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.info.code.blob.renderer.FileBlobImage;
import com.pmease.gitplex.web.page.repository.info.code.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public class SideBySidePanel extends AbstractImageDiffPanel {
	
	public SideBySidePanel(String id, 
			IModel<Repository> repoModel,
			IModel<FileHeader> model,
			String sinceRevision,
			String untilRevision) {
		super(id, repoModel, model, sinceRevision, untilRevision);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		FileHeader file = getFile();
		
		if (file.getChangeType() == ChangeType.ADD || Strings.isNullOrEmpty(sinceRevision)) {
			add(new WebMarkupContainer("old").setVisibilityAllowed(false));
		} else {
			add(new FileBlobImage("old", 
					getRepository(), 
					sinceRevision,
					file.getOldPath()));
		}
		
		if (file.getChangeType() == ChangeType.DELETE) {
			add(new WebMarkupContainer("new").setVisibilityAllowed(false));
		} else {
			add(new FileBlobImage("new",
					getRepository(),
					untilRevision,
					file.getNewPath()));
		}
	}
}
