package com.pmease.gitplex.web.component.diff;

import java.util.Arrays;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.gitlink.GitLink;
import com.pmease.gitplex.web.component.symbollink.SymbolLink;

@SuppressWarnings("serial")
public class BlobDiffPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final IModel<BlobInfo> originalBlobModel;
	
	private final IModel<BlobInfo> revisedBlobModel;
	
	public BlobDiffPanel(String id, IModel<Repository> repoModel, 
			IModel<BlobInfo> originalBlobModel, IModel<BlobInfo> revisedBlobModel) {
		super(id);
		
		this.repoModel = repoModel;
		this.originalBlobModel = originalBlobModel;
		this.revisedBlobModel = revisedBlobModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BlobInfo originalBlob = originalBlobModel.getObject();
		BlobInfo revisedBlob = revisedBlobModel.getObject();
		
		Fragment fragment;
		if (originalBlob == null) {
			fragment = new Fragment("content", "addedFrag", this);
			if (revisedBlob.getMode() == FileMode.TYPE_GITLINK) {
				fragment.add(new Label("type", "Sub module "));
				GitLink link = new GitLink("blobContent", new String(revisedBlob.getContent()));
				link.add(AttributeAppender.append("class", "revised"));
				fragment.add(link);
			} else if (revisedBlob.getMode() == FileMode.TYPE_SYMLINK) {
				fragment.add(new Label("type", "Symbol link "));
				SymbolLink link = new SymbolLink("blobContent", repoModel, revisedBlob.getRevision(), 
						revisedBlob.getPath(), new String(revisedBlob.getContent())); 
				link.add(AttributeAppender.append("class", "revised"));
				fragment.add(link);
			} else {
				fragment.add(new Label("type"));
				fragment.add(new FileDiffPanel("blobContent", new AbstractReadOnlyModel<byte[]>() {

					@Override
					public byte[] getObject() {
						return new byte[0];
					}
					
				}, new AbstractReadOnlyModel<byte[]>() {

					@Override
					public byte[] getObject() {
						return revisedBlobModel.getObject().getContent();
					}
					
				}));
			}

			fragment.add(new Label("path", revisedBlob.getPath()));
		} else if (revisedBlob == null) {
			fragment = new Fragment("content", "deletedFrag", this);
			if (originalBlob.getMode() == FileMode.TYPE_GITLINK) {
				fragment.add(new Label("type", "Sub module "));
				GitLink link = new GitLink("blobContent", new String(originalBlob.getContent()));
				link.add(AttributeAppender.append("class", "original"));
				fragment.add(link);
			} else if (originalBlob.getMode() == FileMode.TYPE_SYMLINK) {
				fragment.add(new Label("type", "Symbol link "));
				SymbolLink link = new SymbolLink("blobContent", repoModel, originalBlob.getRevision(), 
						originalBlob.getPath(), new String(originalBlob.getContent()));
				link.add(AttributeAppender.append("class", "original"));
				fragment.add(link);
			} else {
				fragment.add(new Label("type"));
				fragment.add(new FileDiffPanel("blobContent", new AbstractReadOnlyModel<byte[]>() {

					@Override
					public byte[] getObject() {
						return originalBlobModel.getObject().getContent();
					}
					
				}, new AbstractReadOnlyModel<byte[]>() {

					@Override
					public byte[] getObject() {
						return new byte[0];
					}
					
				}));
			}

			fragment.add(new Label("path", originalBlob.getPath()));
		} else if (!originalBlob.getPath().equals(revisedBlob.getPath())
				|| originalBlob.getMode() != revisedBlob.getMode()
				|| !Arrays.equals(originalBlob.getContent(), revisedBlob.getContent())) {
			fragment = new Fragment("content", "renamedOrModifiedFrag", this);
			
			WebMarkupContainer renamedContainer = new WebMarkupContainer("renamed");
			fragment.add(renamedContainer.setVisible(!originalBlob.getPath().equals(revisedBlob.getPath())));
			renamedContainer.add(new Label("originalPath", originalBlob.getPath()));
			renamedContainer.add(new Label("revisedPath", revisedBlob.getPath()));
			
			WebMarkupContainer modifiedContainer = new WebMarkupContainer("modified");
			fragment.add(modifiedContainer.setVisible(originalBlob.getPath().equals(revisedBlob.getPath())));
			modifiedContainer.add(new Label("path", originalBlob.getPath()));
			
			int originalType = originalBlob.getMode() & FileMode.TYPE_MASK;
			int revisedType = revisedBlob.getMode() & FileMode.TYPE_MASK;
			
			WebMarkupContainer blobTypeChangedContainer = new WebMarkupContainer("blobTypeChanged");
			fragment.add(blobTypeChangedContainer.setVisible(originalType != revisedType));
			blobTypeChangedContainer.add(new Label("originalType", GitUtils.getTypeName(originalType)));
			blobTypeChangedContainer.add(new Label("revisedType", GitUtils.getTypeName(revisedType)));

			WebMarkupContainer fileModeChangedContainer = new WebMarkupContainer("fileModeChanged");
			fragment.add(fileModeChangedContainer.setVisible(originalType == FileMode.TYPE_FILE 
					&& revisedType == FileMode.TYPE_FILE 
					&& originalBlob.getMode() != revisedBlob.getMode()));
			fileModeChangedContainer.add(new Label("originalMode", Integer.toOctalString(originalBlob.getMode())));
			fileModeChangedContainer.add(new Label("revisedMode", Integer.toOctalString(revisedBlob.getMode())));

			if (originalType == FileMode.TYPE_FILE && revisedType == FileMode.TYPE_FILE) {
				fragment.add(new WebMarkupContainer("originalContent").setVisible(false));
				fragment.add(new WebMarkupContainer("revisedContent").setVisible(false));
				fragment.add(new FileDiffPanel("diffContent", new AbstractReadOnlyModel<byte[]>() {

					@Override
					public byte[] getObject() {
						return originalBlobModel.getObject().getContent();
					}
					
				}, new AbstractReadOnlyModel<byte[]>() {

					@Override
					public byte[] getObject() {
						return revisedBlobModel.getObject().getContent();
					}
					
				}));
			} else {
				fragment.add(new WebMarkupContainer("diffContent").setVisible(false));
				if (originalType == FileMode.TYPE_FILE) {
					fragment.add(new FileDiffPanel("originalContent", new AbstractReadOnlyModel<byte[]>() {

						@Override
						public byte[] getObject() {
							return originalBlobModel.getObject().getContent();
						}
						
					}, new AbstractReadOnlyModel<byte[]>() {

						@Override
						public byte[] getObject() {
							return new byte[0];
						}
						
					}));
				} else if (originalType == FileMode.TYPE_GITLINK) {
					GitLink link = new GitLink("originalContent", new String(originalBlob.getContent()));
					link.add(AttributeAppender.append("class", "original"));
					fragment.add(link);
				} else {
					SymbolLink link = new SymbolLink("originalContent", repoModel, originalBlob.getRevision(), 
							originalBlob.getPath(), new String(originalBlob.getContent()));
					link.add(AttributeAppender.append("class", "original"));
					fragment.add(link);
				}
				if (revisedType == FileMode.TYPE_FILE) {
					fragment.add(new FileDiffPanel("revisedContent", new AbstractReadOnlyModel<byte[]>() {

						@Override
						public byte[] getObject() {
							return new byte[0];
						}
						
					}, new AbstractReadOnlyModel<byte[]>() {

						@Override
						public byte[] getObject() {
							return revisedBlobModel.getObject().getContent();
						}
						
					}));
				} else if (revisedType == FileMode.TYPE_GITLINK) {
					GitLink link = new GitLink("revisedContent", new String(revisedBlob.getContent()));
					link.add(AttributeAppender.append("class", "revised"));
					fragment.add(link);
				} else {
					SymbolLink link = new SymbolLink("revisedContent", repoModel, revisedBlob.getRevision(), 
							revisedBlob.getPath(), new String(revisedBlob.getContent()));
					link.add(AttributeAppender.append("class", "revised"));
					fragment.add(link);
				}
			}
		} else {
			fragment = new Fragment("content", "equalFrag", this);
			fragment.add(new Label("path", originalBlob.getPath()));
			if (originalBlob.getMode() == FileMode.TYPE_GITLINK) {
				fragment.add(new GitLink("blobContent", new String(originalBlob.getContent())));
			} else if (originalBlob.getMode() == FileMode.TYPE_SYMLINK) {
				fragment.add(new SymbolLink("blobContent", repoModel, originalBlob.getRevision(), 
						originalBlob.getPath(), new String(originalBlob.getContent())));
			} else {
				fragment.add(new FileDiffPanel("blobContent", new AbstractReadOnlyModel<byte[]>() {

					@Override
					public byte[] getObject() {
						return originalBlobModel.getObject().getContent();
					}
					
				}, new AbstractReadOnlyModel<byte[]>() {

					@Override
					public byte[] getObject() {
						return revisedBlobModel.getObject().getContent();
					}
					
				}));
			}
		}
		add(fragment);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		originalBlobModel.detach();
		revisedBlobModel.detach();
		
		super.onDetach();
	}

}
