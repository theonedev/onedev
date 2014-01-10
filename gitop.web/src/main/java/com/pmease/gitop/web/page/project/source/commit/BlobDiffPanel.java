package com.pmease.gitop.web.page.project.source.commit;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffEntry.Side;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Commit;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.wicket.bootstrap.Alert;
import com.pmease.gitop.web.common.wicket.bootstrap.Alert.Type;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.page.project.source.blob.SourceBlobPage;
import com.pmease.gitop.web.page.project.source.commit.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.patch.FileHeader.PatchType;
import com.pmease.gitop.web.page.project.source.commit.patch.HunkHeader;
import com.pmease.gitop.web.service.FileBlob;
import com.pmease.gitop.web.service.FileBlobService;

@SuppressWarnings("serial")
public class BlobDiffPanel extends Panel {

	private final IModel<Commit> commitModel;
	private final IModel<Project> projectModel;
	
	public BlobDiffPanel(String id,
			IModel<Project> projectModel,
			IModel<FileHeader> fileModel, 
			IModel<Commit> commitModel) {
		super(id, fileModel);
	
		this.projectModel = projectModel;
		this.commitModel = commitModel;
//		this.blobModel = new LoadableDetachableModel<FileBlob>() {
//
//			@Override
//			protected FileBlob load() {
//				return Gitop.getInstance(FileBlobService.class).get(
//						getProject(), 
//						getCommit().getHash(), 
//						getFile().getNewPath());
//			}
//		};
	}

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
		
		statspan.add(new Label("additions", new AbstractReadOnlyModel<String>() {
				@Override
				public String getObject() {
					int additions = getFile().getDiffStat().getAdditions(); 
					return additions > 0 ? "+" + additions : "-";
				} 
			}));
		statspan.add(new Label("deletions", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				int deletions = getFile().getDiffStat().getDeletions(); 
				return deletions > 0 ? "-" + deletions : "-";
			}
		}));
		
		add(new Label("oldpath", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getFile().getPath(Side.OLD);
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				this.setVisibilityAllowed(getFile().getChangeType() == ChangeType.RENAME);
			}
		});
		
		add(new Label("newpath", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getFile().getChangeType() == ChangeType.DELETE) {
					return getFile().getPath(Side.OLD);
				} else {
					return getFile().getPath(Side.NEW);
				}
			}
		}));
		
		String path = getFile().getNewPath();
		List<String> paths = Lists.newArrayList(Splitter.on("/").split(path));
		Commit commit = commitModel.getObject();
		AbstractLink blobLink = new BookmarkablePageLink<Void>("bloblink",
				SourceBlobPage.class,
				SourceBlobPage.newParams(projectModel.getObject(), 
										 commit.getHash(), 
										 paths));
		blobLink.setVisibilityAllowed(getFile().getChangeType() != ChangeType.DELETE);
		add(blobLink);
		blobLink.add(new Label("sha", Model.of(GitUtils.abbreviateSHA(commit.getHash(), 6))));
		
		add(createContent("diffcontent"));
	}

	static final int MAX_DIFF_LINES = 10000;
	private Component createContent(String id) {
		List<? extends HunkHeader> hunks = getFile().getHunks();
		if (hunks.isEmpty() 
				|| getFile().getDiffStat().getTotalChanges() > MAX_DIFF_LINES) {
			return (createMessageLabel("diffcontent"));
			
		} else {
			
			return (createHunks("diffcontent"));
		}
	}
	
	private Component createMessageLabel(String id) {
		return new Alert(id, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				FileHeader file = getFile();
				if (file.getChangeType() == ChangeType.RENAME) {
					return "File renamed without changes";
				}
				
				if (file.getPatchType() == PatchType.BINARY) {
					return "File is a binary file";
				}
				
				if (file.getDiffStat().getTotalChanges() > MAX_DIFF_LINES) {
					Commit commit = commitModel.getObject();
					return "<p>"
							+ "The diff for this file is too large to render. "
							+ "You can run below command to get the diff manually:"
							+ "</p> "
							+ "<pre><code>"
							+ "git diff -C -M " + commit.getHash() + "^.." + commit.getHash() + " -- " 
							+ StringUtils.quoteArgument(getFile().getNewPath())
							+ "</code></pre>";
				}

				return "File is empty";
			}
		}).type(Type.Warning).setCloseButtonVisible(false);
	}
	
	private Component createHunks(String id) {
		Fragment frag = new Fragment(id, "frag", this);
		frag.setOutputMarkupId(true);
		Loop loop = new Loop("loop", getFile().getHunks().size()) {

			@SuppressWarnings("unchecked")
			@Override
			protected void populateItem(LoopItem item) {
				item.add(new HunkPanel("hunk", 
						(IModel<FileHeader>) BlobDiffPanel.this.getDefaultModel(), 
						new LoadableDetachableModel<List<String>>() {

							@Override
							protected List<String> load() {
								FileBlob blob = Gitop.getInstance(FileBlobService.class).get(
										getProject(), 
										getCommit().getHash(), 
										getFile().getNewPath());
								return blob.getLines();
							}
						},
						item.getIndex()));
				item.setRenderBodyOnly(true);
			}
		};
		
		frag.add(loop);
		return frag;
	}
	
	private FileHeader getFile() {
		return (FileHeader) getDefaultModelObject();	
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}
	
	private Commit getCommit() {
		return commitModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (projectModel != null) {
			projectModel.detach();
		}
		
		if (commitModel != null) {
			commitModel.detach();
		}
		
		super.onDetach();
	}
}
