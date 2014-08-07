package com.pmease.gitplex.web.component.diff;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.GitText;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.Charsets;
import com.pmease.commons.util.MediaTypes;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.gitlink.GitLink;
import com.pmease.gitplex.web.component.symbollink.SymbolLink;
import com.pmease.gitplex.web.component.view.BlobRenderInfo;
import com.pmease.gitplex.web.component.view.BlobViewPanel;
import com.pmease.gitplex.web.component.view.TextRenderInfo;
import com.pmease.gitplex.web.extensionpoint.DiffRenderer;
import com.pmease.gitplex.web.extensionpoint.DiffRendererProvider;
import com.pmease.gitplex.web.extensionpoint.MediaRenderInfo;
import com.pmease.gitplex.web.extensionpoint.TextConverter;
import com.pmease.gitplex.web.extensionpoint.TextConverterProvider;

@SuppressWarnings("serial")
public class BlobDiffPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final IModel<BlobDiffInfo> blobDiffModel;
	
	public BlobDiffPanel(String id, IModel<Repository> repoModel, IModel<BlobDiffInfo> blobDiffModel) {
		super(id);
		
		this.repoModel = repoModel;
		this.blobDiffModel = blobDiffModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BlobDiffInfo blobDiff = blobDiffModel.getObject();
		
		WebMarkupContainer blobRenamedContainer = new WebMarkupContainer("blobRenamed");
		blobRenamedContainer.setVisible(blobDiff.getStatus() == BlobDiffInfo.Status.RENAME);
		blobRenamedContainer.add(new Label("oldPath", blobDiff.getOldPath()));
		blobRenamedContainer.add(new Label("newPath", blobDiff.getNewPath()));
		add(blobRenamedContainer);
		
		WebMarkupContainer blobModifiedContainer = new WebMarkupContainer("blobModified");
		blobModifiedContainer.setVisible(blobDiff.getStatus() == BlobDiffInfo.Status.MODIFY);
		blobModifiedContainer.add(new Label("oldPath", blobDiff.getOldPath()));
		add(blobModifiedContainer);
		
		WebMarkupContainer blobAddedContainer = new WebMarkupContainer("blobAdded");
		blobAddedContainer.setVisible(blobDiff.getStatus() == BlobDiffInfo.Status.ADD);
		blobAddedContainer.add(new Label("newPath", blobDiff.getOldPath()));
		add(blobAddedContainer);
		
		WebMarkupContainer blobDeletedContainer = new WebMarkupContainer("blobDeleted");
		blobDeletedContainer.setVisible(blobDiff.getStatus() == BlobDiffInfo.Status.DELETE);
		blobDeletedContainer.add(new Label("oldPath", blobDiff.getOldPath()));
		add(blobDeletedContainer);
		
		WebMarkupContainer blobUnchangedContainer = new WebMarkupContainer("blobUnchanged");
		blobUnchangedContainer.setVisible(blobDiff.getStatus() == BlobDiffInfo.Status.UNCHANGE);
		blobUnchangedContainer.add(new Label("oldPath", blobDiff.getOldPath()));
		add(blobUnchangedContainer);
		
		WebMarkupContainer blobTypeChangedContainer = new WebMarkupContainer("blobTypeChanged");
		int oldBlobType = blobDiff.getOldMode() & FileMode.TYPE_MASK;
		int newBlobType = blobDiff.getNewMode() & FileMode.TYPE_MASK;
		blobTypeChangedContainer.setVisible(blobDiff.getStatus() == BlobDiffInfo.Status.MODIFY 
				&& oldBlobType != newBlobType);
		blobTypeChangedContainer.add(new Label("oldType", GitUtils.getTypeName(oldBlobType)));
		blobTypeChangedContainer.add(new Label("newType", GitUtils.getTypeName(newBlobType)));
		add(blobTypeChangedContainer);
		
		WebMarkupContainer fileModeChangedContainer = new WebMarkupContainer("fileModeChanged");
		fileModeChangedContainer.setVisible(blobDiff.getStatus() == BlobDiffInfo.Status.MODIFY 
				&& oldBlobType == FileMode.TYPE_FILE && newBlobType == FileMode.TYPE_FILE
				&& blobDiff.getOldMode() != blobDiff.getNewMode());
		fileModeChangedContainer.add(new Label("oldMode", Integer.toString(blobDiff.getOldMode(), 8)));
		fileModeChangedContainer.add(new Label("newMode", Integer.toString(blobDiff.getNewMode(), 8)));
		add(fileModeChangedContainer);

		if (blobDiff.getStatus() == BlobDiffInfo.Status.UNCHANGE 
				|| oldBlobType == newBlobType && Arrays.equals(blobDiff.getOldContent(), blobDiff.getNewContent())) {
			add(new WebMarkupContainer("originalContent").setVisible(false));
			add(new WebMarkupContainer("revisedContent").setVisible(false));
			add(new BlobViewPanel("blobContent", repoModel, new LoadableDetachableModel<BlobRenderInfo>(){

				@Override
				protected BlobRenderInfo load() {
					BlobDiffInfo blobDiff = blobDiffModel.getObject();
					return new BlobRenderInfo(blobDiff.getNewPath(), blobDiff.getNewRevision(), 
							blobDiff.getNewMode(), blobDiff.getNewContent());
				}
				
			}));
		} else if (oldBlobType == FileMode.TYPE_FILE && newBlobType == FileMode.TYPE_FILE) {
			add(new WebMarkupContainer("originalContent").setVisible(false));
			add(new WebMarkupContainer("revisedContent").setVisible(false));
			
			final MediaType originalMediaType = MediaTypes.detectFrom(blobDiff.getOldContent(), blobDiff.getOldPath());
			final MediaType revisedMediaType = MediaTypes.detectFrom(blobDiff.getNewContent(), blobDiff.getNewPath());
			
			DiffRenderer renderer = null;
			for (DiffRendererProvider provider: GitPlex.getExtensions(DiffRendererProvider.class)) {
				renderer = provider.getDiffRenderer(originalMediaType, revisedMediaType);
				if (renderer != null)
					break;
			}
			if (renderer != null) {
				add(renderer.render("blobContent", new LoadableDetachableModel<MediaRenderInfo>() {

					@Override
					protected MediaRenderInfo load() {
						BlobDiffInfo blobDiff = blobDiffModel.getObject();
						return new MediaRenderInfo(blobDiff.getOldPath(), blobDiff.getOldRevision(), 
								originalMediaType, blobDiff.getOldContent());
					}
					
				}, new LoadableDetachableModel<MediaRenderInfo>() {

					@Override
					protected MediaRenderInfo load() {
						BlobDiffInfo blobDiff = blobDiffModel.getObject();
						return new MediaRenderInfo(blobDiff.getNewPath(), blobDiff.getNewRevision(), 
								revisedMediaType, blobDiff.getNewContent());
					}
					
				}));
			} else {
				Component diffPanel = null;
				if (originalMediaType == revisedMediaType) {
					TextConverter textConverter = null;
					for (TextConverterProvider provider: GitPlex.getExtensions(TextConverterProvider.class)) {
						textConverter = provider.getTextConverter(originalMediaType);
						if (textConverter != null)
							break;
					}
					if (textConverter != null) {
						GitText originalText = new GitText(textConverter.convert(blobDiff.getOldContent()), true);
						GitText revisedText = new GitText(textConverter.convert(blobDiff.getNewContent()), true);
						TextRenderInfo oldTextRenderInfo = new TextRenderInfo(blobDiff.getOldPath(), 
								blobDiff.getOldRevision(), originalText, Charsets.UTF_8);
						TextRenderInfo newTextRenderInfo = new TextRenderInfo(blobDiff.getNewPath(), 
								blobDiff.getNewRevision(), revisedText, Charsets.UTF_8);
						diffPanel = new TextDiffPanel("blobContent", 
								Model.of(oldTextRenderInfo), Model.of(newTextRenderInfo));
					}
				}
				if (diffPanel == null) {
					if (blobDiff.getOldContent().length == 0) {
						Charset newCharset = Charsets.detectFrom(blobDiff.getNewContent());
						if (newCharset != null) {
							TextRenderInfo oldRenderInfo = new TextRenderInfo(blobDiff.getOldPath(), 
									blobDiff.getOldRevision(), new GitText(new ArrayList<String>(), true), newCharset);
							TextRenderInfo newRenderInfo = new TextRenderInfo(blobDiff.getNewPath(), 
									blobDiff.getNewRevision(), GitText.from(blobDiff.getNewContent(), newCharset), newCharset);
							diffPanel = new TextDiffPanel("blobContent", Model.of(oldRenderInfo), Model.of(newRenderInfo));
						}
					} else {
						Charset oldCharset = Charsets.detectFrom(blobDiff.getOldContent());
						if (blobDiff.getNewContent().length == 0) {
							if (oldCharset != null) {
								TextRenderInfo oldRenderInfo = new TextRenderInfo(blobDiff.getOldPath(), 
										blobDiff.getOldRevision(), GitText.from(blobDiff.getOldContent(), oldCharset), oldCharset);
								TextRenderInfo newRenderInfo = new TextRenderInfo(blobDiff.getNewPath(), 
										blobDiff.getNewRevision(), new GitText(new ArrayList<String>(), true), oldCharset);
								diffPanel = new TextDiffPanel("blobContent", Model.of(oldRenderInfo), Model.of(newRenderInfo));
							}
						} else {
							Charset newCharset = Charsets.detectFrom(blobDiff.getNewContent());
							if (oldCharset != null && newCharset != null) {
								TextRenderInfo oldRenderInfo = new TextRenderInfo(blobDiff.getOldPath(), 
										blobDiff.getOldRevision(), GitText.from(blobDiff.getOldContent(), oldCharset), oldCharset);
								TextRenderInfo newRenderInfo = new TextRenderInfo(blobDiff.getNewPath(), 
										blobDiff.getNewRevision(), GitText.from(blobDiff.getNewContent(), oldCharset), oldCharset);
								diffPanel = new TextDiffPanel("blobContent", Model.of(oldRenderInfo), Model.of(newRenderInfo));
							}
						}
					}
					if (diffPanel == null) {
						diffPanel = new Label("blobContent", 
								"<i class='fa fa-info-circle'></i> <em>Binary files</em>").setEscapeModelStrings(false);
					}
				}
				add(diffPanel);
			}
		} else {
			if (oldBlobType == FileMode.TYPE_SYMLINK) {
				add(new SymbolLink("originalContent", repoModel, blobDiff.getOldRevision(), 
						blobDiff.getOldPath(), new String(blobDiff.getOldContent())));
			} else if (oldBlobType == FileMode.TYPE_GITLINK) {
				add(new GitLink("originalContent", new String(blobDiff.getOldContent())));
			} else if (oldBlobType == FileMode.TYPE_FILE) {
				add(new BlobViewPanel("originalContent", repoModel, new LoadableDetachableModel<BlobRenderInfo>(){

					@Override
					protected BlobRenderInfo load() {
						BlobDiffInfo blobDiff = blobDiffModel.getObject();
						return new BlobRenderInfo(blobDiff.getOldPath(), blobDiff.getOldRevision(), 
								blobDiff.getOldMode(), blobDiff.getOldContent());
					}
					
				}));
			} else {
				add(new WebMarkupContainer("originalContent").setVisible(false));
			}
			
			if (newBlobType == FileMode.TYPE_SYMLINK) {
				add(new SymbolLink("revisedContent", repoModel, blobDiff.getNewRevision(), 
						blobDiff.getNewPath(), new String(blobDiff.getNewContent())));
			} else if (newBlobType == FileMode.TYPE_GITLINK) {
				add(new GitLink("revisedContent", new String(blobDiff.getNewContent())));
			} else if (newBlobType == FileMode.TYPE_FILE) {
				add(new BlobViewPanel("revisedContent", repoModel, new LoadableDetachableModel<BlobRenderInfo>(){

					@Override
					protected BlobRenderInfo load() {
						BlobDiffInfo blobDiff = blobDiffModel.getObject();
						return new BlobRenderInfo(blobDiff.getNewPath(), blobDiff.getNewRevision(), 
								blobDiff.getNewMode(), blobDiff.getNewContent());
					}
					
				}));
			} else {
				add(new WebMarkupContainer("revisedContent").setVisible(false));
			}
			
			add(new WebMarkupContainer("blobContent").setVisible(false));
		}		
		
	}
	
	protected void onDetach() {
		repoModel.detach();
		blobDiffModel.detach();
		
		super.onDetach();
	}

}
