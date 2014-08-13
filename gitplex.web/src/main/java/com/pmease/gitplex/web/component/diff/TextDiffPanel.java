package com.pmease.gitplex.web.component.diff;

import static de.agilecoders.wicket.jquery.JQuery.$;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.GitText;
import com.pmease.commons.util.diff.DiffChunk;
import com.pmease.commons.util.diff.DiffLine;
import com.pmease.commons.util.diff.DiffUtils;
import com.pmease.commons.util.diff.Partial;
import com.pmease.commons.util.diff.WordSplitter;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.menu.CheckMenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.commons.wicket.jquery.FunctionWithParams;

import de.agilecoders.wicket.jquery.JQuery;

@SuppressWarnings("serial")
public class TextDiffPanel extends Panel {

	private enum DiffOption {IGNORE_NOTHING, IGNORE_EOL, IGNORE_EOL_SPACES, IGNORE_CHANGE_SPACES};
	
	private static final int DEFAULT_CONTEXT_LINES = 5;
	
	private static final String HUNK_BODY_ID = "hunkLines";
	
	private static final String HUNK_HEAD_ID = "hunkHead";
	
	private final BlobDiffInfo diffInfo;
	
	private final GitText oldText;
	
	private final GitText newText;
	
	private GitText effectiveOldText;
	
	private GitText effectiveNewText;
	
	private DiffOption diffOption = DiffOption.IGNORE_NOTHING;
	
	private List<DiffLine> diffs;

	private List<DiffHunk> hunks;
	
	private int contextLines;
	
	private ListView<DiffHunk> hunksContainer;
	
	private AjaxLink<Void> viewFullLink;
	
	public TextDiffPanel(String id, BlobDiffInfo diffInfo, GitText oldText, GitText newText) {
		super(id);
		
		this.diffInfo = diffInfo;
		this.oldText = oldText;
		this.newText = newText;
		
		contextLines = DEFAULT_CONTEXT_LINES;
		
		onDiffOptionChanged();
	}
	
	private void onDiffOptionChanged() {
		if (diffOption == DiffOption.IGNORE_EOL) {
			effectiveOldText = oldText.ignoreEOL();
			effectiveNewText = newText.ignoreEOL();
		} else if (diffOption == DiffOption.IGNORE_EOL_SPACES) {
			effectiveOldText = oldText.ignoreEOLSpaces();
			effectiveNewText = newText.ignoreEOLSpaces();
		} else if (diffOption == DiffOption.IGNORE_CHANGE_SPACES) {
			effectiveOldText = oldText.ignoreChangeSpaces();
			effectiveNewText = newText.ignoreChangeSpaces();
		} else {
			effectiveOldText = oldText;
			effectiveNewText = newText;
		}
		
		diffs = DiffUtils.diff(effectiveOldText.getLines(), effectiveNewText.getLines(), new WordSplitter());
		onContextLineChanged();
	}

	private void onContextLineChanged() {
		hunks = new ArrayList<>();

		if (contextLines != 0) {
			for (DiffChunk chunk: DiffUtils.chunksOf(diffs, contextLines))
				hunks.add(DiffHunk.from(chunk));
		} else {
			hunks.add(DiffHunk.from(new DiffChunk(0, 0, diffs)));
		}
	}
	
	private boolean isIdentical() {
		for (DiffLine diffLine: diffs) {
			if (diffLine.getAction() != DiffLine.Action.EQUAL)
				return false;
		}
		return true;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new DiffStatBar("diffStat", new AbstractReadOnlyModel<List<DiffLine>>() {

			@Override
			public List<DiffLine> getObject() {
				return diffs;
			}
			
		}));
		
		List<String> alerts = new ArrayList<>();
		if (!oldText.getCharset().equals(newText.getCharset()))
			alerts.add("Charset is changed from " + oldText.getCharset() + " to " + newText.getCharset());
		if (!oldText.isHasEolAtEof())
			alerts.add("Original text does not have EOL character at EOF");
		if (!newText.isHasEolAtEof())
			alerts.add("Revised text does not have EOL character at EOF");
		
