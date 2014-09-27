package com.pmease.gitplex.web.page.repository.code.commit.diff.renderer.image;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.code.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public class AbstractImageDiffPanel extends Panel {
	
	protected final IModel<Repository> repoModel;
	protected final IModel<FileHeader> fileModel;
	protected final String sinceRevision;
	protected final String untilRevision;
	
	public AbstractImageDiffPanel(String id, 
			IModel<Repository> repoModel,
			IModel<FileHeader> fileModel, 
			String sinceRevision,
			String untilRevision) {
		
		super(id);
		
		this.repoModel = repoModel;
		this.fileModel = fileModel;
		this.sinceRevision = sinceRevision;
		this.untilRevision = untilRevision;
		
		setOutputMarkupId(true);
	}
	
	protected Repository getRepository() {
		return repoModel.getObject();
	}
	
	protected FileHeader getFile() {
		return fileModel.getObject();
	}
	
	@Override
	public void onDetach() {
		repoModel.detach();
		fileModel.detach();
		
		super.onDetach();
	}
}
