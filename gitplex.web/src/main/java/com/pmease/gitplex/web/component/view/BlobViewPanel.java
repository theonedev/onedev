package com.pmease.gitplex.web.component.view;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.BlobText;
import com.pmease.commons.util.MediaTypes;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.gitlink.GitLink;
import com.pmease.gitplex.web.component.symbollink.SymbolLink;
import com.pmease.gitplex.web.extensionpoint.MediaRenderer;
import com.pmease.gitplex.web.extensionpoint.MediaRendererProvider;

@SuppressWarnings("serial")
public class BlobViewPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final BlobInfo blobInfo;
	
	public BlobViewPanel(String id, IModel<Repository> repoModel, BlobInfo blobInfo) {
		super(id);
	
		this.repoModel = repoModel;
		this.blobInfo = blobInfo;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		byte[] content = repoModel.getObject().getBlobContent(blobInfo);
		if (blobInfo.getMode() == FileMode.TYPE_GITLINK) {
			add(new GitLink("blob", new String(content)));
		} else if (blobInfo.getMode() == FileMode.TYPE_SYMLINK) {
			add(new SymbolLink("blob", repoModel, blobInfo.getRevision(), 
					blobInfo.getPath(), new String(content)));
		} else if (content.length == 0) {
			add(new Label("blob", "<i class='fa fa-info-circle'></i> <em>File is empty</em>").setEscapeModelStrings(false));
		} else {
			final MediaType mediaType = MediaTypes.detectFrom(content, blobInfo.getPath());
			MediaRenderer renderer = null;
			for (MediaRendererProvider provider: GitPlex.getExtensions(MediaRendererProvider.class)) {
				renderer = provider.getMediaRenderer(mediaType);
				if (renderer != null)
					break;
			}
			if (renderer != null) {
				add(renderer.render("blob", repoModel, blobInfo));
			} else {
				BlobText text = repoModel.getObject().getBlobText(blobInfo);
				if (text != null) {
					add(new TextViewPanel("blob", blobInfo, text));
				} else {
					add(new Label("blob", 
							"<i class='fa fa-info-circle'></i> <em>Binary file</em>").setEscapeModelStrings(false));
				}
			}
		}
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		super.onDetach();
	}

}
