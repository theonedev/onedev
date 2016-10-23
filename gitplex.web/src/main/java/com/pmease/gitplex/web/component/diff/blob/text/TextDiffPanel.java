package com.pmease.gitplex.web.component.diff.blob.text;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.revwalk.RevCommit;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.Blame;
import com.pmease.commons.git.BlobChange;
import com.pmease.commons.git.BriefCommit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.command.BlameCommand;
import com.pmease.commons.lang.diff.DiffBlock;
import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.diff.DiffUtils;
import com.pmease.commons.lang.diff.LineDiff;
import com.pmease.commons.lang.tokenizers.CmToken;
import com.pmease.commons.util.Range;
import com.pmease.commons.util.RangeUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.AbstractPostAjaxBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.support.CommentPos;
import com.pmease.gitplex.core.entity.support.TextRange;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.behavior.BlameMessageBehavior;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext.Mode;
import com.pmease.gitplex.web.component.diff.blob.SourceAware;
import com.pmease.gitplex.web.component.diff.blob.text.MarkAwareDiffBlock.Type;
import com.pmease.gitplex.web.component.diff.diffstat.DiffStatBar;
import com.pmease.gitplex.web.component.diff.difftitle.BlobDiffTitle;
import com.pmease.gitplex.web.component.diff.revision.BlobCommentSupport;
import com.pmease.gitplex.web.component.diff.revision.DiffViewMode;
import com.pmease.gitplex.web.component.symboltooltip.SymbolTooltipPanel;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.file.RequestCompareInfo;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;
import com.pmease.gitplex.web.util.DateUtils;

@SuppressWarnings("serial")
public class TextDiffPanel extends Panel implements SourceAware {

	private final IModel<Depot> depotModel;
	
	private final IModel<PullRequest> requestModel;
	
	private final BlobChange change;
	
	private final Map<Integer, Integer> contextSizes = new HashMap<>();
	
	private final DiffViewMode diffMode;

	private final IModel<Boolean> blameModel;
	
	private final BlobCommentSupport markSupport;
	
	private Component symbolTooltip;
	
	private AbstractPostAjaxBehavior callbackBehavior;
	
	private BlameMessageBehavior blameMessageBehavior;
	
	private transient List<MarkAwareDiffBlock> diffBlocks;
	
	private BlameInfo blameInfo;
	
	public TextDiffPanel(String id, IModel<Depot> depotModel, IModel<PullRequest> requestModel, 
			BlobChange change, DiffViewMode diffMode, @Nullable IModel<Boolean> blameModel, 
			@Nullable BlobCommentSupport markSupport) {
		super(id);
		
		this.depotModel = depotModel;
		this.requestModel = requestModel;
		this.change = change;
		this.diffMode = diffMode;
		this.markSupport = markSupport;
		this.blameModel = blameModel;
		
		if (blameModel != null && blameModel.getObject()) {
			blameInfo = getBlameInfo();
		}
	}

