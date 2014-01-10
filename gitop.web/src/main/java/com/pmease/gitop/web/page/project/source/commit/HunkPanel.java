package com.pmease.gitop.web.page.project.source.commit;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.Lists;
import com.pmease.gitop.web.page.project.source.commit.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.patch.HunkHeader;
import com.pmease.gitop.web.page.project.source.commit.patch.HunkLine;
import com.pmease.gitop.web.page.project.source.commit.patch.HunkLine.LineType;
import com.pmease.gitop.web.service.FileBlob;

@SuppressWarnings("serial")
public class HunkPanel extends Panel {
	
	private final IModel<FileBlob> blobModel;

	private final int hunkIndex;
	
	private int expandedLines = 0;
	
	public HunkPanel(String id, 
			IModel<FileHeader> model,
			IModel<FileBlob> blobModel,
			final int hunkIndex) {
		
		super(id, model);
		
		this.blobModel = blobModel;
		this.hunkIndex = hunkIndex;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(createHunkHead());
		add(createPreviousLines());
		add(createHunkLines());
	}
	
	private Component createPreviousLines() {
		return createHunkLines("previouslines", new LoadableDetachableModel<List<HunkLine>>() {

			@Override
			protected List<HunkLine> load() {
				if (expandedLines > 0) {
					List<HunkLine> lines = Lists.newArrayList();
					
					// need more context lines
					// line start from 1, not 0
					//
					int oldStart = getActualOldStartLine();
					int newStart = getActualNewStartLine();
					
					List<String> blobLines = getBlob().getLines();
					for (int i = 0; i < expandedLines; i++) {
						HunkLine line = new HunkLine(
								blobLines.get(newStart + i - 1), 
								LineType.CONTEXT, 
								oldStart + i, 
								newStart + i);
						lines.add(line);
					}
					return lines;
				} else {
					return Collections.emptyList();
				}
			}
		});
	}
	
	private Component createHunkLines() {
		return createHunkLines("hunklines", new LoadableDetachableModel<List<HunkLine>>() {

			@Override
			protected List<HunkLine> load() {
				return getCurrentHunk().getLines();
			}
			
		});
	}
	
	private Component createHunkHead() {
		WebMarkupContainer header = new WebMarkupContainer("hunkhead");
		header.setOutputMarkupId(true);
		header.add(new AjaxLink<Void>("expandlink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				expandLines();
				onExpand(target);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				int newStartLine = getActualNewStartLine();
				HunkHeader previousHunk = getPreviousHunk();
				int previousStartLine = previousHunk == null ? 1 : previousHunk.getNewStartLine() + previousHunk.getNewLineCount();
				setEnabled(newStartLine > 1 && newStartLine > previousStartLine);
			}
		});
		
		header.add(new Label("head", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				HunkHeader hunk = getCurrentHunk();
				StringBuffer sb = new StringBuffer();
				sb.append("@@ ");
				int oldStart = getActualOldStartLine();
				int newStart = getActualNewStartLine();
				int oldOffset = expandedLines > 0 ? expandedLines : hunk.getOldImage().getLineCount();
				int newOffset = expandedLines > 0 ? expandedLines : hunk.getNewLineCount();
				
				sb.append("-").append(oldStart).append(",").append(oldOffset);
				sb.append(" +").append(newStart).append(",").append(newOffset);
				sb.append(" @@");
				return sb.toString();
			}
		}));
		
		return header;
	}
	
	private void expandLines() {
		expandedLines += 20;
		
		int startLine = getCurrentHunk().getNewStartLine();
		startLine -= expandedLines;
		startLine = Math.max(1, startLine);
		HunkHeader previous = getPreviousHunk();
		if (previous != null) {
			startLine = Math.max(previous.getNewStartLine() + previous.getNewLineCount(), startLine);
		}
		
		expandedLines = getCurrentHunk().getNewStartLine() - startLine;
	}
	
	Component createHunkLines(String id, IModel<List<HunkLine>> linesModel) {
		Fragment frag = new Fragment(id, "linefrag", this);
		frag.setOutputMarkupId(true);
		frag.add(new ListView<HunkLine>("lines", linesModel) {

			@Override
			protected void populateItem(ListItem<HunkLine> item) {
				HunkLine line = item.getModelObject();
				item.add(AttributeAppender.append("class", line.getLineType().name().toLowerCase()));
				
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
				
				item.add(new Label("oldlineno", oldLineNo).setEscapeModelStrings(false));
				item.add(new Label("newlineno", newLineNo).setEscapeModelStrings(false));
				item.add(new Label("code", line.getText()));
			}
			
		});
		
		return frag;
	}
	
	private int getActualOldStartLine() {
		HunkHeader currentHunk = getCurrentHunk();
		return currentHunk.getOldImage().getStartLine() - expandedLines;
	}
	
	private int getActualNewStartLine() {
		return getCurrentHunk().getNewStartLine() - expandedLines;
	}
	
	private HunkHeader getCurrentHunk() {
		return getHunk(hunkIndex);
	}
	
	private HunkHeader getPreviousHunk() {
		if (hunkIndex > 0) {
			return getHunk(hunkIndex - 1);
		} else {
			return null;
		}
	}
	
	private HunkHeader getHunk(int index) {
		FileHeader file = getFile();
		return file.getHunks().get(index);
	}
	
	private FileHeader getFile() {
		return (FileHeader) getDefaultModelObject();
	}
	
	private FileBlob getBlob() {
		return blobModel.getObject();
	}
	
	private void onExpand(AjaxRequestTarget target) {
		Component head = createHunkHead();
		addOrReplace(head);
		target.add(head);
		
		Component previousLines = createPreviousLines();
		addOrReplace(previousLines);
		target.add(previousLines);
	}
	
	@Override
	public void onDetach() {
		if (blobModel != null) {
			blobModel.detach();
		}
		
		super.onDetach();
	}
}
