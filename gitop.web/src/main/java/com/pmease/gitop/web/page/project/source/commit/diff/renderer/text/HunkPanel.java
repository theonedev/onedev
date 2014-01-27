package com.pmease.gitop.web.page.project.source.commit.diff.renderer.text;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.iterator.ComponentHierarchyIterator;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.parboiled.common.Preconditions;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.CommitCommentManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.Constants;
import com.pmease.gitop.web.component.comment.CommitCommentEditor;
import com.pmease.gitop.web.component.comment.CommitCommentPanel;
import com.pmease.gitop.web.model.CommitCommentModel;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.HunkHeader;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.HunkLine;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.HunkLine.LineType;

@SuppressWarnings("serial")
public class HunkPanel extends Panel {

	private final IModel<Project> projectModel;
	private final IModel<FileHeader> fileModel;
	private final IModel<List<String>> blobLinesModel;
	private final IModel<String> commitModel;
	
	private RepeatingView linesView;

	private int startLine;
	private int endLine;
	
	private String belowMarkupId;
	
	private final IModel<Multimap<String, Long>> commentsModel;
	
	final static String INSERT_ROW_TEMPLATE = "var item = document.createElement('tr'); item.id='%s'; $(item).insertAfter($('#%s'));";
	
	public HunkPanel(String id,
			IModel<Project> projectModel,
			IModel<String> commitModel,
			IModel<Integer> indexModel,
			IModel<FileHeader> fileModel,
			IModel<List<String>> blobLinesModel,
			final IModel<List<CommitComment>> commentsModel) {
		
		super(id, indexModel);
		
		this.projectModel = projectModel;
		this.commitModel = commitModel;
		this.fileModel = fileModel;
		this.blobLinesModel = blobLinesModel;
		this.commentsModel = new LoadableDetachableModel<Multimap<String, Long>>() {

			@Override
			protected Multimap<String, Long> load() {
				List<CommitComment> comments = commentsModel.getObject();
				Multimap<String, Long> map = LinkedListMultimap.<String, Long>create();
				
				String fileId = getFileId();
				for (CommitComment each : comments) {
					String line = each.getLine();
					if (!Strings.isNullOrEmpty(line) && line.startsWith(fileId)) {
						map.put(line, each.getId());
					}
				}
				
				return map;
			}
		};
	}

	private String getFileId() {
		FileHeader file = getFile();
		if (file.getChangeType() == ChangeType.DELETE) {
			return file.getOldId().name();
		} else {
			return file.getNewId().name();
		}
	}
	
	private String getLineId(int position) {
		return getFileId() + "-L" + position;
	}
	