	private String getJson(CommentPos mark) {
		try {
			MarkInfo markInfo = new MarkInfo(mark, getOldCommit().name());
			return GitPlex.getInstance(ObjectMapper.class).writeValueAsString(markInfo);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private BlameInfo getBlameInfo() {
		blameInfo = new BlameInfo();
		BlameCommand cmd = new BlameCommand(depotModel.getObject().getDirectory());
		String oldPath = change.getOldBlobIdent().path;
		if (oldPath != null) {
			cmd.commitHash(getOldCommit().name()).file(oldPath);
			for (Blame blame: cmd.call().values()) {
				for (Range range: blame.getRanges()) {
					for (int i=range.getFrom(); i<range.getTo(); i++) 
						blameInfo.oldBlame.put(i, blame.getCommit());
				}
			}
		}
		String newPath = change.getNewBlobIdent().path;
		if (newPath != null) {
			cmd.commitHash(getNewCommit().name()).file(newPath);
			for (Blame blame: cmd.call().values()) {
				for (Range range: blame.getRanges()) {
					for (int i=range.getFrom(); i<range.getTo(); i++) 
						blameInfo.newBlame.put(i, blame.getCommit());
				}
			}
		}
		return blameInfo;
	}

	private WebMarkupContainer newActions() {
		WebMarkupContainer actions = new WebMarkupContainer("actions");

		actions.add(new AjaxLink<Void>("blameFile") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(blameModel != null);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (blameInfo != null) {
					blameInfo = null;
				} else {
					blameInfo = getBlameInfo();
				}
				target.add(TextDiffPanel.this);
				blameModel.setObject(blameInfo != null);
			}
			
		}.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return blameInfo!=null? "active": "";
			}
			
		})).setOutputMarkupId(true));
		
		Depot depot = depotModel.getObject();
		PullRequest request = requestModel.getObject();
		CodeComment comment;
		if (markSupport != null && markSupport.getOpenComment() != null) {
			comment = markSupport.getOpenComment();
		} else {
			comment = null;
		}
		DepotFilePage.State viewState = new DepotFilePage.State();
		if (comment != null)
			viewState.commentId = comment.getId();
		viewState.blobIdent = change.getBlobIdent();
		
		PageParameters params = DepotFilePage.paramsOf(depot, viewState);
		actions.add(new BookmarkablePageLink<Void>("viewFile", DepotFilePage.class, params));
		
		if (change.getType() != ChangeType.DELETE) {
			if (request != null 
					&& request.getSource() != null 
					&& request.getSource().getObjectName(false) != null
					&& SecurityUtils.canModify(request.getSourceDepot(), request.getSourceBranch(), change.getPath())) { 
				// we are in context of a pull request and pull request source branch exists, so we edit source branch instead
				Link<Void> editLink = new Link<Void>("editFile") {

					@Override
					public void onClick() {
						DepotFilePage.State editState = new DepotFilePage.State();
						editState.blobIdent.revision = request.getSourceBranch();
						editState.blobIdent.path = change.getPath();
						editState.mode = Mode.EDIT;
						editState.requestCompareInfo = new RequestCompareInfo();
						editState.requestCompareInfo.requestId = request.getId();
						RequestChangesPage page = (RequestChangesPage) getPage();
						editState.requestCompareInfo.compareState = page.getState();
						PageParameters params = DepotFilePage.paramsOf(depot, editState);
						setResponsePage(DepotFilePage.class, params);
					}
					
				};
				editLink.add(AttributeAppender.append("title", "Edit on source branch"));
				actions.add(editLink);
			} else if (SecurityUtils.canModify(depot, change.getBlobIdent().revision, change.getPath()) 
					&& depot.getBranchRef(change.getBlobIdent().revision) != null) {
				// we are on a branch 
				Link<Void> editLink = new Link<Void>("editFile") {

					@Override
					public void onClick() {
						DepotFilePage.State editState = new DepotFilePage.State();
						editState.blobIdent = change.getBlobIdent();
						editState.mode = Mode.EDIT;
						PageParameters params = DepotFilePage.paramsOf(depot, editState);
						setResponsePage(DepotFilePage.class, params);
					}
					
				};
				editLink.add(AttributeAppender.append("title", "Edit on branch " + change.getBlobIdent().revision));
				actions.add(editLink);
			} else {
				actions.add(new WebMarkupContainer("editFile").setVisible(false));
			}
		} else {
			actions.add(new WebMarkupContainer("editFile").setVisible(false));
		}
		
		return actions;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new DiffStatBar("diffStat", change.getAdditions(), change.getDeletions(), true));
		add(new BlobDiffTitle("title", change));

		add(newActions());
		
		add(new Label("diffLines", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return renderDiffs();
			}
			
		}).setEscapeModelStrings(false));
		
		add(callbackBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				switch (params.getParameterValue("action").toString()) {
				case "expand":
					if (blameInfo != null) {
						blameInfo.lastCommitHash = null;
						blameInfo.lastOldCommitHash = null;
						blameInfo.lastNewCommitHash = null;
					}
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
				case "openSelectionPopover":
					String jsonOfPosition = String.format("{left: %d, top: %d}", 
							params.getParameterValue("param1").toInt(), 
							params.getParameterValue("param2").toInt());
					CommentPos mark = getMark(params, "param3", "param4", "param5", "param6", "param7");
					script = String.format("gitplex.textdiff.openSelectionPopover('%s', %s, %s, '%s', %s);", 
							getMarkupId(), jsonOfPosition, getJson(mark), markSupport.getMarkUrl(mark), 
							SecurityUtils.getAccount()!=null);
					target.appendJavaScript(script);
					break;
				case "addComment":
					Preconditions.checkNotNull(SecurityUtils.getAccount());
					
					mark = getMark(params, "param1", "param2", "param3", "param4", "param5");
					markSupport.onAddComment(target, mark);
					script = String.format("gitplex.textdiff.onAddComment($('#%s'), %s);", 
							getMarkupId(), getJson(mark));
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
		
		add(blameMessageBehavior = new BlameMessageBehavior() {
			
			@Override
			protected Depot getDepot() {
				return depotModel.getObject();
			}
		});
		
		symbolTooltip = new SymbolTooltipPanel("symbolTooltip", depotModel) {

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
	
	private CommentPos getMark(IRequestParameters params, String leftSideParam, 
			String beginLineParam, String beginCharParam, 
			String endLineParam, String endCharParam) {
		boolean leftSide = params.getParameterValue(leftSideParam).toBoolean();
		int beginLine = params.getParameterValue(beginLineParam).toInt();
		int beginChar = params.getParameterValue(beginCharParam).toInt();
		int endLine = params.getParameterValue(endLineParam).toInt();
		int endChar = params.getParameterValue(endCharParam).toInt();
		String commit = leftSide?getOldCommit().name():getNewCommit().name();
		return new CommentPos(commit, change.getPath(), new TextRange(beginLine, beginChar, endLine, endChar));
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
		
		response.render(JavaScriptHeaderItem.forReference(new TextDiffResourceReference()));
		
		String jsonOfOldCommentInfos;
		String jsonOfNewCommentInfos;
		String jsonOfMark;
		String jsonOfCommentInfo;
		String dirtyContainerId;
		if (markSupport != null) {
			String oldCommitHash = getOldCommit().name();
			Map<Integer, List<CommentInfo>> oldCommentInfos = new HashMap<>(); 
			Map<Integer, List<CommentInfo>> newCommentInfos = new HashMap<>(); 
			for (CodeComment comment: markSupport.getComments()) {
				if (comment.getCommentPos().getRange() != null) {
					int line = comment.getCommentPos().getRange().getBeginLine();
					List<CommentInfo> commentInfosAtLine;
					CommentInfo commentInfo = new CommentInfo();
					commentInfo.id = comment.getId();
					commentInfo.mark = new MarkInfo(comment, oldCommitHash);
					if (commentInfo.mark.leftSide) {
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
			
			ObjectMapper mapper = GitPlex.getInstance(ObjectMapper.class);
			try {
				jsonOfOldCommentInfos = mapper.writeValueAsString(oldCommentInfos);
				jsonOfNewCommentInfos = mapper.writeValueAsString(newCommentInfos);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
			
			CommentPos mark = markSupport.getMark();
			if (mark != null) {
				jsonOfMark = getJson(mark);
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
		String script = String.format("gitplex.textdiff.init('%s', '%s', '%s', '%s', %s, %s, %s, %s, %s, %s, %s, %s, %s, '%s');", 
				getMarkupId(), symbolTooltip.getMarkupId(), 
				change.getOldBlobIdent().revision, change.getNewBlobIdent().revision,
				RequestCycle.get().find(AjaxRequestTarget.class) == null, 
				callback, blameMessageBehavior.getCallback(),
				markSupport!=null, jsonOfMark, jsonOfCommentInfo, 
				jsonOfOldCommentInfos, jsonOfNewCommentInfos, dirtyContainerId, 
				GitPlex.getInstance().getDocLink());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	private String renderDiffs() {
		StringBuilder builder = new StringBuilder();
		if (blameInfo != null) {
			blameInfo.lastCommitHash = null;
			blameInfo.lastOldCommitHash = null;
			blameInfo.lastNewCommitHash = null;
		}
		
		if (diffMode == DiffViewMode.UNIFIED) {
			if (blameInfo != null) {
				builder.append(""
						+ "<colgroup>"
						+ "<col width='240'></col>"
						+ "<col width='60'></col>"
						+ "<col width='60'></col>"
						+ "<col width='15'></col>"
						+ "<col></col>"
						+ "</colgroup>");
			} else {
				builder.append(""
						+ "<colgroup>"
						+ "<col width='60'></col>"
						+ "<col width='60'></col>"
						+ "<col width='15'></col>"
						+ "<col></col>"
						+ "</colgroup>");
			}
		} else {
			if (blameInfo != null) {
				builder.append(""
						+ "<colgroup>"
						+ "<col width='70'></col>"
						+ "<col width='60'></col>"
						+ "<col width='15'></col>"
						+ "<col></col>"
						+ "<col width='70'></col>"
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
		}
		for (int i=0; i<getDiffBlocks().size(); i++) {
			MarkAwareDiffBlock block = getDiffBlocks().get(i);
			if (block.getType() == Type.EQUAL) {
				Integer lastContextSize = contextSizes.get(i);
				if (lastContextSize == null)
					lastContextSize = Constants.DIFF_CONTEXT_SIZE;
				appendEquals(builder, i, 0, lastContextSize);
			} else if (block.getType() == Type.MARKED_EQUAL) {
				appendMarkedEquals(builder, i);
			} else if (block.getType () == Type.DELETE) {
				if (i+1<getDiffBlocks().size()) {
					MarkAwareDiffBlock nextBlock = getDiffBlocks().get(i+1);
					if (nextBlock.getType() == Type.INSERT) {
						LinkedHashMap<Integer, LineDiff> lineChanges = 
								DiffUtils.align(block.getLines(), nextBlock.getLines());
						if (blameInfo != null && diffMode == DiffViewMode.UNIFIED) {
							for (int j=0; j<block.getLines().size(); j++) { 
								LineDiff lineDiff = lineChanges.get(j);
								if (lineDiff != null) {
									appendDelete(builder, block, j, lineDiff.getTokenDiffs());
								} else {
									appendDelete(builder, block, j, null);
								}
							}
							Map<Integer, LineDiff> lineChangesByInsert = new HashMap<>();
							for (LineDiff diff: lineChanges.values()) {
								lineChangesByInsert.put(diff.getCompareLine(), diff);
							}
							for (int j=0; j<nextBlock.getLines().size(); j++) {
								LineDiff lineDiff = lineChangesByInsert.get(j);
								if (lineDiff != null) {
									appendInsert(builder, nextBlock, j, lineDiff.getTokenDiffs());
								} else {
									appendInsert(builder, nextBlock, j, null);
								}
							}
						} else {
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
						}
						i++;
					} else {
						for (int j=0; j<block.getLines().size(); j++) 
							appendDelete(builder, block, j, null);
					}
				} else {
					for (int j=0; j<block.getLines().size(); j++) 
						appendDelete(builder, block, j, null);
				}
			} else {
				for (int j=0; j<block.getLines().size(); j++) 
					appendInsert(builder, block, j, null);
			}
		}
		return builder.toString();
	}

	private void appendDeletesAndInserts(StringBuilder builder, MarkAwareDiffBlock deleteBlock, 
			MarkAwareDiffBlock insertBlock, int fromDeleteLineIndex, int toDeleteLineIndex, 
			int fromInsertLineIndex, int toInsertLineIndex) {
		if (diffMode == DiffViewMode.UNIFIED) {
			for (int i=fromDeleteLineIndex; i<toDeleteLineIndex; i++)
				appendDelete(builder, deleteBlock, i, null);
			for (int i=fromInsertLineIndex; i<toInsertLineIndex; i++)
				appendInsert(builder, insertBlock, i, null);
		} else {
			int deleteSize = toDeleteLineIndex - fromDeleteLineIndex;
			int insertSize = toInsertLineIndex - fromInsertLineIndex;
			if (deleteSize < insertSize) {
				for (int i=fromDeleteLineIndex; i<toDeleteLineIndex; i++) 
					appendSideBySide(builder, deleteBlock, insertBlock, i, i-fromDeleteLineIndex+fromInsertLineIndex);
				for (int i=fromInsertLineIndex+deleteSize; i<toInsertLineIndex; i++)
					appendInsert(builder, insertBlock, i, null);
			} else {
				for (int i=fromInsertLineIndex; i<toInsertLineIndex; i++) 
					appendSideBySide(builder, deleteBlock, insertBlock, i-fromInsertLineIndex+fromDeleteLineIndex, i);
				for (int i=fromDeleteLineIndex+insertSize; i<toDeleteLineIndex; i++)
					appendDelete(builder, deleteBlock, i, null);
			}
		}
	}
	
	private void appendBlame(StringBuilder builder, int oldLineNo, int newLineNo) {
		BriefCommit commit;
		if (newLineNo != -1)
			commit = Preconditions.checkNotNull(blameInfo.newBlame.get(newLineNo));
		else
			commit = Preconditions.checkNotNull(blameInfo.oldBlame.get(oldLineNo));
		if (diffMode == DiffViewMode.UNIFIED && !commit.getHash().equals(blameInfo.lastCommitHash)
				|| diffMode == DiffViewMode.SPLIT && newLineNo != -1 && !commit.getHash().equals(blameInfo.lastNewCommitHash)
				|| diffMode == DiffViewMode.SPLIT && oldLineNo != -1 && !commit.getHash().equals(blameInfo.lastOldCommitHash)) {
			CommitDetailPage.State state = new CommitDetailPage.State();
			state.revision = commit.getHash();
			state.whitespaceOption = change.getWhitespaceOption();
			PageParameters params = CommitDetailPage.paramsOf(depotModel.getObject(), state);
			String url = urlFor(CommitDetailPage.class, params).toString();
			if (diffMode == DiffViewMode.UNIFIED) {
				builder.append(String.format("<td class='blame noselect'><a class='hash' href='%s' data-hash='%s'>%s</a><span class='date'>%s</span><span class='author'>%s</span></td>", 
						url, commit.getHash(), GitUtils.abbreviateSHA(commit.getHash()), 
						DateUtils.formatDate(commit.getCommitter().getWhen()),
						HtmlEscape.escapeHtml5(commit.getAuthor().getName())));
			} else {
				builder.append(String.format("<td class='abbr blame noselect'><a class='hash' href='%s' data-hash='%s'>%s</a></td>", 
						url, commit.getHash(), GitUtils.abbreviateSHA(commit.getHash())));
			}
		} else {
			if (diffMode == DiffViewMode.UNIFIED) {
				builder.append("<td class='blame noselect'><div class='same-as-above'>...</div></td>");
			} else {
				builder.append("<td class='abbr blame noselect'><div class='same-as-above'>...</div></td>");
			}
		}
		blameInfo.lastCommitHash = commit.getHash();
		if (newLineNo != -1)
			blameInfo.lastNewCommitHash = commit.getHash();
		if (oldLineNo != -1)
			blameInfo.lastOldCommitHash = commit.getHash();
	}
	
	private void appendEqual(StringBuilder builder, MarkAwareDiffBlock block, int lineIndex, int lastContextSize) {
		if (lastContextSize != 0)
			builder.append("<tr class='code expanded'>");
		else
			builder.append("<tr class='code original'>");

		int oldLineNo = block.getOldStart() + lineIndex;
		int newLineNo = block.getNewStart() + lineIndex;
		
		if (diffMode == DiffViewMode.UNIFIED) {
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, newLineNo);
			}
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
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, -1);
			}
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
			if (blameInfo != null) {
				appendBlame(builder, -1, newLineNo);
			}
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
	
	private void appendInsert(StringBuilder builder, MarkAwareDiffBlock block, int lineIndex, @Nullable List<DiffBlock<CmToken>> tokenDiffs) {
		builder.append("<tr class='code original'>");

		int newLineNo = block.getNewStart() + lineIndex;
		if (diffMode == DiffViewMode.UNIFIED) {
			if (blameInfo != null) {
				appendBlame(builder, -1, newLineNo);
			}
			builder.append("<td class='number noselect new'>&nbsp;</td>");
			builder.append("<td class='number noselect new'>").append(newLineNo+1).append("</td>");
			builder.append("<td class='operation noselect new'>+</td>");
			builder.append("<td class='content new' data-new='").append(newLineNo).append("'>");
			if (tokenDiffs != null) {
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
			} else {
				List<CmToken> tokens = block.getLines().get(lineIndex);
				if (tokens.isEmpty()) {
					builder.append("&nbsp;");
				} else {
					for (int i=0; i<tokens.size(); i++) 
						builder.append(tokens.get(i).toHtml(Operation.EQUAL));
				}
			}
			builder.append("</td>");
		} else {
			if (blameInfo != null) {
				builder.append("<td class='blame noselect'>&nbsp;</td>");
			}
			builder.append("<td class='number noselect'>&nbsp;</td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content left'>&nbsp;</td>");
			if (blameInfo != null) {
				appendBlame(builder, -1, newLineNo);
			}
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
	
	private void appendDelete(StringBuilder builder, MarkAwareDiffBlock block, int lineIndex, @Nullable List<DiffBlock<CmToken>> tokenDiffs) {
		builder.append("<tr class='code original'>");
		
		int oldLineNo = block.getOldStart() + lineIndex;
		if (diffMode == DiffViewMode.UNIFIED ) {
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, -1);
			}
			builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("</td>");
			builder.append("<td class='number noselect old'>&nbsp;</td>");
			builder.append("<td class='operation noselect old'>-</td>");
			builder.append("<td class='content old' data-old='").append(oldLineNo).append("'>");
			if (tokenDiffs != null) {
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
			} else {
				List<CmToken> tokens = block.getLines().get(lineIndex);
				if (tokens.isEmpty()) {
					builder.append("&nbsp;");
				} else {
					for (int i=0; i<tokens.size(); i++) 
						builder.append(tokens.get(i).toHtml(Operation.EQUAL));
				}
			}
			builder.append("</td>");
		} else {
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, -1);
			}
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
			if (blameInfo != null) {
				builder.append("<td class='blame noselect'>&nbsp;</td>");
			}
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
		if (blameInfo != null) {
			appendBlame(builder, oldLineNo, -1);
		}
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
		if (blameInfo != null) {
			appendBlame(builder, -1, newLineNo);
		}
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
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, newLineNo);
			}
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
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, -1);
			}
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
			
			if (blameInfo != null) {
				appendBlame(builder, -1, newLineNo);
			}
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
		
		String script = String.format("javascript:$('#%s').data('callback')('expand', %d);", getMarkupId(), blockIndex);
		if (diffMode == DiffViewMode.UNIFIED) {
			if (blameInfo != null) {
				builder.append("<td colspan='3' class='expander noselect'><a title='Show more lines' href=\"")
						.append(script).append("\"><i class='fa fa-sort'></i></a></td>");
				blameInfo.lastCommitHash = null;
				blameInfo.lastOldCommitHash = null;
				blameInfo.lastNewCommitHash = null;
			} else {
				builder.append("<td colspan='2' class='expander noselect'><a title='Show more lines' href=\"")
						.append(script).append("\"><i class='fa fa-sort'></i></a></td>");
			}
			builder.append("<td colspan='2' class='skipped noselect'><i class='fa fa-ellipsis-h'></i> skipped ")
					.append(skippedLines).append(" lines <i class='fa fa-ellipsis-h'></i></td>");
		} else {
			if (blameInfo != null) {
				builder.append("<td colspan='2' class='expander noselect'><a title='Show more lines' href=\"").append(script)
						.append("\"><i class='fa fa-sort'></i></a></td>");
				builder.append("<td class='skipped noselect' colspan='6'><i class='fa fa-ellipsis-h'></i> skipped ")
						.append(skippedLines).append(" lines <i class='fa fa-ellipsis-h'></i></td>");
				blameInfo.lastCommitHash = null;
				blameInfo.lastOldCommitHash = null;
				blameInfo.lastNewCommitHash = null;
			} else {
				builder.append("<td class='expander noselect'><a title='Show more lines' href=\"").append(script)
						.append("\"><i class='fa fa-sort'></i></a></td>");
				builder.append("<td class='skipped noselect' colspan='5'><i class='fa fa-ellipsis-h'></i> skipped ")
						.append(skippedLines).append(" lines <i class='fa fa-ellipsis-h'></i></td>");
			}
		}
		builder.append("</tr>");
	}
	
	private List<MarkAwareDiffBlock> getDiffBlocks() {
		if (diffBlocks == null) {
			diffBlocks = new ArrayList<>();
			List<Range> oldRanges = new ArrayList<>();
			List<Range> newRanges = new ArrayList<>();
			if (markSupport != null) {
				List<CommentPos> marks = new ArrayList<>();
				CommentPos mark = markSupport.getMark();
				if (mark != null) {
					marks.add(mark);
				}
				String oldCommitHash = getOldCommit().name();
				for (CodeComment comment: markSupport.getComments()) {
					mark = comment.getCommentPos();
					marks.add(mark);
				}
				for (CommentPos each: marks) {
					Range range = new Range(each.getRange().beginLine, each.getRange().endLine+1);
					if (each.getCommit().equals(oldCommitHash)) {
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
		
		if (blameModel != null)
			blameModel.detach();
		
		super.onDetach();
	}

	private String getJsonOfComment(CodeComment comment) {
		CommentInfo commentInfo = new CommentInfo();
		commentInfo.id = comment.getId();
		commentInfo.mark = new MarkInfo(comment, getOldCommit().name());

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
		mark(target, null);
	}

	@Override
	public void onCommentClosed(AjaxRequestTarget target, CodeComment comment) {
		String script = String.format("gitplex.textdiff.onCloseComment($('#%s'));", getMarkupId());
		target.appendJavaScript(script);
		mark(target, null);
	}

	@Override
	public void onCommentAdded(AjaxRequestTarget target, CodeComment comment) {
		String script = String.format("gitplex.textdiff.onCommentAdded($('#%s'), %s);", 
				getMarkupId(), getJsonOfComment(comment));
		target.appendJavaScript(script);
	}

	@Override
	public void mark(AjaxRequestTarget target, CommentPos mark) {
		String script;
		if (mark != null) {
			script = String.format(""
				+ "var $container = $('#%s');"
				+ "var mark = %s;"
				+ "gitplex.textdiff.scroll($container, mark);"
				+ "gitplex.textdiff.mark($container, mark);", 
				getMarkupId(), getJson(mark));
		} else {
			script = String.format(""
				+ "var $container = $('#%s');"
				+ "gitplex.textdiff.clearMark($container);"
				+ "$container.removeData('mark');", 
				getMarkupId());
		}
		target.appendJavaScript(script);
	}
	
	@Override
	public void onUnblame(AjaxRequestTarget target) {
		blameInfo = null;
		target.add(this);
	}
	
	@SuppressWarnings("unused")
	private static class CommentInfo {
		long id;
		
		String title;
		
		MarkInfo mark;
	}

	@SuppressWarnings("unused")
	private static class MarkInfo extends TextRange {
		
		String path;
		
		boolean leftSide;

		public MarkInfo() {
			
		}
		
		public MarkInfo(CodeComment comment, String oldCommitHash) {
			super(comment.getCommentPos().getRange());
			path = comment.getCommentPos().getPath();
			leftSide = comment.getCommentPos().getCommit().equals(oldCommitHash);
		}
		
		public MarkInfo(CommentPos mark, String oldCommitHash) {
			super(mark.getRange());
			path = mark.getPath();
			leftSide = mark.getCommit().equals(oldCommitHash);
		}
		
	}
	
	private static class BlameInfo implements Serializable {
		
		Map<Integer, BriefCommit> oldBlame = new HashMap<>();
		
		Map<Integer, BriefCommit> newBlame = new HashMap<>();
		
		String lastCommitHash;
		
		String lastOldCommitHash;
		
		String lastNewCommitHash;
		
	}

}
