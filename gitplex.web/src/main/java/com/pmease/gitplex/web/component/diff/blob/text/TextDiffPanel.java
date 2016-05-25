package com.pmease.gitplex.web.component.diff.blob.text;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.revwalk.RevCommit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobChange;
import com.pmease.commons.lang.diff.DiffBlock;
import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.diff.DiffUtils;
import com.pmease.commons.lang.diff.LineDiff;
import com.pmease.commons.lang.tokenizers.CmToken;
import com.pmease.commons.util.Range;
import com.pmease.commons.util.RangeUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext.Mode;
import com.pmease.gitplex.web.component.diff.blob.MarkAware;
import com.pmease.gitplex.web.component.diff.blob.text.MarkAwareDiffBlock.Type;
import com.pmease.gitplex.web.component.diff.diffstat.DiffStatBar;
import com.pmease.gitplex.web.component.diff.difftitle.BlobDiffTitle;
import com.pmease.gitplex.web.component.diff.revision.BlobMarkSupport;
import com.pmease.gitplex.web.component.diff.revision.DiffMark;
import com.pmease.gitplex.web.component.diff.revision.DiffViewMode;
import com.pmease.gitplex.web.component.symboltooltip.SymbolTooltipPanel;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;

@SuppressWarnings("serial")
public class TextDiffPanel extends Panel implements MarkAware {

	private final IModel<Depot> depotModel;
	
	private final IModel<PullRequest> requestModel;
	
	private final BlobChange change;
	
	private final Map<Integer, Integer> contextSizes = new HashMap<>();
	
	private final DiffViewMode diffMode;
	
	private final BlobMarkSupport markSupport;
	
	private Component symbolTooltip;
	
	private AbstractDefaultAjaxBehavior callbackBehavior;
	
	private transient List<MarkAwareDiffBlock> diffBlocks;
	
	public TextDiffPanel(String id, IModel<Depot> depotModel, IModel<PullRequest> requestModel, 
			BlobChange change, DiffViewMode diffMode, @Nullable BlobMarkSupport markSupport) {
		super(id);
		
		this.depotModel = depotModel;
		this.requestModel = requestModel;
		this.change = change;
		this.diffMode = diffMode;
		this.markSupport = markSupport;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new DiffStatBar("diffStat", change.getAdditions(), change.getDeletions(), true));
		add(new BlobDiffTitle("title", change));

		PullRequest request = requestModel.getObject();
		if (request != null) {
			DepotFilePage.State state = new DepotFilePage.State();
			state.requestId = request.getId();
			state.blobIdent = change.getBlobIdent();
			PageParameters params = DepotFilePage.paramsOf(request.getTargetDepot(), state);
			add(new BookmarkablePageLink<Void>("viewFile", DepotFilePage.class, params));
			state = new DepotFilePage.State();
			state.blobIdent.revision = request.getSourceBranch();
			state.blobIdent.path = change.getPath();
			state.mode = Mode.EDIT;
			params = DepotFilePage.paramsOf(request.getSourceDepot(), state);
			Link<Void> editFileLink = new BookmarkablePageLink<Void>("editFile", DepotFilePage.class, params) {
	
				@Override
				protected void onConfigure() {
					super.onConfigure();
					PullRequest request = requestModel.getObject();
					setVisible(request.getSourceDepot() != null 
							&& change.getBlobIdent().revision.equals(request.getSource().getObjectName(false)));
				}
				
			};
			editFileLink.add(AttributeAppender.append("target", "_blank"));
			add(editFileLink);
		} else {
			DepotFilePage.State state = new DepotFilePage.State();
			state.blobIdent = change.getBlobIdent();
			PageParameters params = DepotFilePage.paramsOf(depotModel.getObject(), state);
			add(new BookmarkablePageLink<Void>("viewFile", DepotFilePage.class, params));
			add(new WebMarkupContainer("editFile").setVisible(false));
		}
		
