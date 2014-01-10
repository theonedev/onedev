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

@SuppressWarnings("serial")
public class HunkPanel extends Panel {
	
	private final IModel<List<String>> blobLinesModel;

	private final int hunkIndex;
	
	private int aboveLines = 0;
	private int belowLines = 0;
	
	public HunkPanel(String id, 
			IModel<FileHeader> fileModel,
			IModel<List<String>> blobLinesModel,
			final int hunkIndex) {
		
		super(id, fileModel);
		
		this.blobLinesModel = blobLinesModel;
		this.hunkIndex = hunkIndex;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(createAboveHunkHead());
		add(createAboveLines());
		add(createCurrentHunk());
		
		add(createBelowLines());
		add(createBelowHunkHead());
	}
	
	private Component createAboveLines() {
		return createHunkLines("abovelines", new LoadableDetachableModel<List<HunkLine>>() {

			@Override
			protected List<HunkLine> load() {
				if (aboveLines > 0) {
					List<HunkLine> lines = Lists.newArrayList();
					
					// need more context lines
					// line start from 1, not 0
					//
					int oldStart = getAboveOldStartLine();
					int newStart = getAboveNewStartLine();
					
					List<String> blobLines = getBlobLines();
					for (int i = 0; i < aboveLines; i++) {
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
	
	private Component createBelowLines() {
		if (hunkIndex != getFile().getHunks().size() - 1) {
			return new WebMarkupContainer("belowlines").setVisibilityAllowed(false);
		}
		
		return createHunkLines("belowlines", new LoadableDetachableModel<List<HunkLine>>() {

			@Override
			protected List<HunkLine> load() {
				// Only display the last hunk
				//
				if (hunkIndex == getFile().getHunks().size() - 1 && belowLines > 0) {
					List<HunkLine> lines = Lists.newArrayList();
					HunkHeader hunk = getCurrentHunk();
					int oldStart = hunk.getOldImage().getLineCount() + hunk.getOldImage().getStartLine();
					int newStart = getCurrentHunk().getNewStartLine() + getCurrentHunk().getNewLineCount();
					int newEnd = newStart + belowLines;
					List<String> blobLines = getBlobLines();
					for (; newStart < newEnd; newStart++, oldStart++) {
						HunkLine line = new HunkLine(
								blobLines.get(newStart - 1),
								LineType.CONTEXT,
								oldStart, newStart);
						lines.add(line);
					}
					
					return lines;
				} else {
					return Collections.emptyList();
				}
			}
			
		});
	}
	
	private Component createCurrentHunk() {
		return createHunkLines("hunklines", new LoadableDetachableModel<List<HunkLine>>() {

			@Override
			protected List<HunkLine> load() {
				return getCurrentHunk().getLines();
			}
			
		});
	}
	
	private Component createAboveHunkHead() {
		WebMarkupContainer header = new WebMarkupContainer("abovehunk");
		header.setOutputMarkupId(true);
		header.add(new AjaxLink<Void>("expandlink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				onExpandAbove(target);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				int newStartLine = getAboveNewStartLine();
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
				int oldStart = getAboveOldStartLine();
				int newStart = getAboveNewStartLine();
				int oldOffset = aboveLines > 0 ? aboveLines : hunk.getOldImage().getLineCount();
				int newOffset = aboveLines > 0 ? aboveLines : hunk.getNewLineCount();
				
				sb.append("-").append(oldStart).append(",").append(oldOffset);
				sb.append(" +").append(newStart).append(",").append(newOffset);
				sb.append(" @@");
				return sb.toString();
			}
		}));
		
		return header;
	}
	
	Component createBelowHunkHead() {
		if (hunkIndex != getFile().getHunks().size() - 1) {
			return new WebMarkupContainer("belowhunk").setVisibilityAllowed(false);
		}
		
		WebMarkupContainer header = new WebMarkupContainer("belowhunk") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				int newEnd = getCurrentHunk().getNewStartLine() + getCurrentHunk().getNewLineCount() + belowLines;
				setVisibilityAllowed(newEnd < getBlobLines().size());
			}
		};
		
		header.setOutputMarkupId(true);
		header.add(new AjaxLink<Void>("expandlink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				onExpandBelow(target);
			}
		});
		
		return header;
	}
	
	void expandBelowLines() {
		belowLines += 20;
		HunkHeader hunk = getCurrentHunk();
		int newEnd = hunk.getNewStartLine() + hunk.getNewLineCount() + belowLines;
		if (newEnd > getBlobLines().size()) {
			belowLines = getBlobLines().size() - hunk.getNewLineCount() - hunk.getNewStartLine();
		}
	}
	
	void onExpandBelow(AjaxRequestTarget target) {
		expandBelowLines();
		Component head = createBelowHunkHead();
		addOrReplace(head);
		target.add(head);
		
		Component belowLines = createBelowLines();
		addOrReplace(belowLines);
		target.add(belowLines);
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
	
	private int getAboveOldStartLine() {
		HunkHeader currentHunk = getCurrentHunk();
		return currentHunk.getOldImage().getStartLine() - aboveLines;
	}
	
	private int getAboveNewStartLine() {
		return getCurrentHunk().getNewStartLine() - aboveLines;
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
	
	private List<String> getBlobLines() {
		return blobLinesModel.getObject();
	}
	
	private void expandAboveLines() {
		aboveLines += 20;
		
		int startLine = getCurrentHunk().getNewStartLine();
		startLine -= aboveLines;
		startLine = Math.max(1, startLine);
		HunkHeader previous = getPreviousHunk();
		if (previous != null) {
			startLine = Math.max(previous.getNewStartLine() + previous.getNewLineCount(), startLine);
		}
		
		aboveLines = getCurrentHunk().getNewStartLine() - startLine;
	}
	
	private void onExpandAbove(AjaxRequestTarget target) {
		expandAboveLines();
		Component head = createAboveHunkHead();
		addOrReplace(head);
		target.add(head);
		
		Component previousLines = createAboveLines();
		addOrReplace(previousLines);
		target.add(previousLines);
	}
	
	@Override
	public void onDetach() {
		if (blobLinesModel != null) {
			blobLinesModel.detach();
		}
		
		super.onDetach();
	}
}
