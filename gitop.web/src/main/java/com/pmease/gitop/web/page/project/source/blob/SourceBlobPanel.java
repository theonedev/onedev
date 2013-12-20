package com.pmease.gitop.web.page.project.source.blob;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.web.common.quantity.Data;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.page.project.source.blob.renderer.BlobRenderer;
import com.pmease.gitop.web.page.project.source.blob.renderer.BlobRendererFactory;
import com.pmease.gitop.web.service.FileBlob;

@SuppressWarnings("serial")
public class SourceBlobPanel extends Panel {

	public SourceBlobPanel(String id, IModel<FileBlob> blobModel) {
		super(id, blobModel);
	}

	@SuppressWarnings("unchecked")
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
		
		add(new Label("loc", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getBlob().getLines().size();
			}
			
		}).setVisibilityAllowed(getBlob().isText()));
		
		add(new Label("sloc", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				List<String> lines = getBlob().getLines();
				int sloc = 0;
				for (String each : lines) {
					if (!StringUtils.isBlank(each)) {
						sloc++;
					}
				}
				
				return sloc;
			}
			
		}));
		
		BlobRenderer renderer = getRenderer();
		add(renderer.render("body", (IModel<FileBlob>) getDefaultModel()));
	}
	
	private BlobRenderer getRenderer() {
		return Gitop.getInstance(BlobRendererFactory.class).newRenderer(getBlob());
	}
	
	private FileBlob getBlob() {
		return (FileBlob) getDefaultModelObject();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
}
