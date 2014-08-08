package com.pmease.gitplex.web.component.diff;

import java.util.ArrayList;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.Git;
import com.pmease.commons.git.GitText;
import com.pmease.commons.util.Charsets;
import com.pmease.commons.util.MediaTypes;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.gitlink.GitLink;
import com.pmease.gitplex.web.component.symbollink.SymbolLink;
import com.pmease.gitplex.web.component.view.BlobRenderInfo;
import com.pmease.gitplex.web.component.view.BlobViewPanel;
import com.pmease.gitplex.web.extensionpoint.DiffRenderer;
import com.pmease.gitplex.web.extensionpoint.DiffRendererProvider;
import com.pmease.gitplex.web.extensionpoint.TextConverter;
import com.pmease.gitplex.web.extensionpoint.TextConverterProvider;

@SuppressWarnings("serial")
public class BlobDiffPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final BlobDiffInfo diffInfo;
	
	private final IModel<byte[]> oldContentModel;
	
	private final IModel<byte[]> newContentModel;
	
	public BlobDiffPanel(String id, IModel<Repository> repoModel, final BlobDiffInfo diffInfo) {
		super(id);
		
		this.repoModel = repoModel;
		this.diffInfo = diffInfo;
		
		oldContentModel = new LoadableDetachableModel<byte[]>() {

			@Override
			protected byte[] load() {
				Git git = BlobDiffPanel.this.repoModel.getObject().git();
				if (diffInfo.getStatus() == BlobDiffInfo.Status.ADD) 
					return null;
				else 
					return git.read(diffInfo.getOldRevision(), diffInfo.getOldPath(), diffInfo.getOldMode());
				
			}
			
		};
		newContentModel = new LoadableDetachableModel<byte[]>() {

			@Override
			protected byte[] load() {
				Git git = BlobDiffPanel.this.repoModel.getObject().git();
				if (diffInfo.getStatus() == BlobDiffInfo.Status.DELETE) 
					return null;
				else if (diffInfo.getStatus() != BlobDiffInfo.Status.UNCHANGE)
					return git.read(diffInfo.getNewRevision(), diffInfo.getNewPath(), diffInfo.getNewMode());
				else
					return oldContentModel.getObject();
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		int oldBlobType = diffInfo.getOldMode() & FileMode.TYPE_MASK;
		int newBlobType = diffInfo.getNewMode() & FileMode.TYPE_MASK;
		
		if (diffInfo.getStatus() == BlobDiffInfo.Status.UNCHANGE) {
			add(new WebMarkupContainer("originalContent").setVisible(false));
			add(new WebMarkupContainer("revisedContent").setVisible(false));
			Fragment fragment = new Fragment("blobContent", "notChangedFrag", this);
			fragment.add(new Label("title", diffInfo.getOldPath()));
			BlobRenderInfo renderInfo = new BlobRenderInfo(diffInfo.getNewPath(), 
					diffInfo.getNewRevision(), diffInfo.getNewMode());
			fragment.add(new BlobViewPanel("content", repoModel, renderInfo, newContentModel));
			add(fragment);
		} else if (oldBlobType == FileMode.TYPE_GITLINK && newBlobType == FileMode.TYPE_GITLINK) {
			add(new WebMarkupContainer("originalContent").setVisible(false));
			add(new WebMarkupContainer("revisedContent").setVisible(false));

			Fragment fragment = new Fragment("blobContent", "nonFileDiffFrag", this);
			fragment.add(new Label("renamedTitle", diffInfo.getOldPath())
					.setVisible(!diffInfo.getOldPath().equals(diffInfo.getNewPath())));
			fragment.add(new Label("title", diffInfo.getNewPath()));
			fragment.add(new GitLink("oldContent", new String(oldContentModel.getObject())));
			fragment.add(new GitLink("newContent", new String(newContentModel.getObject())));
			add(fragment);
		} else if (oldBlobType == FileMode.TYPE_SYMLINK && newBlobType == FileMode.TYPE_SYMLINK) {
			add(new WebMarkupContainer("originalContent").setVisible(false));
			add(new WebMarkupContainer("revisedContent").setVisible(false));

			Fragment fragment = new Fragment("blobContent", "nonFileDiffFrag", this);
			fragment.add(new Label("renamedTitle", diffInfo.getOldPath())
					.setVisible(!diffInfo.getOldPath().equals(diffInfo.getNewPath())));
			fragment.add(new Label("title", diffInfo.getNewPath()));
			fragment.add(new SymbolLink("oldContent", repoModel, diffInfo.getOldRevision(), 
					diffInfo.getOldPath(), new String(oldContentModel.getObject())));
			fragment.add(new SymbolLink("newContent", repoModel, diffInfo.getNewRevision(), 
					diffInfo.getNewPath(), new String(newContentModel.getObject())));
			add(fragment);
		} else if (oldBlobType == FileMode.TYPE_FILE && newBlobType == FileMode.TYPE_FILE) {
			add(new WebMarkupContainer("originalContent").setVisible(false));
			add(new WebMarkupContainer("revisedContent").setVisible(false));
			
			byte[] oldContent = oldContentModel.getObject();
			byte[] newContent = newContentModel.getObject();
			MediaType oldMediaType = MediaTypes.detectFrom(oldContent, diffInfo.getOldPath());
			MediaType newMediaType = MediaTypes.detectFrom(newContent, diffInfo.getNewPath());
			
			DiffRenderer renderer = null;
			for (DiffRendererProvider provider: GitPlex.getExtensions(DiffRendererProvider.class)) {
				renderer = provider.getDiffRenderer(oldMediaType, newMediaType);
				if (renderer != null)
					break;
			}
			if (renderer != null) {
				add(renderer.render("blobContent", diffInfo, oldContentModel, newContentModel));
			} else {
				Component diffPanel = null;
				if (oldMediaType == newMediaType) {
					TextConverter textConverter = null;
					for (TextConverterProvider provider: GitPlex.getExtensions(TextConverterProvider.class)) {
						textConverter = provider.getTextConverter(oldMediaType);
						if (textConverter != null)
							break;
					}
					if (textConverter != null) {
						GitText originalText = new GitText(textConverter.convert(oldContent), 
								true, Charsets.UTF_8.name());
						GitText revisedText = new GitText(textConverter.convert(newContent), 
								true, Charsets.UTF_8.name());
						diffPanel = new TextDiffPanel("blobContent", diffInfo, originalText, revisedText);
					}
				}
				if (diffPanel == null) {
					if (oldContent.length == 0) {
						if (newContent.length == 0) {
							Fragment fragment = new Fragment("blobContent", "emptyFileFrag", this);
							fragment.add(new FileDiffTitle("summary", diffInfo));
							diffPanel = fragment;
						} else {
							GitText newText = GitText.from(newContent);
							if (newText != null) {
								GitText oldText = new GitText(new ArrayList<String>(), true, newText.getCharset());
								diffPanel = new TextDiffPanel("blobContent", diffInfo, oldText, newText);
							} else {
								Fragment fragment = new Fragment("blobContent", "binaryFileFrag", this);
								fragment.add(new FileDiffTitle("summary", diffInfo));
								diffPanel = fragment;
							}
						}
					} else {
						if (newContent.length == 0) {
							GitText oldText = GitText.from(oldContent);
							if (oldText != null) {
								GitText newText = new GitText(new ArrayList<String>(), true, oldText.getCharset());
								diffPanel = new TextDiffPanel("blobContent", diffInfo, oldText, newText);
							} else {
								Fragment fragment = new Fragment("blobContent", "binaryFileFrag", this);
								fragment.add(new FileDiffTitle("summary", diffInfo));
								diffPanel = fragment;
							}
						} else {
							GitText oldText = GitText.from(oldContent);
							GitText newText = GitText.from(newContent);
							if (oldText != null && newText != null) {
								diffPanel = new TextDiffPanel("blobContent", diffInfo, oldText, newText);
							} else {
								Fragment fragment = new Fragment("blobContent", "binaryFileFrag", this);
								fragment.add(new FileDiffTitle("summary", diffInfo));
								diffPanel = fragment;
							}
						}
					}
				}
				add(diffPanel);
			}
		} else {
			if (oldBlobType == FileMode.TYPE_SYMLINK) {
				add(new SymbolLink("originalContent", repoModel, diffInfo.getOldRevision(), 
						diffInfo.getOldPath(), new String(oldContentModel.getObject())));
			} else if (oldBlobType == FileMode.TYPE_GITLINK) {
				add(new GitLink("originalContent", new String(oldContentModel.getObject())));
			} else if (oldBlobType == FileMode.TYPE_FILE) {
				BlobRenderInfo renderInfo = new BlobRenderInfo(diffInfo.getOldPath(), 
						diffInfo.getOldRevision(), diffInfo.getOldMode());
				add(new BlobViewPanel("originalContent", repoModel, renderInfo, oldContentModel));
			} else {
				add(new WebMarkupContainer("originalContent").setVisible(false));
			}
			add(new Label("originalTitle", diffInfo.getOldPath()));
			
			if (newBlobType == FileMode.TYPE_SYMLINK) {
				add(new SymbolLink("revisedContent", repoModel, diffInfo.getNewRevision(), 
						diffInfo.getNewPath(), new String(newContentModel.getObject())));
			} else if (newBlobType == FileMode.TYPE_GITLINK) {
				add(new GitLink("revisedContent", new String(newContentModel.getObject())));
			} else if (newBlobType == FileMode.TYPE_FILE) {
				BlobRenderInfo renderInfo = new BlobRenderInfo(diffInfo.getNewPath(), 
						diffInfo.getNewRevision(), diffInfo.getNewMode());
				add(new BlobViewPanel("revisedContent", repoModel, renderInfo, newContentModel));
			} else {
				add(new WebMarkupContainer("revisedContent").setVisible(false));
			}
			add(new Label("revisedTitle", diffInfo.getNewPath()));
			
			add(new WebMarkupContainer("blobContent").setVisible(false));
		}		
		
	}
	
	protected void onDetach() {
		repoModel.detach();
		oldContentModel.detach();
		newContentModel.detach();
		
		super.onDetach();
	}

}