		add(new FileDiffTitle("title", diffInfo, alerts));
		
		add(new WebMarkupContainer("identical") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(isIdentical());
			}
			
		});
		
		add(viewFullLink = new AjaxLink<Void>("viewFull") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				contextLines = 0;
				onContextLineChanged();
				target.add(TextDiffPanel.this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				int lines = 0;
				for (DiffHunk hunk: hunks)
					lines += hunk.getLines().size();
				
				setVisible(lines < diffs.size());
			}
			
		});
		viewFullLink.setOutputMarkupId(true);
		
		MenuPanel diffOptionMenuPanel = new MenuPanel("diffOptions") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();
				
				menuItems.add(new CheckMenuItem() {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						onDiffOptionChanged();
						close(target);
						target.add(TextDiffPanel.this);
					}
					
					@Override
					protected String getLabel() {
						return "Ignore end of line differences";
					}
					
					@Override
					protected IModel<Boolean> getCheckModel() {
						return new IModel<Boolean>() {

							@Override
							public void detach() {
							}

							@Override
							public Boolean getObject() {
								return diffOption == DiffOption.IGNORE_EOL;
							}

							@Override
							public void setObject(Boolean object) {
								if (object)
									diffOption = DiffOption.IGNORE_EOL;
								else
									diffOption = DiffOption.IGNORE_NOTHING;
							}
							
						};
					}
				});
				
				menuItems.add(new CheckMenuItem() {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						onDiffOptionChanged();
						close(target);
						target.add(TextDiffPanel.this);
					}
					
					@Override
					protected String getLabel() {
						return "Ignore white spaces at line end";
					}
					
					@Override
					protected IModel<Boolean> getCheckModel() {
						return new IModel<Boolean>() {

							@Override
							public void detach() {
							}

							@Override
							public Boolean getObject() {
								return diffOption == DiffOption.IGNORE_EOL_SPACES;
							}

							@Override
							public void setObject(Boolean object) {
								if (object)
									diffOption = DiffOption.IGNORE_EOL_SPACES;
								else
									diffOption = DiffOption.IGNORE_NOTHING;
							}
							
						};
					}
				});

				menuItems.add(new CheckMenuItem() {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						onDiffOptionChanged();
						close(target);
						target.add(TextDiffPanel.this);
					}
					
					@Override
					protected String getLabel() {
						return "Ignore white space changes";
					}
					
					@Override
					protected IModel<Boolean> getCheckModel() {
						return new IModel<Boolean>() {

							@Override
							public void detach() {
							}

							@Override
							public Boolean getObject() {
								return diffOption == DiffOption.IGNORE_CHANGE_SPACES;
							}

							@Override
							public void setObject(Boolean object) {
								if (object)
									diffOption = DiffOption.IGNORE_CHANGE_SPACES;
								else
									diffOption = DiffOption.IGNORE_NOTHING;
							}
							
						};
					}
				});

				return menuItems;
			}
			
		};
		
		add(diffOptionMenuPanel);
		
		add(new WebMarkupContainer("diffOptionsTrigger").add(new MenuBehavior(diffOptionMenuPanel)));
		
		hunksContainer = new ListView<DiffHunk>("hunks", new AbstractReadOnlyModel<List<DiffHunk>>() {

			@Override
			public List<DiffHunk> getObject() {
				return hunks;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<DiffHunk> item) {
				DiffHunk hunk = item.getModelObject();
				
				RepeatingView hunkBody = new RepeatingView(HUNK_BODY_ID);
				item.add(newHeadContainer(HUNK_HEAD_ID, item.getIndex()));
				for (HunkLine line: hunk.getLines())
					hunkBody.add(newLineContainer(hunkBody.newChildId(), line));
				item.add(hunkBody);
			}

		};
		add(hunksContainer);
		
		final WebMarkupContainer hunkFoot = new WebMarkupContainer("hunkFoot") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (hunks.isEmpty()) {
					setVisible(false);
				} else {
					int lines = 0;
					for (DiffLine diffLine: diffs) {
						if (diffLine.getAction() != DiffLine.Action.DELETE)
							lines++;
					}
					DiffHunk hunk = hunks.get(hunks.size()-1);
					setVisible(hunk.getHeader().getNewEnd() < lines);
				}
			}
			
		};
		hunkFoot.setOutputMarkupId(true);
		add(hunkFoot);
		hunkFoot.add(new AjaxLink<Void>("expand") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				expandBelow(target, hunks.size()-1, diffs.size());
				target.add(findPreviousVisibleHunkHead(hunks.size()));
				target.add(hunkFoot);
				target.add(viewFullLink);
			}
			
		}.add(new TooltipBehavior(Model.of("Show more lines"))));
	}
	
	private WebMarkupContainer newLineContainer(String id, HunkLine line) {
		WebMarkupContainer lineContainer = new WebMarkupContainer(id);
		lineContainer.add(new AjaxLink<Void>("addComment") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				
			}
			
		});
		if (line.getDiffLine().getAction() == DiffLine.Action.ADD) {
			lineContainer.add(AttributeAppender.append("class", " revised"));
			lineContainer.add(new Label("originalLineNo"));
			lineContainer.add(new Label("revisedLineNo", "+ " + (line.getRevisedLineNo()+1)));
		} else if (line.getDiffLine().getAction() == DiffLine.Action.DELETE) {
			lineContainer.add(AttributeAppender.append("class", " original"));
			lineContainer.add(new Label("originalLineNo", "- " + (line.getOriginalLineNo()+1)));
			lineContainer.add(new Label("revisedLineNo"));
		} else {
			lineContainer.add(AttributeAppender.append("class", " equal"));
			lineContainer.add(new Label("originalLineNo", "  " + (line.getOriginalLineNo()+1)));
			lineContainer.add(new Label("revisedLineNo", "  " + (line.getRevisedLineNo()+1)));
		}
		lineContainer.add(new ListView<Partial>("partials", line.getDiffLine().getPartials()) {

			@Override
			protected void populateItem(ListItem<Partial> item) {
				Partial partial = item.getModelObject();
				Label label;
				if (partial.getContent().equals("\r"))
					label = new Label("partial", " ");
				else
					label = new Label("partial", partial.getContent());
				if (partial.isEmphasized())
					label.add(AttributeAppender.append("class", "emphasize"));
				item.add(label);
			}
			
		});
		lineContainer.setOutputMarkupId(true);
		return lineContainer;
	}
	
	private WebMarkupContainer newHeadContainer(String id, final int index) {
		final DiffHunk hunk = hunks.get(index);
		final WebMarkupContainer headContainer = new WebMarkupContainer(id) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(index == 0 || hunk.getHeader().getNewStart() > hunks.get(index-1).getHeader().getNewEnd());
			}
			
		};
		headContainer.add(new AjaxLink<Void>("expand") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (index == 0) {
					expandAbove(target, index, 0);
					target.add(headContainer);
				} else {
					int diffPos = locateDiffPos(hunk.getHeader().getNewStart(), DiffLine.Action.DELETE);
					expandBelow(target, index-1, diffPos);
					
					diffPos = locateDiffPos(hunks.get(index-1).getHeader().getNewEnd(), DiffLine.Action.DELETE);
					expandAbove(target, index, diffPos);
					
					target.add(headContainer);
					target.add(findPreviousVisibleHunkHead(index));
				}
				target.add(viewFullLink);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (index == 0) {
					setVisible(hunk.getHeader().getOldStart() != 0 && hunk.getHeader().getNewStart() != 0);
				} else {
					DiffHunk previousHunk = hunks.get(index-1);
					setVisible(previousHunk.getHeader().getNewEnd() < hunk.getHeader().getNewStart());
				}
			}
			
		}.add(new TooltipBehavior(Model.of("Show more lines"))));
		
		headContainer.add(new Label("content", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				int oldStart = hunk.getHeader().getOldStart();
				int newStart = hunk.getHeader().getNewStart();
				int oldEnd = hunk.getHeader().getOldEnd();
				int newEnd = hunk.getHeader().getNewEnd();
				for (int i=index+1; i<hunks.size(); i++) {
					HunkHeader nextHunkHeader = hunks.get(i).getHeader();
					if (newEnd == nextHunkHeader.getNewStart()) {
						newEnd = nextHunkHeader.getNewEnd();
						oldEnd = nextHunkHeader.getOldEnd();
					} else {
						break;
					}
				}
				return new HunkHeader(oldStart, oldEnd, newStart, newEnd).toString();
			}
			
		}));
		
		headContainer.setOutputMarkupId(true);
		
		return headContainer;
	}

	private int locateDiffPos(int lineNo, DiffLine.Action excludeAction) {
		int index = 0;
		for (int i=0; i<diffs.size(); i++) {
			if (index < lineNo) {
				if (diffs.get(i).getAction() != excludeAction)
					index++;
			} else {
				return i;
			}
		}
		throw new IllegalStateException();
	}
	
	private void expandBelow(AjaxRequestTarget target, int hunkIndex, int diffLimit) {
		DiffHunk hunk = hunks.get(hunkIndex);
		RepeatingView hunkBody = (RepeatingView) hunksContainer.get(hunkIndex).get(HUNK_BODY_ID);
		
		HunkHeader header = hunk.getHeader();
		
		int diffPos = locateDiffPos(header.getNewEnd(), DiffLine.Action.DELETE);
		for (int i = diffPos; i<diffs.size(); i++) {
			if (i-diffPos < DEFAULT_CONTEXT_LINES && i<diffLimit) {
				HunkLine line = new HunkLine(header.getOldEnd(), header.getNewEnd(), diffs.get(i));
				hunk.getLines().add(line);
				header.setNewEnd(header.getNewEnd()+1);
				header.setOldEnd(header.getOldEnd()+1);
				
				WebMarkupContainer lineComponent = newLineContainer(hunkBody.newChildId(), line);
				JQuery script = $(String.format("<tr id=\"%s\"></tr>", lineComponent.getMarkupId()));
				Component lastComponent = hunkBody.get(hunkBody.size()-1);
				script.chain(new FunctionWithParams("insertAfter", "'#" + lastComponent.getMarkupId() + "'"));
				target.prependJavaScript(script.get());
				hunkBody.add(lineComponent);
				target.add(lineComponent);
			} else {
				break;
			}			
		}
	}
	
	private Component findPreviousVisibleHunkHead(int index) {
		for (int i=index-1; i>=0; i--) {
			Component hunkHead = hunksContainer.get(i).get(HUNK_HEAD_ID);
			hunkHead.configure();
			if (hunkHead.isVisible())
				return hunkHead;
		}
		throw new IllegalStateException();
	}
	
	private void expandAbove(AjaxRequestTarget target, int hunkIndex, int diffLimit) {
		DiffHunk hunk = hunks.get(hunkIndex);
		RepeatingView hunkBody = (RepeatingView) hunksContainer.get(hunkIndex).get(HUNK_BODY_ID);
		HunkHeader header = hunk.getHeader();
		
		int diffPos = locateDiffPos(header.getNewStart(), DiffLine.Action.DELETE);
		for (int i=diffPos-1; i>=diffLimit; i--) {
			if (diffPos-i <= DEFAULT_CONTEXT_LINES) {
				header.setOldStart(header.getOldStart()-1);
				header.setNewStart(header.getNewStart()-1);
				HunkLine line = new HunkLine(header.getOldStart(), header.getNewStart(), diffs.get(i));
				hunk.getLines().add(0, line);
				WebMarkupContainer lineComponent = newLineContainer(hunkBody.newChildId(), line);
				JQuery script = $(String.format("<tr id=\"%s\"></tr>", lineComponent.getMarkupId()));
				script.chain(new FunctionWithParams("insertBefore", "'#" + hunkBody.get(0).getMarkupId() + "'"));
				target.prependJavaScript(script.get());
				hunkBody.add(lineComponent);
				for (int j=hunkBody.size()-1;j>0;j--)
					hunkBody.swap(j, j-1);
				target.add(lineComponent);
			} else {
				break;
			}
		}
	}
}
