package com.pmease.gitplex.web.component.view;

import java.nio.charset.Charset;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.util.Charsets;
import com.pmease.commons.util.MediaTypes;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.gitlink.GitLink;
import com.pmease.gitplex.web.component.symbollink.SymbolLink;
import com.pmease.gitplex.web.extensionpoint.MediaRenderInfo;
import com.pmease.gitplex.web.extensionpoint.MediaRenderer;
import com.pmease.gitplex.web.extensionpoint.MediaRendererProvider;

@SuppressWarnings("serial")
public class BlobViewPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final IModel<BlobRenderInfo> blobModel;
	
	public BlobViewPanel(String id, IModel<Repository> repoModel, IModel<BlobRenderInfo> blobModel) {
		super(id);
	
		this.repoModel = repoModel;
		this.blobModel = blobModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BlobRenderInfo blob = blobModel.getObject();
		if (blob.getMode() == FileMode.TYPE_GITLINK) {
			add(new GitLink("blob", new String(blob.getContent())));
		} else if (blob.getMode() == FileMode.TYPE_SYMLINK) {
			add(new SymbolLink("blob", repoModel, blob.getRevision(), 
					blob.getPath(), new String(blob.getContent())));
		} else if (blob.getContent().length == 0) {
			add(new Label("blob", "<i class='fa fa-info-circle'></i> <em>File is empty</em>").setEscapeModelStrings(false));
		} else {
			final MediaType mediaType = MediaTypes.detectFrom(blob.getContent(), blob.getPath());
			MediaRenderer renderer = null;
			for (MediaRendererProvider provider: GitPlex.getExtensions(MediaRendererProvider.class)) {
				renderer = provider.getMediaRenderer(mediaType);
				if (renderer != null)
					break;
			}
			if (renderer != null) {
				add(renderer.render("blob", new LoadableDetachableModel<MediaRenderInfo>() {

					@Override
					protected MediaRenderInfo load() {
						return MediaRenderInfo.from(blobModel.getObject(), mediaType);
					}
					
				}));
			} else {
				Charset charset = Charsets.detectFrom(blob.getContent());
				if (charset != null) {
					add(new TextViewPanel("blob", Model.of(TextRenderInfo.from(blobModel.getObject(), charset))));
				} else {
					add(new Label("blob", 
							"<i class='fa fa-info-circle'></i> <em>Binary file</em>").setEscapeModelStrings(false));
				}
			}
		}
	}

	@Override
	protected void onDetach() {
		blobModel.detach();
		super.onDetach();
	}

}
