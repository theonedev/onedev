package com.pmease.gitop.web.page.project.source.commit.diff.renderer.text;

import java.util.List;

import org.apache.wicket.Component;
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

import com.google.common.base.Strings;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.component.comment.CommitCommentRemoved;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.HunkHeader;
import com.pmease.gitop.web.page.project.source.commit.diff.renderer.BlobDiffPanel;
import com.pmease.gitop.web.service.FileBlob;
import com.pmease.gitop.web.service.FileBlobService;

@SuppressWarnings("serial")
public class TextDiffPanel extends BlobDiffPanel {

	private IModel<Boolean> showInlineComments = Model.of(true);
	private IModel<Boolean> enableAddComments = Model.of(true);
	
	private final IModel<FileBlob> newFileModel;
	private final IModel<FileBlob> oldFileModel;
	
	public TextDiffPanel(String id,
			int index,
			IModel<FileHeader> fileModel,
			IModel<Project> projectModel, 
			IModel<String> sinceModel,
			IModel<String> untilModel,
			IModel<List<CommitComment>> commentsModel,
			IModel<Boolean> showInlineComments,
			IModel<Boolean> enableAddComments) {
		
		super(id, index, fileModel, projectModel, sinceModel, untilModel, commentsModel);
		
		this.showInlineComments = showInlineComments;
		this.enableAddComments = enableAddComments;


		this.newFileModel = new LoadableDetachableModel<FileBlob>() {

			@Override
			protected FileBlob load() {
				return loadBlob(getUntil(), getFile().getNewPath());
			}
			
		};
		this.oldFileModel = new LoadableDetachableModel<FileBlob>() {

			@Override
			protected FileBlob load() {
				return loadBlob(getSince(), getFile().getOldPath());
			}
		};
	}

	private FileBlob loadBlob(String revision, String path) {
		if (GitUtils.isNullHash(revision) || GitUtils.isEmptyPath(path)) {
			return null;
		}
		
		return Gitop.getInstance(FileBlobService.class).get(getProject(), revision, path);
	}
	
	WebMarkupContainer actionsBar;
	
	static String getFileId(FileHeader file) {
		if (file.getChangeType() == ChangeType.DELETE) {
			return file.getOldId().name();
		} else {
			return file.getNewId().name();
		}
	}
	
	private boolean hasInlineComments() {
		List<CommitComment> comments = commentsModel.getObject();
		String fileId = getFileId(getFile());
		for (CommitComment each : comments) {
			if (each.getLine().startsWith(fileId)) {
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
		if (sink.getPayload() instanceof InlineCommentAddedEvent) {
			InlineCommentAddedEvent e = (InlineCommentAddedEvent) sink.getPayload();
			String lineId = e.getLineId();
			if (!Strings.isNullOrEmpty(lineId) && lineId.startsWith(getFileId(getFile()))) {
				addOrReplace(createActionsBar("actions"));
				e.getTarget().add(actionsBar);
			}
		} else if (sink.getPayload() instanceof CommitCommentRemoved) {
			CommitCommentRemoved e = (CommitCommentRemoved) sink.getPayload();
			String lineId = e.getCommitComment().getLine();
			if (!Strings.isNullOrEmpty(lineId) && lineId.startsWith(getFileId(getFile()))) {
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
						projectModel, 
						untilModel,
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
						},
						commentsModel,
						showInlineComments,
						enableAddComments));
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
		
		if (showInlineComments != null) {
			showInlineComments.detach();
		}
		
		if (enableAddComments != null) {
			enableAddComments.detach();
		}

		if (newFileModel != null) {
			newFileModel.detach();
		}
		
		if (oldFileModel != null) {
			oldFileModel.detach();
		}
		
		super.onDetach();
	}
}
