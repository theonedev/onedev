package com.pmease.gitplex.web.page.repository.info.code.commit.diff.renderer;

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

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.info.code.blob.RepoBlobPage;
import com.pmease.gitplex.web.page.repository.info.code.commit.diff.DiffStatBar;
import com.pmease.gitplex.web.page.repository.info.code.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public abstract class BlobDiffPanel extends Panel {

	protected final IModel<Repository> repoModel;
	
	protected final String sinceRevision;
	
	protected final String untilRevision;
	
	//	protected final IModel<List<CommitComment>> commentsModel;
	
	protected final int index;
	
	abstract protected Component createActionsBar(String id);
	
	abstract protected Component createDiffContent(String id);
	
	protected final String markupId;
	
	public BlobDiffPanel(String id,
			final int index,
			IModel<Repository> repoModel,
			IModel<FileHeader> fileModel,
			String sinceRevision,
			String untilRevision) {
		super(id, fileModel);
	
		this.index = index;
		this.markupId = "diff-" + index;
		this.repoModel = repoModel;
		this.sinceRevision = sinceRevision;
		this.untilRevision = untilRevision;
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
		AbstractLink blobLink = new BookmarkablePageLink<Void>("bloblink",
				RepoBlobPage.class,
				RepoBlobPage.paramsOf(repoModel.getObject(), 
										 untilRevision, 
										 path));
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
	
	@Override
	public void onDetach() {
		repoModel.detach();
		
//		if (commentsModel != null) {
//			commentsModel.detach();
//		}

		super.onDetach();
	}
}
