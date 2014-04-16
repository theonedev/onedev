package com.pmease.gitop.web.page.repository.source.commit.diff.renderer;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffEntry.Side;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.page.repository.source.blob.SourceBlobPage;
import com.pmease.gitop.web.page.repository.source.commit.diff.DiffStatBar;
import com.pmease.gitop.web.page.repository.source.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public abstract class BlobDiffPanel extends Panel {

	protected final IModel<Repository> repositoryModel;
	protected final IModel<String> sinceModel;
	protected final IModel<String> untilModel;
//	protected final IModel<List<CommitComment>> commentsModel;
	
	protected final int index;
	
	abstract protected Component createActionsBar(String id);
	abstract protected Component createDiffContent(String id);
	
	protected final String markupId;
	
	public BlobDiffPanel(String id,
			final int index,
			IModel<FileHeader> fileModel,
			IModel<Repository> repositoryModel,
			IModel<String> sinceModel,
			IModel<String> untilModel) {
		super(id, fileModel);
	
		this.index = index;
		this.markupId = "diff-" + index;
		this.repositoryModel = repositoryModel;
		this.sinceModel = sinceModel;
		this.untilModel = untilModel;
//		this.commentsModel = commentsModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer statspan = new WebMarkupContainer("statspan");
		add(statspan);
		statspan.add(AttributeModifier.replace("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getFile().getDiffStat().toString();
			}
			
		}));
		
		statspan.add(new DiffStatBar("stat", (IModel<FileHeader>) getDefaultModel()));
		
		add(new Label("path", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String path;
				FileHeader file = getFile();
				if (file.getChangeType() == ChangeType.RENAME) {
					path = file.getPath(Side.OLD) + " &#10142; " + file.getPath(Side.NEW);
				} else if (file.getChangeType() == ChangeType.DELETE) {
					path = file.getPath(Side.OLD);
				} else {
					path = file.getPath(Side.NEW);
				}
				
				return path;
			}
		}).setEscapeModelStrings(false));
		
		String path = getFile().getNewPath();
		List<String> paths = Lists.newArrayList(Splitter.on("/").split(path));
		String until = untilModel.getObject();
		AbstractLink blobLink = new BookmarkablePageLink<Void>("bloblink",
				SourceBlobPage.class,
				SourceBlobPage.newParams(repositoryModel.getObject(), 
										 until, 
										 paths));
		blobLink.setVisibilityAllowed(getFile().getChangeType() != ChangeType.DELETE);
		add(blobLink);
//		blobLink.add(new Label("sha", Model.of(GitUtils.abbreviateSHA(until, 6))));
		
		add(createActionsBar("actions"));
		add(createDiffContent("diffcontent"));
	}

	protected FileHeader getFile() {
		return (FileHeader) getDefaultModelObject();	
	}
	
	@SuppressWarnings("unchecked")
	protected IModel<FileHeader> getFileModel() {
		return (IModel<FileHeader>) getDefaultModel();
	}
	
	protected String getSince() {
		return sinceModel.getObject();
	}
	
	protected String getUntil() {
		return untilModel.getObject();
	}
	
	protected Repository getRepository() {
		return repositoryModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (repositoryModel != null) {
			repositoryModel.detach();
		}
		
		if (sinceModel != null) {
			sinceModel.detach();
		}
		
		if (untilModel != null) {
			untilModel.detach();
		}
		
//		if (commentsModel != null) {
//			commentsModel.detach();
//		}

		super.onDetach();
	}
}
