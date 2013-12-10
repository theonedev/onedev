package com.pmease.gitop.web.page.project.source.component;

import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.util.UrlUtils;

public class AbstractSourcePagePanel extends Panel {
	private static final long serialVersionUID = 1L;

	protected final IModel<String> revisionModel;
	protected final IModel<List<String>> pathsModel;
	
	public AbstractSourcePagePanel(String id,
			IModel<Project> projectModel,
			IModel<String> revisionModel,
			IModel<List<String>> pathsModel) {
		super(id, projectModel);
		
		this.revisionModel = revisionModel;
		this.pathsModel = pathsModel;
	}

	protected Project getProject() {
		return (Project) getDefaultModelObject();
	}
	
	protected String getRevision() {
		return revisionModel.getObject();
	}
	
	protected List<String> getPaths() {
		return pathsModel.getObject();
	}
	
	protected String getJoinedPath() {
		if (getPaths().isEmpty()) {
			return null;
		} else {
			return UrlUtils.concatSegments(getPaths());
		}
	}
	
	@Override
	public void onDetach() {
		if (revisionModel != null) {
			revisionModel.detach();
		}
		
		if (pathsModel!=null) {
			pathsModel.detach();
		}
		
		super.onDetach();
	}
}
