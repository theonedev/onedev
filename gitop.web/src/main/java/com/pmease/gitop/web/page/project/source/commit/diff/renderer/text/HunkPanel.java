package com.pmease.gitop.web.page.project.source.commit.diff.renderer.text;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.CommitCommentManager;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.web.Constants;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.HunkHeader;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.HunkLine;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.HunkLine.LineType;

@SuppressWarnings("serial")
public class HunkPanel extends Panel {

	private final IModel<FileHeader> fileModel;
	private final IModel<List<String>> blobLinesModel;
	
	private RepeatingView linesView;

	private int startLine;
	private int endLine;
	
	private String belowMarkupId;
	
	private final IModel<Multimap<String, Long>> commentsModel;
	
	final static String INSERT_ROW_TEMPLATE = "var item = document.createElement('tr'); item.id='%s'; $(item).insertAfter($('#%s'));";
	
	public HunkPanel(String id, 
			IModel<Integer> indexModel,
			IModel<FileHeader> fileModel,
			IModel<List<String>> blobLinesModel,
			final IModel<List<CommitComment>> commentsModel) {
		
		super(id, indexModel);
		
		this.fileModel = fileModel;
		this.blobLinesModel = blobLinesModel;
		this.commentsModel = new LoadableDetachableModel<Multimap<String, Long>>() {

			@Override
			protected Multimap<String, Long> load() {
				List<CommitComment> comments = commentsModel.getObject();
				Multimap<String, Long> map = LinkedListMultimap.<String, Long>create();
				
				String pathId = getPathId();
				for (CommitComment each : comments) {
					String line = each.getLine();
					if (!Strings.isNullOrEmpty(line) && line.startsWith(pathId)) {
						map.put(line, each.getId());
					}
				}
				
				return map;
			}
		};
	}

	private String getPathId() {
		FileHeader file = getFile();
		String path;
		if (file.getChangeType() == ChangeType.DELETE) {
			path = file.getOldPath();
		} else {
			path = file.getNewPath();
		}
		
		return StringUtils.replace(StringUtils.replace(path, "/", "-"), ".", "-");
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(createHunkLines());
		add(createAboveExpander());
		add(createBelowExpander());
	}
	
	private Multimap<String, Long> getInlineComments() {
		return commentsModel.getObject();
	}
	
	private Component createHunkLines() {
		linesView = new RepeatingView("hunklines");
		List<HunkLine> lines = getCurrentHunk().getLines();
		
		Multimap<String, Long> inlineComments = getInlineComments();
		
		for (int i = 0; i < lines.size(); i++) {
			HunkLine line = lines.get(i);
			Component item = newLineRow(linesView.newChildId(), line, i + 1, true);
			linesView.add(item);
			
			String lineId = item.getMarkupId();
			if (inlineComments.containsKey(lineId)) {
				// create inline comment block
				createInlineCommentBlock(i+1, false);
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

	private Component createInlineCommentBlock(int position, boolean showForm) {
		String markupId = getPathId() + "-C" + getHunkIndex() + "-" + position;
		WebMarkupContainer item = new WebMarkupContainer(linesView.newChildId());
		item.setMarkupId(markupId);
		item.add(AttributeAppender.append("class", Model.of("inline-comment-holder")));
		
		Fragment frag = new Fragment("line", "inlinecommentfrag", this);
		Multimap<String, Long> map = getInlineComments();
		final Collection<Long> ids = map.get(markupId);
		frag.add(new Label("count", ids.size()));
		
		WebMarkupContainer commentsHolder = new WebMarkupContainer("commentsholder");
		frag.add(commentsHolder);
		
		Loop commentsView = new Loop("comments", ids.size()) {

			@Override
			protected void populateItem(LoopItem item) {
				int index = item.getIndex();
				Long id = Iterables.get(ids, index);
				CommitComment comment = Gitop.getInstance(CommitCommentManager.class).get(id);
				item.add(new Label("comment", comment.getContent()));
			}
		};
		commentsHolder.add(commentsView);
		
		if (showForm) {
			frag.add(new CommitCommentFormPanel("commentform"));
		} else {
			frag.add(new WebMarkupContainer("commentform"));
		}
		
		return item;
	}
	
	private Component newLineRow(String id, HunkLine line, final int position, boolean original) {
		final WebMarkupContainer item = new WebMarkupContainer(id);
		String markupId = getPathId() + "-P" + getHunkIndex() + "-" + position;
		if (original) {
			item.setMarkupId(markupId);
		}
		
		item.add(AttributeAppender.append("class", "diff-line " + line.getLineType().name().toLowerCase()));
		Fragment frag = new Fragment("line", "linefrag", this);
		item.add(frag);
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
		
		frag.add(new Label("oldnum", oldLineNo).setEscapeModelStrings(false));
		frag.add(new Label("newnum", newLineNo).setEscapeModelStrings(false));
		frag.add(new Label("code", Model.of(line.getText())));
		if (Strings.isNullOrEmpty(markupId)) {
			frag.add(new WebMarkupContainer("commentlink").setVisibilityAllowed(false));
		} else {
			frag.add(new AjaxLink<Void>("commentlink") {
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					String markupId = getPathId() + "-C" + getHunkIndex() + "-" + position;
					WebMarkupContainer holder = (WebMarkupContainer) linesView.get(markupId);
					Component c = null;
					if (holder != null) {
						c = holder.get("commitform");
					}
					
					if (c != null) {
						return;
					}
					
					Component commentRow = newLineCommentRow(markupId);
					commentRow.setMarkupId(markupId);
					commentRow.setOutputMarkupId(true);
					target.prependJavaScript(
							String.format(INSERT_ROW_TEMPLATE,
									commentRow.getMarkupId(true), 
									item.getMarkupId()));
					
					linesView.addOrReplace(commentRow);
					target.add(commentRow);
				}
			});			
		}
		
		return item;
	}
	
	private Component newLineCommentRow(String id) {
		WebMarkupContainer item = new WebMarkupContainer(id);
		item.add(AttributeAppender.append("class", "inline-comments"));
		Fragment frag = new Fragment("line", "inlinecommentfrag", this);
		item.add(frag);
		return item;
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
		}).setOutputMarkupId(true);
		
		return aboveExpander;
	}
	
	private int expandTo() {
		int start = Math.max(startLine - 20, 1);
		HunkHeader previous = getPreviousHunk();
		if (previous != null) {
			int previousEnd = previous.getNewEndLine();
			start = Math.max(previousEnd + 1, start);
		}
		
		return start;
	}
	
	private void expandAbove(AjaxRequestTarget target) {
		int newStart = expandTo();
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
			Component item = newLineRow(markupId, line, 0, false);
			item.setOutputMarkupId(true);
			target.prependJavaScript(
					String.format(INSERT_ROW_TEMPLATE,
							item.getMarkupId(true), aboveMarkupId));
			
			linesView.addOrReplace(item);
			target.add(item);
			oldStart++;
			aboveMarkupId = item.getMarkupId();
//			oldLineNo--;
		}
		
		startLine = newStart;
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
			
			Component item = newLineRow(linesView.newChildId(), line, 0, false);
			item.setOutputMarkupId(true);
			target.prependJavaScript(
					String.format(INSERT_ROW_TEMPLATE,
							item.getMarkupId(true), belowMarkupId));
			
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
