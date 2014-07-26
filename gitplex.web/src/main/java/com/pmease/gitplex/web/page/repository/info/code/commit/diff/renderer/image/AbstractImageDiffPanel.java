package com.pmease.gitplex.web.page.repository.info.code.commit.diff.renderer.image;

import org.apache.wicket.model.IModel;

import com.pmease.gitplex.web.component.repository.RepoAwarePanel;
import com.pmease.gitplex.web.page.repository.info.code.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public class AbstractImageDiffPanel extends RepoAwarePanel {
	
	protected final IModel<FileHeader> fileModel;
	protected final IModel<String> sinceModel;
	protected final IModel<String> untilModel;
	
	public AbstractImageDiffPanel(String id, 
			IModel<FileHeader> fileModel, 
			IModel<String> sinceModel,
			IModel<String> untilModel) {
		
		super(id);
		
		this.fileModel = fileModel;
		this.sinceModel = sinceModel;
		this.untilModel = untilModel;
		
		setOutputMarkupId(true);
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
		if (fileModel != null)
			fileModel.detach();
		
		if (sinceModel != null) 
			sinceModel.detach();
		
		if (untilModel != null) 
			untilModel.detach();
		
		super.onDetach();
	}
}
