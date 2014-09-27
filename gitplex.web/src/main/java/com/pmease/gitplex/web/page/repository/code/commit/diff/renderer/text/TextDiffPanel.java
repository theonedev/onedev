package com.pmease.gitplex.web.page.repository.code.commit.diff.renderer.text;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.parboiled.common.Preconditions;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.OldCommitComment;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.comment.event.CommitCommentAdded;
import com.pmease.gitplex.web.component.comment.event.CommitCommentRemoved;
import com.pmease.gitplex.web.git.GitUtils;
import com.pmease.gitplex.web.page.repository.code.commit.diff.CommitCommentsAware;
import com.pmease.gitplex.web.page.repository.code.commit.diff.patch.FileHeader;
import com.pmease.gitplex.web.page.repository.code.commit.diff.patch.HunkHeader;
import com.pmease.gitplex.web.page.repository.code.commit.diff.renderer.BlobDiffPanel;
import com.pmease.gitplex.web.service.FileBlob;
import com.pmease.gitplex.web.service.FileBlobService;

@SuppressWarnings("serial")
public class TextDiffPanel extends BlobDiffPanel {

//	private IModel<Boolean> showInlineComments = Model.of(true);
//	private IModel<Boolean> enableAddComments = Model.of(true);
	
	private final IModel<FileBlob> newFileModel;
	private final IModel<FileBlob> oldFileModel;
	
	public TextDiffPanel(String id,
			int index,
			IModel<Repository> repoModel,
			IModel<FileHeader> fileModel,
			final String sinceRevision,
			final String untilRevision) {
		
		super(id, index, repoModel, fileModel, sinceRevision, untilRevision);
		
		this.newFileModel = new LoadableDetachableModel<FileBlob>() {

			@Override
			protected FileBlob load() {
				return loadBlob(untilRevision, getFile().getNewPath());
			}
			
		};
		this.oldFileModel = new LoadableDetachableModel<FileBlob>() {

			@Override
			protected FileBlob load() {
				return loadBlob(sinceRevision, getFile().getOldPath());
			}
		};
	}

	private FileBlob loadBlob(String revision, String path) {
		if (GitUtils.isNullHash(revision) || GitUtils.isEmptyPath(path)) {
			return null;
		}
		
		return GitPlex.getInstance(FileBlobService.class).get(repoModel.getObject(), revision, path);
	}
	
	WebMarkupContainer actionsBar;
	
	static String getFileId(FileHeader file) {
		if (file.getChangeType() == ChangeType.DELETE) {
			return file.getOldId().name();
		} else {
			return file.getNewId().name();
		}
	}
	
	private CommitCommentsAware getCommentsAware() {
		Page page = getPage();
		Preconditions.checkState(page instanceof CommitCommentsAware);
		return (CommitCommentsAware) page;
	}
	
	private boolean hasInlineComments() {
		List<OldCommitComment> comments = getCommentsAware().getCommitComments();
		String fileId = getFileId(getFile());
		for (OldCommitComment each : comments) {
			if (each.isLineComment() && each.getLine().startsWith(fileId)) {
				return true;
			}
		}
		
		return false;
	}
	
	static final String SHOW_INLINE_COMMENTS =
			"$('#%s .trigger-show-inline-comments').click(function() {\n"
			+ " if ($(this).prop('checked')) {\n"
			+ "		$('#%s tr.inline-comment-holder').show();\n"
			+ "	} else {\n"
			+ "		$('#%s tr.inline-comment-holder').hide();\n"
			+ "	}\n"
			+ "})";
	
	@Override
	protected Component createActionsBar(String id) {
		if (hasInlineComments()) {
			actionsBar = new Fragment(id, "actionsfrag", this) {
				
				@Override
				public void renderHead(IHeaderResponse response) {
					super.renderHead(response);
				
					response.render(OnDomReadyHeaderItem.forScript(String.format(SHOW_INLINE_COMMENTS, 
							markupId, markupId, markupId)));
				}
			};
		} else {
			actionsBar = new WebMarkupContainer(id);
		}
		
		actionsBar.setOutputMarkupId(true);
		return actionsBar;
	}

	@Override
	public void onEvent(IEvent<?> sink) {
		if (sink.getPayload() instanceof CommitCommentAdded) {
			CommitCommentAdded e = (CommitCommentAdded) sink.getPayload();
			
			// inline comment added, enable view inline comments checkbox
			//
			if (e.getComment().isLineComment() && e.getComment().getLine().startsWith(getFileId(getFile()))) {
				addOrReplace(createActionsBar("actions"));
				e.getTarget().add(actionsBar);
			}
			
		} else if (sink.getPayload() instanceof CommitCommentRemoved) {
			CommitCommentRemoved e = (CommitCommentRemoved) sink.getPayload();
			if (!hasInlineComments()) {
				addOrReplace(createActionsBar("actions"));
				e.getTarget().add(actionsBar);
			}
		}
	}
	
	@Override
	protected Component createDiffContent(String id) {
		Fragment frag = new Fragment(id, "tablefrag", this);
		frag.add(new ListView<HunkHeader>("hunks", new LoadableDetachableModel<List<? extends HunkHeader>>() {

			@Override
			protected List<? extends HunkHeader> load() {
				return getFile().getHunks();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<HunkHeader> item) {
				item.add(new HunkPanel("hunk",
						repoModel,
						untilRevision,
						Model.of(item.getIndex()),
						getFileModel(),
						new LoadableDetachableModel<List<String>>() {

							@Override
							protected List<String> load() {
								FileHeader file = getFile();
								if (file.getChangeType() == ChangeType.DELETE) {
									return oldFileModel.getObject().getLines();
								} else {
									return newFileModel.getObject().getLines();
								}
							}
						}));
			}
		});
		
		return frag;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}
	
	@Override
	public void onDetach() {
		
		if (newFileModel != null) {
			newFileModel.detach();
		}
		
		if (oldFileModel != null) {
			oldFileModel.detach();
		}
		
		super.onDetach();
	}
}
