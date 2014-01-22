package com.pmease.gitop.web.page.project.source.commit.diff.renderer.image;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public class AbstractImageDiffPanel extends Panel {
	
	protected final IModel<FileHeader> fileModel;
	protected final IModel<Project> projectModel;
	protected final IModel<String> sinceModel;
	protected final IModel<String> untilModel;
	
	public AbstractImageDiffPanel(String id, 
			IModel<FileHeader> fileModel, 
			IModel<Project> projectModel,
			IModel<String> sinceModel,
			IModel<String> untilModel) {
		
		super(id);
		
		this.fileModel = fileModel;
		this.projectModel = projectModel;
		this.sinceModel = sinceModel;
		this.untilModel = untilModel;
		
		setOutputMarkupId(true);
	}
	
	protected FileHeader getFile() {
		return fileModel.getObject();
	}
	
	protected Project getProject() {
		return projectModel.getObject();
	}
	
	protected String getSince() {
		return sinceModel.getObject();
	}
	
	protected String getUntil() {
		return untilModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (fileModel != null) {
			fileModel.detach();
		}
		
		if (projectModel != null) {
			projectModel.detach();
		}
		
		if (sinceModel != null) {
			sinceModel.detach();
		}
		
		if (untilModel != null) {
			untilModel.detach();
		}
		
		super.onDetach();
	}
}
