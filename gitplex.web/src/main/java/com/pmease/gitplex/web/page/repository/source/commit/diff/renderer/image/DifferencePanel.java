package com.pmease.gitplex.web.page.repository.source.commit.diff.renderer.image;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.google.common.base.Strings;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.source.blob.renderer.FileBlobImage;
import com.pmease.gitplex.web.page.repository.source.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public class DifferencePanel extends AbstractImageDiffPanel {

	public DifferencePanel(String id, IModel<FileHeader> fileModel,
			IModel<Repository> repositoryModel, IModel<String> sinceModel,
			IModel<String> untilModel) {
		
		super(id, fileModel, repositoryModel, sinceModel, untilModel);
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

		differenceControl = new WebMarkupContainer("differenceControl");
		add(differenceControl);
	}
	
	private WebMarkupContainer differenceControl;
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(ResembleResourceReference.getInstance()));
	}
}
