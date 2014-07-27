package com.pmease.gitplex.web.page.repository.info.code.commit.diff.renderer.image;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.info.code.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public class AbstractImageDiffPanel extends Panel {
	
	protected final IModel<Repository> repoModel;
	protected final IModel<FileHeader> fileModel;
	protected final IModel<String> sinceModel;
	protected final IModel<String> untilModel;
	
	public AbstractImageDiffPanel(String id, 
			IModel<Repository> repoModel,
			IModel<FileHeader> fileModel, 
			IModel<String> sinceModel,
			IModel<String> untilModel) {
		
		super(id);
		
		this.repoModel = repoModel;
		this.fileModel = fileModel;
		this.sinceModel = sinceModel;
		this.untilModel = untilModel;
		
		setOutputMarkupId(true);
	}
	
	protected Repository getRepository() {
		return repoModel.getObject();
	}
	
	protected FileHeader getFile() {
		return fileModel.getObject();
	}
	
	protected String getSince() {
		return sinceModel.getObject();
	}
	
	protected String getUntil() {
		return untilModel.getObject();
	}
	
	@Override
	public void onDetach() {
		repoModel.detach();
		fileModel.detach();
		sinceModel.detach();
		untilModel.detach();
		
		super.onDetach();
	}
}
