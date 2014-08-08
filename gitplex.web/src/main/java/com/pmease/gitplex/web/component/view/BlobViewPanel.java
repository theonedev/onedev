package com.pmease.gitplex.web.component.view;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.GitText;
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
	
	private final BlobRenderInfo blobInfo;
	
	private final IModel<byte[]> blobContentModel;
	
	public BlobViewPanel(String id, IModel<Repository> repoModel, BlobRenderInfo blobInfo, 
			IModel<byte[]> blobContentModel) {
		super(id);
	
		this.repoModel = repoModel;
		this.blobInfo = blobInfo;
		this.blobContentModel = blobContentModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		byte[] content = blobContentModel.getObject();
		if (blobInfo.getMode() == FileMode.TYPE_GITLINK) {
			add(new GitLink("blob", new String(content)));
		} else if (blobInfo.getMode() == FileMode.TYPE_SYMLINK) {
			add(new SymbolLink("blob", repoModel, blobInfo.getRevision(), 
					blobInfo.getPath(), new String(content)));
		} else if (blobContentModel.getObject().length == 0) {
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
				add(renderer.render("blob", blobInfo, blobContentModel));
			} else {
				GitText text = GitText.from(content);
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
		blobContentModel.detach();
		super.onDetach();
	}

}