	/*
	static String toPathId(String original) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < original.length(); i++) {
			char c = original.charAt(i);
			if (CharMatcher.JAVA_LETTER_OR_DIGIT.matches(c)) {
				sb.append(c);
			} else {
				sb.append('-');
			}
		}
		
		return sb.toString();
	}
	
	private String getPathId() {
		FileHeader file = getFile();
		String path;
		if (file.getChangeType() == ChangeType.DELETE) {
			path = file.getOldPath();
		} else {
			path = file.getNewPath();
		}
		
		return toPathId(path);
	}
	*/
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(createAboveExpander());
		add(createHunkLines());
		add(createBelowExpander());
	}
	
	private Component createAboveExpander() {
		WebMarkupContainer aboveExpander = new WebMarkupContainer("aboveexpander") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				HunkHeader previous = getPreviousHunk();
				if (previous != null) {
					this.setVisibilityAllowed(startLine > previous.getNewEndLine() + 1);
				}
			}
		};
		
		aboveExpander.setOutputMarkupId(true);
		
		aboveExpander.add(new Label("header", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				HunkHeader current = getCurrentHunk();
				StringBuffer sb = new StringBuffer();
				if (startLine == current.getNewStartLine()) {
					sb.append("@@ ")
						.append("-")
							.append(current.getOldImage().getStartLine())
							.append(",")
							.append(current.getOldImage().getLineCount())
						.append(" +")
							.append(current.getNewStartLine())
							.append(",")
							.append(current.getNewLineCount())
						.append(" @@ ").append(current.getHunkFunction());
				} else {
					int offset = current.getNewStartLine() - startLine;
					int oldStart = current.getOldImage().getStartLine() - offset;
					offset = Math.min(offset, 20);
					sb.append("@@ ")
						.append("-")
							.append(oldStart).append(",").append(offset)
						.append(" +")
							.append(startLine).append(",").append(offset)
						.append(" @@");
				}
				
				return sb.toString();
			}
		}));
		
		aboveExpander.add(new AjaxLink<Void>("expander") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				expandAbove(target);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(isAboveExpandable());
			}
		}.setOutputMarkupId(true));
		
		return aboveExpander;
	}
	
	private int expandAboveTo() {
		int start = Math.max(startLine - 20, 1);
		HunkHeader previous = getPreviousHunk();
		if (previous != null) {
			int previousEnd = previous.getNewEndLine();
			start = Math.max(previousEnd + 1, start);
		}
		
		return start;
	}
	
	private void expandAbove(AjaxRequestTarget target) {
		int newStart = expandAboveTo();
		if (newStart == startLine) {
			return;
		}
		
		Component expander = createAboveExpander();
		addOrReplace(expander);
		target.add(expander);
		
		String aboveMarkupId = expander.getMarkupId();
		target.appendJavaScript("$('#" + aboveMarkupId + " a').tooltip();");

		HunkHeader hunk = getCurrentHunk();
		List<String> blobLines = blobLinesModel.getObject();

		int totalOffset = hunk.getNewStartLine() - newStart;
		int oldStart = hunk.getOldImage().getStartLine() - totalOffset;
		
		for (int i = newStart; i < startLine; i++) {
			HunkLine line = HunkLine.builder()
					.lineType(LineType.CONTEXT)
					.oldLineNo(oldStart)
					.newLineNo(i)
					.text(blobLines.get(i - 1))
					.build();
			
			String markupId = linesView.newChildId();
			Component item = newLineRow(markupId, line, -1);
			item.setOutputMarkupId(true);
			
			insertRow(target, item.getMarkupId(), aboveMarkupId);
//			target.prependJavaScript(
//					String.format(INSERT_ROW_TEMPLATE,
//							item.getMarkupId(true), aboveMarkupId));
			
			linesView.addOrReplace(item);
			target.add(item);
			oldStart++;
			aboveMarkupId = item.getMarkupId();
		}
		
		startLine = newStart;
	}
	
	private void insertRow(AjaxRequestTarget target, String markupId, String afterId) {
		target.prependJavaScript(String.format(INSERT_ROW_TEMPLATE, markupId, afterId));
	}
	
	private boolean isAboveExpandable() {
		if (startLine == 1 || startLine < 0) {
			return false;
		}

		HunkHeader previous = getPreviousHunk();
		if (previous == null) {
			return true;
		}
		
		return startLine > previous.getNewEndLine() + 1;
	}
	
	private Multimap<String, Long> getInlineComments() {
		return commentsModel.getObject();
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		if (event.getPayload() instanceof AddInlineCommentEvent) {
			AddInlineCommentEvent e = (AddInlineCommentEvent) event.getPayload();
			int position = e.getPosition();
			CommentRow commentRow = findCommentRow(position);

			if (commentRow == null) {
				// insert a comment row
				Component row = newCommentRow(position, true);
				Component lineRow = findLineRow(position);
				insertRow(e.getTarget(), row.getMarkupId(), lineRow.getParent().getMarkupId());
				linesView.addOrReplace(row);
				e.getTarget().add(row);
			}
		}
	}
	
	private LineRow findLineRow(int position) {
		ComponentHierarchyIterator it = linesView.visitChildren(LineRow.class);
		LineRow row = null;
		while (it.hasNext()) {
			LineRow each = (LineRow) it.next();
			if (each.position == position) {
				row = each;
				break;
			}
		}
		
		Preconditions.checkState(row != null, "line row " + position + " doesn't exist");
		return row;
	}
	
	private CommentRow findCommentRow(int position) {
		ComponentHierarchyIterator it = linesView.visitChildren(CommentRow.class);
		CommentRow row = null;
		while (it.hasNext()) {
			CommentRow each = (CommentRow) it.next();
			if (each.position == position) {
				row = each;
				break;
			}
		}
		
		return row;
	}
	
	private Component createHunkLines() {
		linesView = new RepeatingView("hunklines");
		
		List<HunkLine> lines = getCurrentHunk().getLines();
		
		Multimap<String, Long> inlineComments = getInlineComments();
		
		for (int i = 0; i < lines.size(); i++) {
			HunkLine line = lines.get(i);
			Component item = newLineRow(linesView.newChildId(), line, i + 1);
			linesView.add(item);
			
			String lineId = getLineId(i + 1);
			if (inlineComments.containsKey(lineId)) {
				// create inline comment block
				linesView.add(newCommentRow(i+1, false));
			}
			
			if (i == lines.size() - 1) {
				belowMarkupId = item.getMarkupId();
			}
		}
		
		if (!lines.isEmpty()) {
			HunkLine line = Iterables.getFirst(lines, null);
			startLine = line.getNewLineNo();
			line = Iterables.getLast(lines);
			endLine = line.getNewLineNo();
		}
		
		return linesView;
	}
	
	int getPreviousTotalLines() {
		int lines = 0;
		for (int i = 0; i < getHunkIndex(); i++) {
			lines += getHunk(i).getLines().size();
		}
		
		return lines;
	}

	private WebMarkupContainer newCommentForm(final int position) {
		return new CommitCommentEditor("commentform") {

			@Override
			protected Component createSubmitButtons(String id, final Form<?> form) {
				Fragment frag = new Fragment(id, "commentformsubmitsfrag", HunkPanel.this);
				frag.add(new AjaxLink<Void>("close") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						send(HunkPanel.this, Broadcast.DEPTH, new CloseInlineCommentEvent(target, position));
					}
				});

				AjaxButton submit = new AjaxButton("comment", form) {
					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						String lineId = getLineId(position);
						CommitComment comment = new CommitComment();
						comment.setAuthor(Gitop.getInstance(UserManager.class).getCurrent());
						comment.setCommit(commitModel.getObject());
						comment.setLine(lineId);
						comment.setProject(projectModel.getObject());
						comment.setContent(getCommentText());
						Gitop.getInstance(CommitCommentManager.class).save(comment);
						
						send(HunkPanel.this, Broadcast.DEPTH, new InlineCommentAddedEvent(target, position, comment.getId()));
					}
				};
				
				frag.add(submit);
				form.setDefaultButton(submit);
				
				return frag;
			}
		};
	}
	
	private class LineRow extends Fragment {

		private final int position;
		
		public LineRow(String id, IModel<HunkLine> model, int position) {
			super(id, "linefrag", HunkPanel.this, model);
			
			this.position = position;
		}
		
		private HunkLine getLine() {
			return (HunkLine) getDefaultModelObject();
		}
		
		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			HunkLine line = getLine();
			String oldLineNo, newLineNo;
			if (line.getLineType() == LineType.CONTEXT) {
				oldLineNo = String.valueOf(line.getOldLineNo());
				newLineNo = String.valueOf(line.getNewLineNo());
			} else if (line.getLineType() == LineType.OLD) {
				oldLineNo = "- " + line.getOldLineNo();
				newLineNo = "&nbsp;";
			} else {
				oldLineNo = "&nbsp;";
				newLineNo = "+ " + line.getNewLineNo();
			}
			
			add(new Label("oldnum", oldLineNo).setEscapeModelStrings(false));
			add(new Label("newnum", newLineNo).setEscapeModelStrings(false));
			add(new Label("code", Model.of(line.getText())));
			
			if (position < 0) {
				add(new WebMarkupContainer("commentlink").setVisibilityAllowed(false));
			} else {
				add(new AjaxLink<Void>("commentlink") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						send(HunkPanel.this, Broadcast.BREADTH, new AddInlineCommentEvent(target, position));
					}
				});
			}
		}
	}
	
	/**
	 * Create a hunk line row, the row including old line number, new line number
	 * and line code, the line can be one of OLD, NEW or CONTEXT
	 * 
	 * @param id
	 * @param line
	 * @param position
	 * @return
	 */
	private Component newLineRow(String id, HunkLine line, final int position) {
		WebMarkupContainer item = new WebMarkupContainer(id);
		item.setOutputMarkupId(true);
		item.add(AttributeAppender.append("class", "diff-line " + line.getLineType().name().toLowerCase()));
		item.add(new LineRow("line", Model.of(line), position));
		return item;
	}
	
	private class CommentRow extends Fragment {

		final int position;
		boolean withForm;
		
		WebMarkupContainer commentForm;
		WebMarkupContainer commentsHolder;
		
		public CommentRow(String id, int position, boolean withForm) {
			super(id, "commentfrag", HunkPanel.this);
			
			this.position = position;
			this.withForm = withForm;
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			IModel<Integer> sizeModel = new AbstractReadOnlyModel<Integer>() {

				@Override
				public Integer getObject() {
					return getCommentIds().size();
				}
			};
			
			add(new Label("count", sizeModel).setOutputMarkupId(true));
			
			commentsHolder = new WebMarkupContainer("commentsholder");
			commentsHolder.setOutputMarkupId(true);
			add(commentsHolder);
			
			Loop commentsView = new Loop("comments", sizeModel) {

				@Override
				protected void populateItem(LoopItem item) {
					int index = item.getIndex();
					CommitComment comment = getComment(index);
					item.add(new CommitCommentPanel("comment", new CommitCommentModel(comment)));
				}
			};
			
			commentsHolder.add(commentsView);
			if (withForm) {
				commentForm = newCommentForm(position);
			} else {
				commentForm = newEmptyForm();
			}
			
			add(commentForm);
		}
		
		private WebMarkupContainer newEmptyForm() {
			WebMarkupContainer commentForm = new WebMarkupContainer("commentform");
			commentForm.setOutputMarkupId(true);
			return commentForm;
		}
		
		private CommitComment getComment(int index) {
			Long id = Iterables.get(getCommentIds(), index);
			return Gitop.getInstance(CommitCommentManager.class).get(id);
		}
		
		private List<Long> getCommentIds() {
			Multimap<String, Long> map = getInlineComments();
			final String lineId = getLineId(position);
			List<Long> ids = Lists.newArrayList(map.get(lineId));
			Collections.sort(ids);
			return ids;
		}
		
		@Override
		public void onEvent(IEvent<?> sink) {
			if (sink.getPayload() instanceof InlineCommentEvent) {
				InlineCommentEvent e = (InlineCommentEvent) sink.getPayload();
				if (e.getPosition() != position) {
					return;
				}
				
				if (e instanceof CloseInlineCommentEvent) {
					List<Long> ids = getCommentIds();
					if (ids.isEmpty()) {
						// remove the whole row
						Component parent = getParent();
						linesView.remove(parent);
						e.getTarget().appendJavaScript("$('#" + parent.getMarkupId() + "').remove()");
					} else {
						// only hide comment form
						commentForm = newEmptyForm();
						addOrReplace(commentForm);
						e.getTarget().add(commentForm);
					}
				} else if (e instanceof AddInlineCommentEvent) {
					if (commentForm instanceof CommitCommentEditor) {
						// form maybe hidden
						e.getTarget().appendJavaScript("$('#" + commentForm.getMarkupId(true) + "').show()");
					} else {
						commentForm = newCommentForm(position);
						addOrReplace(commentForm);
						e.getTarget().add(commentForm);
					}
				} else if (e instanceof InlineCommentAddedEvent) {
					e.getTarget().add(get("count"));
					e.getTarget().add(commentsHolder);
					commentForm = newEmptyForm();
					addOrReplace(commentForm);
					e.getTarget().add(commentForm);
				}
			}
		}
	}
	
	private Component newCommentRow(int position, boolean withForm) {
		WebMarkupContainer item = new WebMarkupContainer(linesView.newChildId());
		item.setOutputMarkupId(true);
		item.add(AttributeAppender.append("class", "inline-comment-holder"));
		item.add(new CommentRow("line", position, withForm));
		
		return item;
	}

	private Component createBelowExpander() {
		WebMarkupContainer expanderTr = new WebMarkupContainer("belowexpander") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisibilityAllowed(isLastHunk() && isBelowExpandable());
			}
		};
		expanderTr.setOutputMarkupId(true);
		
		expanderTr.add(new AjaxLink<Void>("expander") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				expandBelow(target);
			}
		});
		
		return expanderTr;
	}

	private boolean isLastHunk() {
		return getHunkIndex() == getFile().getHunks().size() - 1;
	}
	
	private void expandBelow(AjaxRequestTarget target) {
		int lineNo = endLine + 20;
		List<String> blobLines = blobLinesModel.getObject();
		lineNo = Math.min(lineNo, blobLines.size());
		if (lineNo == endLine) {
			return;
		}

		Component expander = createBelowExpander();
		addOrReplace(expander);
		target.add(expander);
		target.appendJavaScript("$('#" + expander.getMarkupId(true) + " a').tooltip();");
		
		int offset = endLine - getCurrentHunk().getNewEndLine() + 1;
		int oldLineNo = getCurrentHunk().getOldEndLine() + offset;
		
		for (int i = endLine + 1; i <= lineNo; i++) {
			HunkLine line = HunkLine.builder()
					.text(blobLines.get(i - 1))
					.newLineNo(i)
					.oldLineNo(oldLineNo)
					.lineType(LineType.CONTEXT)
					.build();
			
			Component item = newLineRow(linesView.newChildId(), line, -1);
			item.setOutputMarkupId(true);
			insertRow(target, item.getMarkupId(), belowMarkupId);
			linesView.addOrReplace(item);
			target.add(item);
			oldLineNo++;
			belowMarkupId = item.getMarkupId();
		}
		
		endLine = lineNo;
	}
	
	private boolean isBelowExpandable() {
		List<HunkLine> lines = getCurrentHunk().getLines();
		
		if (lines.size() <= Constants.DEFAULT_CONTEXT_LINES) {
			// all lines have been displayed
			return false;
		} else {
			// check the last line but two
			HunkLine line = lines.get(lines.size() - Constants.DEFAULT_CONTEXT_LINES);
			
			if (line.getLineType() != LineType.CONTEXT) {
				return false;
			} else if (endLine > getCurrentHunk().getNewEndLine()) {
				// already expanded
				return endLine < blobLinesModel.getObject().size();
			}
		}
		
		return true;
	}
	
	private int getHunkIndex() {
		return (int) getDefaultModelObject();
	}
	
	private HunkHeader getHunk(int index) {
		return index < 0 ? null : getFile().getHunks().get(index);
	}
	
	private HunkHeader getCurrentHunk() {
		return getHunk(getHunkIndex());
	}
	
	private HunkHeader getPreviousHunk() {
		return getHunk(getHunkIndex() - 1);
	}
	
	private FileHeader getFile() {
		return fileModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (projectModel != null) {
			projectModel.detach();
		}

		if (commitModel != null) {
			commitModel.detach();
		}
		
		if (fileModel != null) {
			fileModel.detach();
		}
		
		if (blobLinesModel != null) {
			blobLinesModel.detach();
		}

		if (commentsModel != null) {
			commentsModel.detach();
		}
		
		super.onDetach();
	}
}