		add(new Label("diffLines", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return renderDiffs();
			}
			
		}).setEscapeModelStrings(false));
		
		add(callbackBehavior = new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				switch (params.getParameterValue("action").toString()) {
				case "expand":
					int index = params.getParameterValue("param1").toInt();
					Integer lastContextSize = contextSizes.get(index);
					if (lastContextSize == null)
						lastContextSize = Constants.DIFF_CONTEXT_SIZE;
					int contextSize = lastContextSize + Constants.DIFF_EXPAND_SIZE;
					contextSizes.put(index, contextSize);
					
					StringBuilder builder = new StringBuilder();
					appendEquals(builder, index, lastContextSize, contextSize);
					
					String expanded = StringUtils.replace(builder.toString(), "\"", "\\\"");
					expanded = StringUtils.replace(expanded, "\n", "");
					String script = String.format("gitplex.textdiff.expand('%s', %d, \"%s\");",
							getMarkupId(), index, expanded);
					target.appendJavaScript(script);
					break;
				case "openSelectionPopup":
					String jsonOfPosition = String.format("{left: %d, top: %d}", 
							params.getParameterValue("param1").toInt(), 
							params.getParameterValue("param2").toInt());
					DiffMark mark = getMark(params, "param3", "param4", "param5", "param6", "param7");
					script = String.format("gitplex.textdiff.openSelectionPopup('%s', %s, %s, '%s', %s);", 
							getMarkupId(), jsonOfPosition, mark.toJson(), markSupport.getMarkUrl(mark), 
							SecurityUtils.getAccount()!=null);
					target.appendJavaScript(script);
					break;
				case "addComment":
					Preconditions.checkNotNull(SecurityUtils.getAccount());
					
					mark = getMark(params, "param1", "param2", "param3", "param4", "param5");
					markSupport.onAddComment(target, mark);
					script = String.format("gitplex.textdiff.onAddComment($('#%s'), %s);", 
							getMarkupId(), mark.toJson());
					target.appendJavaScript(script);
					break;
				case "openComment": 
					Long commentId = params.getParameterValue("param1").toLong();
					CodeComment comment = GitPlex.getInstance(CodeCommentManager.class).load(commentId);
					markSupport.onOpenComment(target, comment);
					script = String.format("gitplex.textdiff.onOpenComment($('#%s'), %s);", 
							getMarkupId(), getJsonOfComment(comment));
					target.appendJavaScript(script);
					break;
				}
			}

		});
		
		symbolTooltip = new SymbolTooltipPanel("symbolTooltip", depotModel, requestModel) {

			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				setResponsePage(DepotFilePage.class, getQueryHitParams(hit));
			}

			@Override
			protected void onOccurrencesQueried(AjaxRequestTarget target, List<QueryHit> hits) {
				WebSession.get().setMetaData(DepotFilePage.SEARCH_RESULT_KEY, (ArrayList<QueryHit>)hits);
				setResponsePage(DepotFilePage.class, getFindOccurrencesParams());
			}

			@Override
			protected String getBlobPath() {
				if (getRevision().equals(change.getNewBlobIdent().revision))
					return change.getNewBlobIdent().path;
				else
					return change.getOldBlobIdent().path;
			}
			
		};
		add(symbolTooltip);
		
		setOutputMarkupId(true);
	}
	
	private DiffMark getMark(IRequestParameters params, String leftSideParam, 
			String beginLineParam, String beginCharParam, 
			String endLineParam, String endCharParam) {
		boolean leftSide = params.getParameterValue(leftSideParam).toBoolean();
		int beginLine = params.getParameterValue(beginLineParam).toInt();
		int beginChar = params.getParameterValue(beginCharParam).toInt();
		int endLine = params.getParameterValue(endLineParam).toInt();
		int endChar = params.getParameterValue(endCharParam).toInt();
		return new DiffMark(change.getPath(), leftSide, beginLine, beginChar, endLine, endChar);
	}
	
	private void appendEquals(StringBuilder builder, int index, int lastContextSize, int contextSize) {
		MarkAwareDiffBlock block = getDiffBlocks().get(index);
		if (index == 0) {
			int start = block.getLines().size()-contextSize;
			if (start < 0)
				start=0;
			else if (start > 0)
				appendExpander(builder, index, start);
			for (int j=start; j<block.getLines().size()-lastContextSize; j++) 
				appendEqual(builder, block, j, lastContextSize);
		} else if (index == getDiffBlocks().size()-1) {
			int end = block.getLines().size();
			int skipped = 0;
			if (end > contextSize) {
				skipped = end-contextSize;
				end = contextSize;
			}
			for (int j=lastContextSize; j<end; j++)
				appendEqual(builder, block, j, lastContextSize);
			if (skipped != 0)
				appendExpander(builder, index, skipped);
		} else if (2*contextSize < block.getLines().size()) {
			for (int j=lastContextSize; j<contextSize; j++)
				appendEqual(builder, block, j, lastContextSize);
			appendExpander(builder, index, block.getLines().size() - 2*contextSize);
			for (int j=block.getLines().size()-contextSize; j<block.getLines().size()-lastContextSize; j++)
				appendEqual(builder, block, j, lastContextSize);
		} else {
			for (int j=lastContextSize; j<block.getLines().size()-lastContextSize; j++)
				appendEqual(builder, block, j, lastContextSize);
		}
	}
	
	private void appendMarkedEquals(StringBuilder builder, int index) {
		MarkAwareDiffBlock block = getDiffBlocks().get(index);
		for (int i=0; i<block.getLines().size(); i++)
			appendEqual(builder, block, i, 0);
	}
	
	private RevCommit getOldCommit() {
		return depotModel.getObject().getRevCommit(change.getOldBlobIdent().revision);
	}
	
	private RevCommit getNewCommit() {
		return depotModel.getObject().getRevCommit(change.getNewBlobIdent().revision);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(TextDiffPanel.class, "text-diff.js")));
		response.render(CssHeaderItem.forReference(
				new WebjarsCssResourceReference("codemirror/current/theme/eclipse.css")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(TextDiffPanel.class, "text-diff.css")));
		
		String jsonOfOldCommentInfos;
		String jsonOfNewCommentInfos;
		String jsonOfMark;
		String jsonOfCommentInfo;
		String dirtyContainerId;
		if (markSupport != null) {
			String oldCommitHash = getOldCommit().name();
			String newCommitHash = getNewCommit().name();
			Map<Integer, List<CommentInfo>> oldCommentInfos = new HashMap<>(); 
			Map<Integer, List<CommentInfo>> newCommentInfos = new HashMap<>(); 
			for (CodeComment comment: markSupport.getComments()) {
				if (comment.getMark() != null) {
					int line = comment.getMark().getBeginLine();
					List<CommentInfo> commentInfosAtLine;
					CommentInfo commentInfo = new CommentInfo();
					commentInfo.id = comment.getId();
					commentInfo.mark = new DiffMark(comment, oldCommitHash, newCommitHash);
					if (commentInfo.mark.isLeftSide()) {
						commentInfosAtLine = oldCommentInfos.get(line);
						if (commentInfosAtLine == null) {
							commentInfosAtLine = new ArrayList<>();
							oldCommentInfos.put(line, commentInfosAtLine);
						}
					} else {
						commentInfosAtLine = newCommentInfos.get(line);
						if (commentInfosAtLine == null) {
							commentInfosAtLine = new ArrayList<>();
							newCommentInfos.put(line, commentInfosAtLine);
						}
					}
					commentInfosAtLine.add(commentInfo);
				}
			}
			for (List<CommentInfo> value: oldCommentInfos.values()) {
				value.sort((o1, o2)->(int)(o1.id-o2.id));
			}
			for (List<CommentInfo> value: newCommentInfos.values()) {
				value.sort((o1, o2)->(int)(o1.id-o2.id));
			}
			
			try {
				jsonOfOldCommentInfos = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(oldCommentInfos);
				jsonOfNewCommentInfos = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(newCommentInfos);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
			
			DiffMark mark = markSupport.getMark();
			if (mark != null) {
				jsonOfMark = mark.toJson();
			} else {
				jsonOfMark = "undefined";
			}
			CodeComment comment = markSupport.getOpenComment();
			if (comment != null) {
				jsonOfCommentInfo = getJsonOfComment(comment);
			} else {
				jsonOfCommentInfo = "undefined";
			}
			dirtyContainerId = "'" + markSupport.getDirtyContainer().getMarkupId() + "'";
		} else {
			jsonOfMark = "undefined";
			jsonOfCommentInfo = "undefined";
			jsonOfOldCommentInfos = "undefined";
			jsonOfNewCommentInfos = "undefined";
			dirtyContainerId = "undefined";
		}
		
		CharSequence callback = callbackBehavior.getCallbackFunction(
				explicit("action"), explicit("param1"), explicit("param2"), 
				explicit("param3"), explicit("param4"), explicit("param5"),
				explicit("param6"), explicit("param7"), explicit("param8")); 
		String script = String.format("gitplex.textdiff.init('%s', '%s', '%s', '%s', %s, %s, %s, %s, %s, %s, %s, %s);", 
				getMarkupId(), symbolTooltip.getMarkupId(), 
				change.getOldBlobIdent().revision, change.getNewBlobIdent().revision,
				RequestCycle.get().find(AjaxRequestTarget.class) == null, callback, 
				markSupport!=null, jsonOfMark, jsonOfCommentInfo, 
				jsonOfOldCommentInfos, jsonOfNewCommentInfos, dirtyContainerId);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	private String renderDiffs() {
		int contextSize = Constants.DIFF_CONTEXT_SIZE;
		StringBuilder builder = new StringBuilder();
		if (diffMode == DiffViewMode.UNIFIED) {
			builder.append(""
					+ "<colgroup>"
					+ "<col width='60'></col>"
					+ "<col width='60'></col>"
					+ "<col width='15'></col>"
					+ "<col></col>"
					+ "</colgroup>");
		} else {
			builder.append(""
					+ "<colgroup>"
					+ "<col width='60'></col>"
					+ "<col width='15'></col>"
					+ "<col></col>"
					+ "<col width='60'></col>"
					+ "<col width='15'></col>"
					+ "<col></col>"
					+ "</colgroup>");
		}
		for (int i=0; i<getDiffBlocks().size(); i++) {
			MarkAwareDiffBlock block = getDiffBlocks().get(i);
			if (block.getType() == Type.EQUAL) {
				appendEquals(builder, i, 0, contextSize);
			} else if (block.getType() == Type.MARKED_EQUAL) {
				appendMarkedEquals(builder, i);
			} else if (block.getType () == Type.DELETE) {
				if (i+1<getDiffBlocks().size()) {
					MarkAwareDiffBlock nextBlock = getDiffBlocks().get(i+1);
					if (nextBlock.getType() == Type.INSERT) {
						LinkedHashMap<Integer, LineDiff> lineChanges = 
								DiffUtils.align(block.getLines(), nextBlock.getLines());
						int prevDeleteLineIndex = 0;
						int prevInsertLineIndex = 0;
						for (Map.Entry<Integer, LineDiff> entry: lineChanges.entrySet()) {
							int deleteLineIndex = entry.getKey();
							LineDiff lineChange = entry.getValue();
							int insertLineIndex = lineChange.getCompareLine();
							
							appendDeletesAndInserts(builder, block, nextBlock, prevDeleteLineIndex, deleteLineIndex, 
									prevInsertLineIndex, insertLineIndex);
							
							appendModification(builder, block, nextBlock, deleteLineIndex, insertLineIndex, lineChange.getTokenDiffs()); 
							
							prevDeleteLineIndex = deleteLineIndex+1;
							prevInsertLineIndex = insertLineIndex+1;
						}
						appendDeletesAndInserts(builder, block, nextBlock, 
								prevDeleteLineIndex, block.getLines().size(), 
								prevInsertLineIndex, nextBlock.getLines().size());
						i++;
					} else {
						for (int j=0; j<block.getLines().size(); j++) 
							appendDelete(builder, block, j);
					}
				} else {
					for (int j=0; j<block.getLines().size(); j++) 
						appendDelete(builder, block, j);
				}
			} else {
				for (int j=0; j<block.getLines().size(); j++) 
					appendInsert(builder, block, j);
			}
		}
		return builder.toString();
	}

	private void appendDeletesAndInserts(StringBuilder builder, MarkAwareDiffBlock deleteBlock, 
			MarkAwareDiffBlock insertBlock, int fromDeleteLineIndex, int toDeleteLineIndex, 
			int fromInsertLineIndex, int toInsertLineIndex) {
		if (diffMode == DiffViewMode.UNIFIED) {
			for (int i=fromDeleteLineIndex; i<toDeleteLineIndex; i++)
				appendDelete(builder, deleteBlock, i);
			for (int i=fromInsertLineIndex; i<toInsertLineIndex; i++)
				appendInsert(builder, insertBlock, i);
		} else {
			int deleteSize = toDeleteLineIndex - fromDeleteLineIndex;
			int insertSize = toInsertLineIndex - fromInsertLineIndex;
			if (deleteSize < insertSize) {
				for (int i=fromDeleteLineIndex; i<toDeleteLineIndex; i++) 
					appendSideBySide(builder, deleteBlock, insertBlock, i, i-fromDeleteLineIndex+fromInsertLineIndex);
				for (int i=fromInsertLineIndex+deleteSize; i<toInsertLineIndex; i++)
					appendInsert(builder, insertBlock, i);
			} else {
				for (int i=fromInsertLineIndex; i<toInsertLineIndex; i++) 
					appendSideBySide(builder, deleteBlock, insertBlock, i-fromInsertLineIndex+fromDeleteLineIndex, i);
				for (int i=fromDeleteLineIndex+insertSize; i<toDeleteLineIndex; i++)
					appendDelete(builder, deleteBlock, i);
			}
		}
	}
	
	private void appendEqual(StringBuilder builder, MarkAwareDiffBlock block, int lineIndex, int lastContextSize) {
		if (lastContextSize != 0)
			builder.append("<tr class='code expanded'>");
		else
			builder.append("<tr class='code original'>");

		int oldLineNo = block.getOldStart() + lineIndex;
		int newLineNo = block.getNewStart() + lineIndex;
		
		if (diffMode == DiffViewMode.UNIFIED) {
			builder.append("<td class='number noselect'>").append(oldLineNo+1).append("</td>");
			builder.append("<td class='number noselect'>").append(newLineNo+1).append("</td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			List<CmToken> tokens = block.getLines().get(lineIndex);
			if (tokens.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (CmToken token: tokens) {
					builder.append(token.toHtml(Operation.EQUAL));
				}
			}
			builder.append("</td>");
		} else {
			builder.append("<td class='number noselect'>").append(oldLineNo+1).append("</td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content left' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			List<CmToken> tokens = block.getLines().get(lineIndex);
			if (tokens.isEmpty()) {
				builder.append("&nbsp;");
			} else {			
				for (CmToken token: tokens) {
					builder.append(token.toHtml(Operation.EQUAL));
				}
			}
			builder.append("</td>");
			builder.append("<td class='number noselect'>").append(newLineNo+1).append("</td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content right' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			tokens = block.getLines().get(lineIndex);
			if (tokens.isEmpty()) {
				builder.append("&nbsp;");
			} else {			
				for (CmToken token: tokens) {
					builder.append(token.toHtml(Operation.EQUAL));
				}
			}
			builder.append("</td>");
		}
		builder.append("</tr>");
	}
	
	private void appendInsert(StringBuilder builder, MarkAwareDiffBlock block, int lineIndex) {
		builder.append("<tr class='code original'>");

		int newLineNo = block.getNewStart() + lineIndex;
		if (diffMode == DiffViewMode.UNIFIED) {
			builder.append("<td class='number noselect new'>&nbsp;</td>");
			builder.append("<td class='number noselect new'>").append(newLineNo+1).append("</td>");
			builder.append("<td class='operation noselect new'>+</td>");
			builder.append("<td class='content new' data-new='").append(newLineNo).append("'>");
			List<CmToken> tokens = block.getLines().get(lineIndex);
			if (tokens.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (int i=0; i<tokens.size(); i++) 
					builder.append(tokens.get(i).toHtml(Operation.EQUAL));
			}
			builder.append("</td>");
		} else {
			builder.append("<td class='number noselect'>&nbsp;</td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content left'>&nbsp;</td>");
			builder.append("<td class='number noselect new'>").append(newLineNo+1).append("</td>");
			builder.append("<td class='operation noselect new'>+</td>");
			builder.append("<td class='content right new' data-new='").append(newLineNo).append("'>");
			List<CmToken> tokens = block.getLines().get(lineIndex);
			if (tokens.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (int i=0; i<tokens.size(); i++) 
					builder.append(tokens.get(i).toHtml(Operation.EQUAL));
			}
			builder.append("</td>");
		}
		builder.append("</tr>");
	}
	
	private void appendDelete(StringBuilder builder, MarkAwareDiffBlock block, int lineIndex) {
		builder.append("<tr class='code original'>");
		
		int oldLineNo = block.getOldStart() + lineIndex;
		if (diffMode == DiffViewMode.UNIFIED) {
			builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("</td>");
			builder.append("<td class='number noselect old'>&nbsp;</td>");
			builder.append("<td class='operation noselect old'>-</td>");
			builder.append("<td class='content old' data-old='").append(oldLineNo).append("'>");
			List<CmToken> tokens = block.getLines().get(lineIndex);
			if (tokens.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (int i=0; i<tokens.size(); i++) 
					builder.append(tokens.get(i).toHtml(Operation.EQUAL));
			}
			builder.append("</td>");
		} else {
			builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("</td>");
			builder.append("<td class='operation noselect old'>-</td>");
			builder.append("<td class='content left old' data-old='").append(oldLineNo).append("'>");
			List<CmToken> tokens = block.getLines().get(lineIndex);
			if (tokens.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (int i=0; i<tokens.size(); i++) 
					builder.append(tokens.get(i).toHtml(Operation.EQUAL));
			}
			builder.append("</td>");
			builder.append("<td class='number noselect'>&nbsp;</td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content right'>&nbsp;</td>");
		}
		builder.append("</tr>");
	}
	
	private void appendSideBySide(StringBuilder builder, MarkAwareDiffBlock deleteBlock, 
			MarkAwareDiffBlock insertBlock, int deleteLineIndex, int insertLineIndex) {
		builder.append("<tr class='code original'>");

		int oldLineNo = deleteBlock.getOldStart()+deleteLineIndex;
		builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("</td>");
		builder.append("<td class='operation noselect old'>-</td>");
		builder.append("<td class='content left old' data-old='").append(oldLineNo).append("'>");
		List<CmToken> tokens = deleteBlock.getLines().get(deleteLineIndex);
		if (tokens.isEmpty()) {
			builder.append("&nbsp;");
		} else {
			for (CmToken token: tokens)
				builder.append(token.toHtml(Operation.EQUAL));
		}
		builder.append("</td>");
		
		int newLineNo = insertBlock.getNewStart()+insertLineIndex;
		builder.append("<td class='number noselect new'>").append(newLineNo+1).append("</td>");
		builder.append("<td class='operation noselect new'>+</td>");
		builder.append("<td class='content right new' data-new='").append(newLineNo).append("'>");
		tokens = insertBlock.getLines().get(insertLineIndex);
		if (tokens.isEmpty()) {
			builder.append("&nbsp;");
		} else {
			for (CmToken token: tokens)
				builder.append(token.toHtml(Operation.EQUAL));
		}
		builder.append("</td>");
		
		builder.append("</tr>");
	}

	private void appendModification(StringBuilder builder, MarkAwareDiffBlock deleteBlock, 
			MarkAwareDiffBlock insertBlock, int deleteLineIndex, int insertLineIndex, 
			List<DiffBlock<CmToken>> tokenDiffs) {
		builder.append("<tr class='code original'>");

		int oldLineNo = deleteBlock.getOldStart() + deleteLineIndex;
		int newLineNo = insertBlock.getNewStart() + insertLineIndex;
		if (diffMode == DiffViewMode.UNIFIED) {
			builder.append("<td class='number noselect old new'>").append(oldLineNo+1).append("</td>");
			builder.append("<td class='number noselect old new'>").append(newLineNo+1).append("</td>");
			builder.append("<td class='operation noselect old new'>*</td>");
			builder.append("<td class='content old new' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			if (tokenDiffs.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (DiffBlock<CmToken> tokenBlock: tokenDiffs) { 
					for (CmToken token: tokenBlock.getUnits()) 
						builder.append(token.toHtml(tokenBlock.getOperation()));
				}
			}
			builder.append("</td>");
		} else {
			builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("</td>");
			builder.append("<td class='operation noselect old'>-</td>");
			builder.append("<td class='content left old' data-old='").append(oldLineNo).append("'>");
			if (tokenDiffs.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (DiffBlock<CmToken> tokenBlock: tokenDiffs) { 
					for (CmToken token: tokenBlock.getUnits()) {
						if (tokenBlock.getOperation() != Operation.INSERT) 
							builder.append(token.toHtml(tokenBlock.getOperation()));
					}
				}
			}
			builder.append("</td>");
			
			builder.append("<td class='number noselect new'>").append(newLineNo+1).append("</td>");
			builder.append("<td class='operation noselect new'>+</td>");
			builder.append("<td class='content right new' data-new='").append(newLineNo).append("'>");
			if (tokenDiffs.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (DiffBlock<CmToken> tokenBlock: tokenDiffs) { 
					for (CmToken token: tokenBlock.getUnits()) {
						if (tokenBlock.getOperation() != Operation.DELETE) 
							builder.append(token.toHtml(tokenBlock.getOperation()));
					}
				}
			}			
			builder.append("</td>");
		}
		
		builder.append("</tr>");
	}
	
	private void appendExpander(StringBuilder builder, int blockIndex, int skippedLines) {
		builder.append("<tr class='expander expander").append(blockIndex).append("'>");
		
		String script = String.format("javascript: "
				+ "var callback = $('#%s').data('callback');"
				+ "if (callback) callback('expand', %d);", 
				getMarkupId(), blockIndex);
		if (diffMode == DiffViewMode.UNIFIED) {
			builder.append("<td colspan='2' class='expander noselect'><a title='Show more lines' href=\"")
					.append(script).append("\"><i class='fa fa-sort'></i></a></td>");
			builder.append("<td colspan='2' class='skipped noselect'><i class='fa fa-ellipsis-h'></i> skipped ")
					.append(skippedLines).append(" lines <i class='fa fa-ellipsis-h'></i></td>");
		} else {
			builder.append("<td class='expander noselect'><a title='Show more lines' href=\"").append(script)
					.append("\"><i class='fa fa-sort'></i></a></td>");
			builder.append("<td class='skipped noselect' colspan='5'><i class='fa fa-ellipsis-h'></i> skipped ")
					.append(skippedLines).append(" lines <i class='fa fa-ellipsis-h'></i></td>");
		}
		builder.append("</tr>");
	}
	
	private List<MarkAwareDiffBlock> getDiffBlocks() {
		if (diffBlocks == null) {
			diffBlocks = new ArrayList<>();
			List<Range> oldRanges = new ArrayList<>();
			List<Range> newRanges = new ArrayList<>();
			if (markSupport != null) {
				List<DiffMark> marks = new ArrayList<>();
				DiffMark mark = markSupport.getMark();
				if (mark != null) {
					marks.add(mark);
				}
				String oldCommitHash = getOldCommit().name();
				String newCommitHash = getNewCommit().name();
				for (CodeComment comment: markSupport.getComments()) {
					mark = new DiffMark(comment, oldCommitHash, newCommitHash);
					marks.add(mark);
				}
				for (DiffMark each: marks) {
					Range range = new Range(each.beginLine, each.endLine+1);
					if (each.isLeftSide()) {
						oldRanges.add(range);
					} else {
						newRanges.add(range);
					}
				}
			}
			
			for (DiffBlock<List<CmToken>> diffBlock: change.getDiffBlocks()) {
				if (diffBlock.getOperation() == Operation.DELETE) {
					diffBlocks.add(new MarkAwareDiffBlock(Type.DELETE, diffBlock.getUnits(), 
							diffBlock.getOldStart(), diffBlock.getNewStart()));
				} else if (diffBlock.getOperation() == Operation.INSERT) {
					diffBlocks.add(new MarkAwareDiffBlock(Type.INSERT, diffBlock.getUnits(), 
							diffBlock.getOldStart(), diffBlock.getNewStart()));
				} else {
					List<Range> ranges = new ArrayList<>();
					for (Range range: oldRanges) {
						ranges.add(new Range(range.getFrom()-diffBlock.getOldStart(), range.getTo()-diffBlock.getOldStart()));
					}
					for (Range range: newRanges) {
						ranges.add(new Range(range.getFrom()-diffBlock.getNewStart(), range.getTo()-diffBlock.getNewStart()));
					}
					ranges = RangeUtils.merge(ranges);
					
					int lastIndex = 0;
					for (Range range: ranges) {
						int from = range.getFrom();
						int to = range.getTo();
						if (from < diffBlock.getUnits().size() && to > 0) {
							if (from < lastIndex)
								from = lastIndex;
							if (to > diffBlock.getUnits().size())
								to = diffBlock.getUnits().size();
							if (lastIndex < from) {
								List<List<CmToken>> lines = diffBlock.getUnits().subList(lastIndex, from);
								diffBlocks.add(new MarkAwareDiffBlock(Type.EQUAL, lines, 
										diffBlock.getOldStart()+lastIndex, diffBlock.getNewStart()+lastIndex));
							}
							if (from < to) {
								List<List<CmToken>> lines = diffBlock.getUnits().subList(from, to);
								diffBlocks.add(new MarkAwareDiffBlock(Type.MARKED_EQUAL, lines, 
										diffBlock.getOldStart()+from, diffBlock.getNewStart()+from));
							}
							lastIndex = to;
						}
					}
					
					if (lastIndex < diffBlock.getUnits().size()) {
						List<List<CmToken>> lines = diffBlock.getUnits().subList(lastIndex, diffBlock.getUnits().size());
						diffBlocks.add(new MarkAwareDiffBlock(Type.EQUAL, lines, 
								diffBlock.getOldStart()+lastIndex, diffBlock.getNewStart()+lastIndex));
					}
				}
			}
		}
		return diffBlocks;
	}
	
	@Override
	protected void onDetach() {
		depotModel.detach();
		requestModel.detach();
		
		super.onDetach();
	}

	private String getJsonOfComment(CodeComment comment) {
		CommentInfo commentInfo = new CommentInfo();
		commentInfo.id = comment.getId();
		commentInfo.mark = new DiffMark(comment, getOldCommit().name(), getNewCommit().name());

		String jsonOfCommentInfo;
		try {
			jsonOfCommentInfo = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(commentInfo);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return jsonOfCommentInfo;
	}
	
	@Override
	public void onCommentDeleted(AjaxRequestTarget target, CodeComment comment) {
		String script = String.format("gitplex.textdiff.onCommentDeleted($('#%s'), %s);", 
				getMarkupId(), getJsonOfComment(comment));
		target.appendJavaScript(script);
	}

	@Override
	public void onCommentClosed(AjaxRequestTarget target, CodeComment comment) {
		String script = String.format("gitplex.textdiff.onCloseComment($('#%s'));", getMarkupId());
		target.appendJavaScript(script);
	}

	@Override
	public void onCommentAdded(AjaxRequestTarget target, CodeComment comment) {
		String script = String.format("gitplex.textdiff.onCommentAdded($('#%s'), %s);", 
				getMarkupId(), getJsonOfComment(comment));
		target.appendJavaScript(script);
	}

	@Override
	public void mark(AjaxRequestTarget target, DiffMark mark) {
		String script = String.format(""
				+ "var $container = $('#%s');"
				+ "var mark = %s;"
				+ "gitplex.textdiff.scroll($container, mark);"
				+ "gitplex.textdiff.mark($container, mark);", 
				getMarkupId(), mark.toJson());
		target.appendJavaScript(script);
	}
	
	@Override
	public void clearMark(AjaxRequestTarget target) {
		String script = String.format(""
				+ "var $container = $('#%s');"
				+ "gitplex.textdiff.clearMark($container);"
				+ "$container.removeData('mark');", 
				getMarkupId());
		target.appendJavaScript(script);
	}
	
	private static class CommentInfo {
		long id;
		
		DiffMark mark;
	}

}
