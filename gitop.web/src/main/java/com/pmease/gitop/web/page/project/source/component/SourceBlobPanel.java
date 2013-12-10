package com.pmease.gitop.web.page.project.source.component;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.quantity.Data;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.page.project.source.GitBlob;
import com.pmease.gitop.web.page.project.source.renderer.BlobRenderer;
import com.pmease.gitop.web.page.project.source.renderer.BlobRendererFactory;

@SuppressWarnings("serial")
public class SourceBlobPanel extends AbstractSourcePagePanel {

	private final IModel<GitBlob> blobModel;
	
	public SourceBlobPanel(String id, IModel<Project> projectModel,
			IModel<String> revisionModel, IModel<List<String>> pathsModel) {
		super(id, projectModel, revisionModel, pathsModel);
		
		blobModel = new LoadableDetachableModel<GitBlob>() {

			@Override
			protected GitBlob load() {
				return GitBlob.of(getProject(), getRevision(), getJoinedPath());
			}
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("mode", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				FileMode mode = getBlob().getMode();
				if (mode == FileMode.SYMLINK) {
					return "symbolic link";
				} else if (mode == FileMode.EXECUTABLE_FILE) {
					return "executable file";
				} else {
					return "file";
				}
			}
			
		}));
		
		add(new Icon("typeicon", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "icon-file-text";
			}
		}));

		add(new Label("size", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return Data.formatBytes(getBlob().getSize(), Data.KB);
			}
			
		}));
		
		BlobRenderer renderer = getRenderer();
		add(renderer.render("body", blobModel));
	}
	
	private BlobRenderer getRenderer() {
		return Gitop.getInstance(BlobRendererFactory.class).create(getBlob());
	}
	
	private GitBlob getBlob() {
		return blobModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (blobModel != null) {
			blobModel.detach();
		}
		
		super.onDetach();
	}
	
}
