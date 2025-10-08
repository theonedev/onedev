package io.onedev.server.web.component.diff.blob.text;

import static io.onedev.server.codequality.BlobTarget.groupByLine;
import static io.onedev.server.util.diff.DiffRenderer.toHtml;
import static io.onedev.server.web.translation.Translation._T;
import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;
import static org.unbescape.javascript.JavaScriptEscape.escapeJavaScript;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.ibm.icu.text.SpoofChecker;

import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.git.BlameBlock;
import io.onedev.server.git.BlameCommit;
import io.onedev.server.git.BlobChange;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Pair;
import io.onedev.server.util.diff.DiffBlock;
import io.onedev.server.util.diff.DiffMatchPatch.Operation;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.blamemessage.BlameMessageBehavior;
import io.onedev.server.web.component.diff.blob.BlobAnnotationSupport;
import io.onedev.server.web.component.diff.revision.DiffViewMode;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.symboltooltip.SymbolTooltipPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.util.AnnotationInfo;
import io.onedev.server.web.util.CodeCommentInfo;
import io.onedev.server.web.util.DiffPlanarRange;

public class BlobTextDiffPanel extends Panel {

	private final BlobChange change;
	
	private final Map<Integer, Integer> contextSizes = new HashMap<>();
	
	private final DiffViewMode diffMode;

	private final IModel<Boolean> blameModel;
	
	private final IModel<DiffAnnotationInfo> annotationInfoModel = new LoadableDetachableModel<DiffAnnotationInfo>() {

		@Override
		protected DiffAnnotationInfo load() {
			if (getAnnotationSupport() != null) {
				Map<Integer, List<CodeCommentInfo>> oldCommentsByLine = 
						CodeCommentInfo.groupByLine(getAnnotationSupport().getOldComments());
				Map<Integer, List<CodeCommentInfo>> newCommentsByLine = 
						CodeCommentInfo.groupByLine(getAnnotationSupport().getNewComments());

				var oldLines = change.getType()!=ChangeType.ADD? change.getOldText().getLines(): new ArrayList<String>();
				Map<Integer, List<CodeProblem>> oldProblems = groupByLine(
						getAnnotationSupport().getOldProblems(), oldLines);

				var newLines = change.getType()!=ChangeType.DELETE? change.getNewText().getLines(): new ArrayList<String>();
				Map<Integer, List<CodeProblem>> newProblems = groupByLine(
						getAnnotationSupport().getNewProblems(), newLines);
				
				return new DiffAnnotationInfo(
						new AnnotationInfo(oldCommentsByLine, oldProblems, 
								getAnnotationSupport().getOldCoverages()), 
						new AnnotationInfo(newCommentsByLine, newProblems, 
								getAnnotationSupport().getNewCoverages()));
			} else {
				return new DiffAnnotationInfo(
						new AnnotationInfo(new HashMap<>(), new HashMap<>(), new HashMap<>()), 
						new AnnotationInfo(new HashMap<>(), new HashMap<>(), new HashMap<>()));
			}
		}
		
	};

	private static final SpoofChecker confusableChecker = new SpoofChecker.Builder().setChecks(SpoofChecker.CONFUSABLE).build();
	
	private Component symbolTooltip;
	
	private AbstractPostAjaxBehavior callbackBehavior;
	
	private BlameMessageBehavior blameMessageBehavior;
	
	private BlameInfo blameInfo;
	
	public BlobTextDiffPanel(String id, BlobChange change, DiffViewMode diffMode, 
			@Nullable IModel<Boolean> blameModel) {
		super(id);
		
		this.change = change;
		this.diffMode = diffMode;
		this.blameModel = blameModel;
		
		if (blameModel != null && blameModel.getObject()) 
			blameInfo = getBlameInfo();
	}

	private String convertToJson(Object obj) {
		try {
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void toggleBlame(AjaxRequestTarget target) {
		if (blameInfo != null) 
			blameInfo = null;
		else 
			blameInfo = getBlameInfo();
		target.add(this);
		blameModel.setObject(blameInfo != null);
		((BasePage)getPage()).resizeWindow(target);
	}
	
 	public boolean isBlame() {
		return blameInfo != null;
	}
	
	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}
	
	private BlameInfo getBlameInfo() {
		blameInfo = new BlameInfo();
		String oldPath = change.getOldBlobIdent().path;
		if (oldPath != null) {
			for (BlameBlock blame: getGitService().blame(change.getProject(), change.getOldCommitId(), oldPath, null)) {
				for (LinearRange range: blame.getRanges()) {
					for (int i=range.getFrom(); i<=range.getTo(); i++) 
						blameInfo.oldBlame.put(i, blame.getCommit());
				}
			}
		}
		String newPath = change.getNewBlobIdent().path;
		if (newPath != null) {
			for (BlameBlock blame: getGitService().blame(change.getProject(), change.getNewCommitId(), newPath, null)) {
				for (LinearRange range: blame.getRanges()) {
					for (int i=range.getFrom(); i<=range.getTo(); i++) 
						blameInfo.newBlame.put(i, blame.getCommit());
				}
			}
		}
		return blameInfo;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("diffLines", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return renderDiffs();
			}
			
		}).setEscapeModelStrings(false));
		
		add(callbackBehavior = new AbstractPostAjaxBehavior() {
			
			private String getMarkedText(DiffPlanarRange markRange) {
				List<String> lines;
				if (markRange.leftSide)
					lines = change.getOldText().getLines();
				else 
					lines = change.getNewText().getLines();
				StringBuilder builder = new StringBuilder();
				if (markRange.getFromRow() != markRange.getToRow()) {
					String line = lines.get(markRange.getFromRow());
					int beginChar = markRange.getFromColumn();
					if (beginChar < line.length())
						builder.append(line.substring(beginChar));
					for (int i=markRange.getFromRow()+1; i<markRange.getToRow(); i++) {
						if (builder.length() != 0)
							builder.append("\n");
						builder.append(lines.get(i));
					}
					line = lines.get(markRange.getToRow());
					int endChar = markRange.getToColumn();
					if (endChar > 0) {
						if (endChar > line.length())
							endChar = line.length();
						if (builder.length() != 0)
							builder.append("\n");
						builder.append(line.substring(0, endChar));
					}
				} else {
					String line = lines.get(markRange.getFromRow());
					int beginChar = markRange.getFromColumn();
					int endChar = markRange.getToColumn();
					if (beginChar < line.length() && endChar > 0) {
						if (endChar > line.length())
							endChar = line.length();
						builder.append(line.substring(beginChar, endChar));
					}
				}
				
				return builder.toString();
			}
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				switch (params.getParameterValue("action").toString("")) {
					case "expand":
						if (blameInfo != null) {
							blameInfo.lastCommitHash = null;
							blameInfo.lastOldCommitHash = null;
							blameInfo.lastNewCommitHash = null;
						}
						int index = params.getParameterValue("param1").toInt();
						Integer lastContextSize = contextSizes.get(index);
						if (lastContextSize == null)
							lastContextSize = WebConstants.DIFF_CONTEXT_SIZE;
						int contextSize = lastContextSize + WebConstants.DIFF_EXPAND_SIZE;
						contextSizes.put(index, contextSize);
						
						StringBuilder builder = new StringBuilder();
						appendEquals(builder, index, lastContextSize, contextSize);
						
						String expanded = StringUtils.replace(builder.toString(), "\n", "");
						String script = String.format("onedev.server.blobTextDiff.expand('%s', %d, \"%s\");",
								getMarkupId(), index, escapeJavaScript(expanded));
						target.appendJavaScript(script);
						break;
					case "openSelectionPopover":
						String jsonOfPosition = String.format("{left: %d, top: %d}", 
								params.getParameterValue("param1").toInt(), 
								params.getParameterValue("param2").toInt());
						DiffPlanarRange commentRange = getRange(params, "param3", "param4", "param5", "param6", "param7");
						
						String markUrl;
						if (getAnnotationSupport() != null) {
							markUrl = getAnnotationSupport().getMarkUrl(commentRange);
							if (markUrl != null) 
								markUrl = "'" + escapeJavaScript(markUrl) + "'";
							else 
								markUrl = "undefined";
						} else {
							markUrl = "undefined";
						}
						script = String.format("onedev.server.blobTextDiff.openSelectionPopover('%s', %s, %s, %s, '%s', %s);", 
								getMarkupId(), jsonOfPosition, convertToJson(commentRange), markUrl, 
								escapeJavaScript(getMarkedText(commentRange)),
								SecurityUtils.getAuthUser()!=null);
						target.appendJavaScript(script);
						break;
					case "addComment":
						Preconditions.checkNotNull(SecurityUtils.getAuthUser());
						
						commentRange = getRange(params, "param1", "param2", "param3", "param4", "param5");
						getAnnotationSupport().onAddComment(target, commentRange);
						script = String.format("onedev.server.blobTextDiff.onAddComment($('#%s'), %s);", 
								getMarkupId(), convertToJson(commentRange));
						target.appendJavaScript(script);
						break;
					case "openComment": 
						Long commentId = params.getParameterValue("param1").toLong();
						commentRange = getRange(params, "param2", "param3", "param4", "param5", "param6");
						CodeComment comment = OneDev.getInstance(CodeCommentService.class).load(commentId);
						getAnnotationSupport().onOpenComment(target, comment, commentRange);
						script = String.format("onedev.server.blobTextDiff.onCommentOpened($('#%s'), %s);", 
								getMarkupId(), convertToJson(new DiffCodeCommentInfo(comment, commentRange)));
						target.appendJavaScript(script);
						break;
					case "setActive": 
						onActive(target);
						break;
				}
			}

		});
		
		add(blameMessageBehavior = new BlameMessageBehavior() {
			
			@Override
			protected Project getProject() {
				return change.getProject();
			}
		});
		
		symbolTooltip = new SymbolTooltipPanel("symbolTooltip") {

			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				setResponsePage(ProjectBlobPage.class, getQueryHitParams(hit));
			}

			@Override
			protected void onOccurrencesQueried(AjaxRequestTarget target, List<QueryHit> hits) {
				setResponsePage(ProjectBlobPage.class, getFindOccurrencesParams());
			}

			@Override
			protected String getBlobPath() {
				if (getRevision().equals(change.getNewBlobIdent().revision))
					return change.getNewBlobIdent().path;
				else
					return change.getOldBlobIdent().path;
			}

			@Override
			protected Project getProject() {
				return change.getProject();
			}
			
		};
		add(symbolTooltip);
		
		add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {
			
			@Override
			public String getObject() {
				return blameInfo!=null? "blob-text-diff need-width": "blob-text-diff";
			}
			
		}));
		setOutputMarkupId(true);
	}
	
	private DiffPlanarRange getRange(IRequestParameters params,
			String leftSideParam, String beginLineParam, String beginCharParam, 
			String endLineParam, String endCharParam) {
		boolean leftSide = params.getParameterValue(leftSideParam).toBoolean();
		int beginLine = params.getParameterValue(beginLineParam).toInt();
		int beginChar = params.getParameterValue(beginCharParam).toInt();
		int endLine = params.getParameterValue(endLineParam).toInt();
		int endChar = params.getParameterValue(endCharParam).toInt();
		return new DiffPlanarRange(leftSide, beginLine, beginChar, endLine, endChar);
	}
	
	protected void onActive(AjaxRequestTarget target) {
		
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new BlobTextDiffResourceReference()));
		
		DiffCodeCommentInfo openCommentInfo;
		DiffPlanarRange markRange;
		String commentContainerId;
		if (getAnnotationSupport() != null) {
			markRange = getAnnotationSupport().getMarkRange();
			Pair<CodeComment, DiffPlanarRange> openCommentPair = getAnnotationSupport().getOpenComment();
			if (openCommentPair != null) 
				openCommentInfo = new DiffCodeCommentInfo(openCommentPair.getLeft(), openCommentPair.getRight());
			else 
				openCommentInfo = null;
			commentContainerId = "'" + getAnnotationSupport().getCommentContainer().getMarkupId() + "'";
		} else {
			markRange = null;
			openCommentInfo = null;
			commentContainerId = null;
		}
		
		CharSequence callback = callbackBehavior.getCallbackFunction(
				explicit("action"), explicit("param1"), explicit("param2"), 
				explicit("param3"), explicit("param4"), explicit("param5"),
				explicit("param6"), explicit("param7"), explicit("param8")); 

		var translations = new HashMap<String, String>();
		translations.put("unable-to-comment", _T("Unable to comment"));
		translations.put("perma-link", _T("Permanent link of this selection")); 
		translations.put("copy-to-clipboard", _T("Copy selected text to clipboard")); 
		translations.put("add-selection-comment", _T("Add comment on this selection")); 
		translations.put("login-to-comment", _T("Login to comment on selection"));
		translations.put("covered-by-tests", _T("Covered by tests"));
		translations.put("not-covered-by-any-test", _T("Not covered by any test"));
		translations.put("partially-covered-by-some-tests", _T("Partially covered by some tests"));
		translations.put("unsaved-changes-prompt", _T("There are unsaved changes, discard and continue?"));
		translations.put("show-comment", _T("Click to show comment of marked text"));
		translations.put("loading", _T("Loading..."));
		translations.put("invalid-selection", _T("Invalid selection, click for details"));
		for (var severity: CodeProblem.Severity.values())
			translations.put(severity.name(), _T("severity:" + severity.name()));
		translations.put("add-problem-comment", _T("Add comment"));

		String script = String.format("onedev.server.blobTextDiff.onDomReady('%s', '%s', '%s', '%s', '%s', '%s', %s, %s, %s, %s, %s, %s, %s);", 
				getMarkupId(), symbolTooltip.getMarkupId(), 
				change.getOldBlobIdent().revision, 
				change.getNewBlobIdent().revision,
				change.getOldBlobIdent().path!=null? escapeJavaScript(change.getOldBlobIdent().path):"",
				change.getNewBlobIdent().path!=null? escapeJavaScript(change.getNewBlobIdent().path):"",
				callback, blameMessageBehavior.getCallback(),
				convertToJson(markRange), convertToJson(openCommentInfo), 
				convertToJson(annotationInfoModel.getObject()), 
				commentContainerId, convertToJson(translations));
		
		response.render(OnDomReadyHeaderItem.forScript(script));

		String jsonOfMarkRange;
		if (RequestCycle.get().find(AjaxRequestTarget.class) == null && markRange != null)
			jsonOfMarkRange = convertToJson(markRange);
		else
			jsonOfMarkRange = "undefined";
		script = String.format("onedev.server.blobTextDiff.onLoad('%s', %s);", getMarkupId(), jsonOfMarkRange);
		response.render(OnLoadHeaderItem.forScript(script));
	}
	
	private String renderDiffs() {
		StringBuilder builder = new StringBuilder();
		if (blameInfo != null) {
			blameInfo.lastCommitHash = null;
			blameInfo.lastOldCommitHash = null;
			blameInfo.lastNewCommitHash = null;
		}
		
		int oldProblemsWidth;
		if (!annotationInfoModel.getObject().getOldAnnotations().getProblems().isEmpty())
			oldProblemsWidth = 24;
		else
			oldProblemsWidth = 0;
		
		int newProblemsWidth;
		if (!annotationInfoModel.getObject().getNewAnnotations().getProblems().isEmpty())
			newProblemsWidth = 24;
		else
			newProblemsWidth = 0;
		
		int shortBlameColumnWidth = 70;
		int longBlameColumnWidth = 240;
		int baseLineNumColumnWidth = 66;
		int operationColumnWidth = 15;
		
		if (diffMode == DiffViewMode.UNIFIED) {
			builder.append("<colgroup>");
			if (blameInfo != null)
				builder.append(String.format("<col width='%d'></col>", longBlameColumnWidth));
			builder.append(String.format(""
					+ "<col width='%d'></col>"
					+ "<col width='%d'></col>"
					+ "<col width='%d'></col>"
					+ "<col></col>"
					+ "</colgroup>", 
					baseLineNumColumnWidth+oldProblemsWidth, baseLineNumColumnWidth+newProblemsWidth, 
					operationColumnWidth));
		} else {
			builder.append("<colgroup>");
			if (blameInfo != null)
				builder.append(String.format("<col width='%d'></col>", shortBlameColumnWidth));
			builder.append(String.format(""
					+ "<col width='%d'></col>"
					+ "<col width='%d'></col>"
					+ "<col></col>", 
					baseLineNumColumnWidth+oldProblemsWidth, operationColumnWidth));
			if (blameInfo != null)
				builder.append(String.format("<col width='%d'></col>", shortBlameColumnWidth));
			builder.append(String.format(""
					+ "<col width='%d'></col>"
					+ "<col width='%d'></col>"
					+ "<col></col>"
					+ "</colgroup>", 
					baseLineNumColumnWidth+newProblemsWidth, operationColumnWidth));
		}
		for (int i=0; i<change.getDiffBlocks().size(); i++) {
			DiffBlock<String> block = change.getDiffBlocks().get(i);
			if (block.getOperation() == Operation.EQUAL) {
				Integer lastContextSize = contextSizes.get(i);
				if (lastContextSize == null)
					lastContextSize = WebConstants.DIFF_CONTEXT_SIZE;
				appendEquals(builder, i, 0, lastContextSize);
			} else if (block.getOperation() == Operation.DELETE) {
				if (i+1<change.getDiffBlocks().size()) {
					DiffBlock<String> nextBlock = change.getDiffBlocks().get(i+1);
					if (nextBlock.getOperation() == Operation.INSERT) {
						LinkedHashMap<Integer, List<DiffBlock<String>>> lineDiffs = 
								DiffUtils.diffLines(block.getElements(), nextBlock.getElements());
						if (diffMode == DiffViewMode.UNIFIED) {
							for (int j=0; j<block.getElements().size(); j++) { 
								List<DiffBlock<String>> lineDiff = lineDiffs.get(j);
								if (lineDiff != null) 
									appendUnifiedDelete(builder, block, j, lineDiff);
								else 
									appendUnifiedDelete(builder, block, j, null);
							}
							for (int j=0; j<nextBlock.getElements().size(); j++) {
								List<DiffBlock<String>> lineDiff = lineDiffs.get(j);
								if (lineDiff != null) {
									var oldLine = block.getElements().get(j);
									var newLine = nextBlock.getElements().get(j);
									var confusable = isConfusable(oldLine, newLine);
									appendUnifiedInsert(builder, nextBlock, j, new Pair<>(lineDiff, confusable));
								} else {
									appendUnifiedInsert(builder, nextBlock, j, null);
								}
							}
						} else {
							int prevLineIndex = 0;
							for (Map.Entry<Integer, List<DiffBlock<String>>> entry: lineDiffs.entrySet()) {
								int lineIndex = entry.getKey();
								List<DiffBlock<String>> lineDiff = entry.getValue();
								
								appendDeletesAndInserts(builder, block, nextBlock, prevLineIndex, lineIndex, 
										prevLineIndex, lineIndex);
								
								appendModification(builder, block, nextBlock, lineIndex, lineIndex, lineDiff); 
								
								prevLineIndex = lineIndex+1;
							}
							appendDeletesAndInserts(builder, block, nextBlock, 
									prevLineIndex, block.getElements().size(), 
									prevLineIndex, nextBlock.getElements().size());
						}
						i++;
					} else {
						for (int j=0; j<block.getElements().size(); j++) {
							if (diffMode == DiffViewMode.UNIFIED)
								appendUnifiedDelete(builder, block, j, null);
							else
								appendSplitDelete(builder, block, j);
						}
					}
				} else {
					for (int j=0; j<block.getElements().size(); j++) {
						if (diffMode == DiffViewMode.UNIFIED)
							appendUnifiedDelete(builder, block, j, null);
						else
							appendSplitDelete(builder, block, j);
					}
				}
			} else {
				for (int j=0; j<block.getElements().size(); j++) {
					if (diffMode == DiffViewMode.UNIFIED)
						appendUnifiedInsert(builder, block, j, null);
					else
						appendSplitInsert(builder, block, j);
				}
			}
		}
		return builder.toString();
	}

	private void appendDeletesAndInserts(StringBuilder builder, DiffBlock<String> deleteBlock, 
			DiffBlock<String> insertBlock, int fromDeleteLineIndex, int toDeleteLineIndex, 
			int fromInsertLineIndex, int toInsertLineIndex) {
		int deleteSize = toDeleteLineIndex - fromDeleteLineIndex;
		int insertSize = toInsertLineIndex - fromInsertLineIndex;
		if (deleteSize < insertSize) {
			for (int i=fromDeleteLineIndex; i<toDeleteLineIndex; i++) 
				appendDeleteAndInsert(builder, deleteBlock, insertBlock, i, i-fromDeleteLineIndex+fromInsertLineIndex);
			for (int i=fromInsertLineIndex+deleteSize; i<toInsertLineIndex; i++)
				appendSplitInsert(builder, insertBlock, i);
		} else {
			for (int i=fromInsertLineIndex; i<toInsertLineIndex; i++) 
				appendDeleteAndInsert(builder, deleteBlock, insertBlock, i-fromInsertLineIndex+fromDeleteLineIndex, i);
			for (int i=fromDeleteLineIndex+insertSize; i<toDeleteLineIndex; i++)
				appendSplitDelete(builder, deleteBlock, i);
		}
	}
	
	private void appendBlame(StringBuilder builder, int oldLineNo, int newLineNo) {
		BlameCommit commit;
		if (newLineNo != -1)
			commit = blameInfo.newBlame.get(newLineNo);
		else
			commit = blameInfo.oldBlame.get(oldLineNo);
		if (commit != null) {
			if (diffMode == DiffViewMode.UNIFIED && !commit.getHash().equals(blameInfo.lastCommitHash)
					|| diffMode == DiffViewMode.SPLIT && newLineNo != -1 && !commit.getHash().equals(blameInfo.lastNewCommitHash)
					|| diffMode == DiffViewMode.SPLIT && oldLineNo != -1 && !commit.getHash().equals(blameInfo.lastOldCommitHash)) {
				CommitDetailPage.State state = new CommitDetailPage.State();
				state.revision = commit.getHash();
				state.whitespaceOption = change.getWhitespaceOption();
				PageParameters params = CommitDetailPage.paramsOf(change.getProject(), state);
				String url = urlFor(CommitDetailPage.class, params).toString();
				if (diffMode == DiffViewMode.UNIFIED) {
					builder.append(String.format("<td class='blame noselect'><a class='hash' href='%s' onclick='onedev.server.viewState.getFromViewAndSetToHistory();' data-hash='%s'>%s</a><span class='date'>%s</span><span class='author'>%s</span></td>",
							url, commit.getHash(), GitUtils.abbreviateSHA(commit.getHash()),
							DateUtils.formatDate(commit.getCommitter().getWhen()),
							HtmlEscape.escapeHtml5(commit.getAuthor().getName())));
				} else {
					builder.append(String.format("<td class='abbr blame noselect'><a class='hash' href='%s' onclick='onedev.server.viewState.getFromViewAndSetToHistory();' data-hash='%s'>%s</a></td>",
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
		} else { // commit will be null if we add a new blank file
			if (diffMode == DiffViewMode.UNIFIED) {
				builder.append("<td class='blame noselect'>&nbsp;</td>");
			} else {
				builder.append("<td class='abbr blame noselect'>&nbsp;</td>");
			}
		}
	}
	
	private String getOperationClass(Operation operation) {
		if (operation == Operation.INSERT)
			return "insert";
		else if (operation == Operation.DELETE)
			return "delete";
		else
			return null;
	}
	
	private void appendLine(StringBuilder builder, String line) {
		if (line.length() == 0) 
			builder.append("&nbsp;");
		else 
			builder.append(toHtml(line, null));
	}
	
	private void appendEquals(StringBuilder builder, int index, int lastContextSize, int contextSize) {
		DiffBlock<String> block = change.getDiffBlocks().get(index);
		if (index == 0) {
			int start = block.getElements().size()-contextSize;
			if (start < 0)
				start=0;
			else if (start > 0)
				appendExpander(builder, index, start);
			for (int j=start; j<block.getElements().size()-lastContextSize; j++) 
				appendEqual(builder, block, j, lastContextSize);
		} else if (index == change.getDiffBlocks().size()-1) {
			int end = block.getElements().size();
			int skipped = 0;
			if (end > contextSize) {
				skipped = end-contextSize;
				end = contextSize;
			}
			for (int j=lastContextSize; j<end; j++)
				appendEqual(builder, block, j, lastContextSize);
			if (skipped != 0)
				appendExpander(builder, index, skipped);
		} else if (2*contextSize < block.getElements().size()) {
			for (int j=lastContextSize; j<contextSize; j++)
				appendEqual(builder, block, j, lastContextSize);
			appendExpander(builder, index, block.getElements().size() - 2*contextSize);
			for (int j=block.getElements().size()-contextSize; j<block.getElements().size()-lastContextSize; j++)
				appendEqual(builder, block, j, lastContextSize);
		} else {
			for (int j=lastContextSize; j<block.getElements().size()-lastContextSize; j++)
				appendEqual(builder, block, j, lastContextSize);
		}
	}
	
	private void appendEqual(StringBuilder builder, DiffBlock<String> block, int lineIndex, int lastContextSize) {
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
			builder.append("<td class='number noselect'>").append(oldLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='number noselect'>").append(newLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content equal' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			appendLine(builder, block.getElements().get(lineIndex));
			builder.append("</td>");
		} else {
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, -1);
			}
			builder.append("<td class='number noselect'>").append(oldLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content equal left' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			appendLine(builder, block.getElements().get(lineIndex));
			builder.append("</td>");
			if (blameInfo != null) {
				appendBlame(builder, -1, newLineNo);
			}
			builder.append("<td class='number noselect'>").append(newLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content equal right' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			appendLine(builder, block.getElements().get(lineIndex));
			builder.append("</td>");
		}
		builder.append("</tr>");
	}
	
	private void appendSplitInsert(StringBuilder builder, DiffBlock<String> block, int lineIndex) {
		builder.append("<tr class='code original'>");

		int newLineNo = block.getNewStart() + lineIndex;
		if (blameInfo != null) {
			builder.append("<td class='blame noselect'>&nbsp;</td>");
		}
		builder.append("<td class='number noselect'><a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='operation noselect'></td>");
		builder.append("<td class='content left none'>&nbsp;</td>");
		if (blameInfo != null) {
			appendBlame(builder, -1, newLineNo);
		}
		builder.append("<td class='number noselect new'>").append(newLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='operation noselect new'>+</td>");
		builder.append("<td class='content right new' data-new='").append(newLineNo).append("'>");
		appendLine(builder, block.getElements().get(lineIndex));
		builder.append("</td></tr>");
	}
	
	private void appendUnifiedInsert(StringBuilder builder, DiffBlock<String> block, int lineIndex, 
			@Nullable Pair<List<DiffBlock<String>>, Boolean> lineCompareInfo) {
		builder.append("<tr class='code original'>");

		int newLineNo = block.getNewStart() + lineIndex;
		if (blameInfo != null) {
			appendBlame(builder, -1, newLineNo);
		}
		builder.append("<td class='number noselect new'><a class='coverage'>&nbsp;</a></td>");	
		builder.append("<td class='number noselect new'>");
		if (lineCompareInfo != null && lineCompareInfo.getRight())
			builder.append(getConfusableMark());
		builder.append(newLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='operation noselect new'>+</td>");
		builder.append("<td class='content new' data-new='").append(newLineNo).append("'>");
		if (lineCompareInfo != null) {
			if (lineCompareInfo.getLeft().isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (DiffBlock<String> tokenBlock: lineCompareInfo.getLeft()) { 
					for (String token: tokenBlock.getElements()) {
						if (tokenBlock.getOperation() != Operation.DELETE) 
							builder.append(toHtml(token, getOperationClass(tokenBlock.getOperation())));
					}
				}
			}			
		} else {
			appendLine(builder, block.getElements().get(lineIndex));
		}
		builder.append("</td></tr>");
	}

	private void appendSplitDelete(StringBuilder builder, DiffBlock<String> block, int lineIndex) {
		builder.append("<tr class='code original'>");
		
		int oldLineNo = block.getOldStart() + lineIndex;
		if (blameInfo != null) {
			appendBlame(builder, oldLineNo, -1);
		}
		builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='operation noselect old'>-</td>");
		builder.append("<td class='content left old' data-old='").append(oldLineNo).append("'>");
		appendLine(builder, block.getElements().get(lineIndex));
		builder.append("</td>");
		if (blameInfo != null) {
			builder.append("<td class='blame noselect'>&nbsp;</td>");
		}
		builder.append("<td class='number noselect'><a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='operation noselect'>&nbsp;</td>");
		builder.append("<td class='content right none'>&nbsp;</td></tr>");
	}
	
	private void appendUnifiedDelete(StringBuilder builder, DiffBlock<String> block, int lineIndex, 
			@Nullable List<DiffBlock<String>> tokenDiffs) {
		builder.append("<tr class='code original'>");
		
		int oldLineNo = block.getOldStart() + lineIndex;
		if (blameInfo != null) {
			appendBlame(builder, oldLineNo, -1);
		}
		builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='number noselect old'><a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='operation noselect old'>-</td>");
		builder.append("<td class='content old' data-old='").append(oldLineNo).append("'>");
		if (tokenDiffs != null) {
			if (tokenDiffs.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (DiffBlock<String> tokenBlock: tokenDiffs) { 
					for (String token: tokenBlock.getElements()) {
						if (tokenBlock.getOperation() != Operation.INSERT) 
							builder.append(toHtml(token, getOperationClass(tokenBlock.getOperation())));
					}
				}
			}
		} else {
			appendLine(builder, block.getElements().get(lineIndex));
		}
		builder.append("</td></tr>");
	}

	private void appendDeleteAndInsert(StringBuilder builder, DiffBlock<String> deleteBlock, 
			DiffBlock<String> insertBlock, int deleteLineIndex, int insertLineIndex) {
		builder.append("<tr class='code original'>");

		int oldLineNo = deleteBlock.getOldStart()+deleteLineIndex;
		if (blameInfo != null) {
			appendBlame(builder, oldLineNo, -1);
		}
		builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='operation noselect old'>-</td>");
		builder.append("<td class='content left old' data-old='").append(oldLineNo).append("'>");
		appendLine(builder, deleteBlock.getElements().get(deleteLineIndex));
		builder.append("</td>");
		
		int newLineNo = insertBlock.getNewStart()+insertLineIndex;
		if (blameInfo != null) {
			appendBlame(builder, -1, newLineNo);
		}
		builder.append("<td class='number noselect new'>").append(newLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='operation noselect new'>+</td>");
		builder.append("<td class='content right new' data-new='").append(newLineNo).append("'>");
		appendLine(builder, insertBlock.getElements().get(insertLineIndex));
		builder.append("</td>");
		
		builder.append("</tr>");
	}

	private void appendModification(StringBuilder builder, DiffBlock<String> deleteBlock, 
			DiffBlock<String> insertBlock, int deleteLineIndex, int insertLineIndex, 
			List<DiffBlock<String>> tokenDiffs) {
		builder.append("<tr class='code original'>");

		int oldLineNo = deleteBlock.getOldStart() + deleteLineIndex;
		int newLineNo = insertBlock.getNewStart() + insertLineIndex;
		if (blameInfo != null) {
			appendBlame(builder, oldLineNo, -1);
		}

		builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='operation noselect old'>-</td>");
		builder.append("<td class='content left old' data-old='").append(oldLineNo).append("'>");

		if (tokenDiffs.isEmpty()) {
			builder.append("&nbsp;");
		} else {
			for (DiffBlock<String> tokenBlock: tokenDiffs) { 
				for (String token: tokenBlock.getElements()) {
					if (tokenBlock.getOperation() != Operation.INSERT) 
						builder.append(toHtml(token, getOperationClass(tokenBlock.getOperation())));
				}
			}
		}
		builder.append("</td>");

		if (blameInfo != null) {
			appendBlame(builder, -1, newLineNo);
		}
		builder.append("<td class='number noselect new'>");
		if (isConfusable(deleteBlock.getElements().get(deleteLineIndex), insertBlock.getElements().get(insertLineIndex)))
			builder.append(getConfusableMark());		
		builder.append(newLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='operation noselect new'>+</td>");
		builder.append("<td class='content right new' data-new='").append(newLineNo).append("'>");
		if (tokenDiffs.isEmpty()) {
			builder.append("&nbsp;");
		} else {
			for (DiffBlock<String> tokenBlock: tokenDiffs) { 
				for (String token: tokenBlock.getElements()) {
					if (tokenBlock.getOperation() != Operation.DELETE) 
						builder.append(toHtml(token, getOperationClass(tokenBlock.getOperation())));
				}
			}
		}			
		builder.append("</td></tr>");		
	}
	
	private void appendExpander(StringBuilder builder, int blockIndex, int skippedLines) {
		builder.append("<tr class='expander expander").append(blockIndex).append("'>");
		
		String expandSvg = String.format("<svg class='icon'><use xlink:href='%s'/></svg>", 
				SpriteImage.getVersionedHref(IconScope.class, "expand2"));
		String ellipsisSvg = String.format("<svg class='icon'><use xlink:href='%s'/></svg>", 
				SpriteImage.getVersionedHref(IconScope.class, "ellipsis"));
		
		String script = String.format("javascript:$('#%s').data('callback')('expand', %d);", getMarkupId(), blockIndex);
		var skippedMessage = MessageFormat.format(_T("skipped {0} lines"), skippedLines);
		if (diffMode == DiffViewMode.UNIFIED) {
			if (blameInfo != null) {
				builder.append("<td colspan='3' class='expander noselect'><a data-tippy-content='" + _T("Show more lines") + "' href=\"")
						.append(script).append("\">").append(expandSvg).append("</a></td>");
				blameInfo.lastCommitHash = null;
				blameInfo.lastOldCommitHash = null;
				blameInfo.lastNewCommitHash = null;
			} else {
				builder.append("<td colspan='2' class='expander noselect'><a data-tippy-content='" + _T("Show more lines") + "' href=\"")
						.append(script).append("\">").append(expandSvg).append("</a></td>");
			}
			builder.append("<td colspan='2' class='skipped noselect'>").append(ellipsisSvg).append(" ")
					.append(skippedMessage).append(" ").append(ellipsisSvg).append("</td>");
		} else {
			if (blameInfo != null) {
				builder.append("<td colspan='2' class='expander noselect'><a data-tippy-content='" + _T("Show more lines") + "' href=\"").append(script)
						.append("\">").append(expandSvg).append("</a></td>");
				builder.append("<td class='skipped noselect' colspan='6'>").append(ellipsisSvg).append(" ")
						.append(skippedMessage).append(" ").append(ellipsisSvg).append("</td>");
				blameInfo.lastCommitHash = null;
				blameInfo.lastOldCommitHash = null;
				blameInfo.lastNewCommitHash = null;
			} else {
				builder.append("<td class='expander noselect'><a data-tippy-content='" + _T("Show more lines") + "' href=\"").append(script)
						.append("\">").append(expandSvg).append("</a></td>");
				builder.append("<td class='skipped noselect' colspan='5'>").append(ellipsisSvg).append(" ")
						.append(skippedMessage).append(" ").append(ellipsisSvg).append("</td>");
			}
		}
		builder.append("</tr>");
	}
	
	@Override
	protected void onDetach() {
		annotationInfoModel.detach();
		
		if (blameModel != null)
			blameModel.detach();
		
		super.onDetach();
	}

	public void onCommentDeleted(AjaxRequestTarget target) {
		String script = String.format("onedev.server.blobTextDiff.onCommentDeleted($('#%s'));", getMarkupId());
		target.appendJavaScript(script);
		unmark(target);
	}

	public void onCommentClosed(AjaxRequestTarget target) {
		String script = String.format("onedev.server.blobTextDiff.onCommentClosed($('#%s'));", getMarkupId());
		target.appendJavaScript(script);
		unmark(target);
	}

	public void onCommentAdded(AjaxRequestTarget target, CodeComment comment, DiffPlanarRange range) {
		String script = String.format("onedev.server.blobTextDiff.onCommentAdded($('#%s'), %s);", 
				getMarkupId(), convertToJson(new DiffCodeCommentInfo(comment, range)));
		target.appendJavaScript(script);
	}
	
	public void mark(AjaxRequestTarget target, DiffPlanarRange markRange) {
		String script = String.format(""
			+ "var $container = $('#%s');"
			+ "var markRange = %s;"
			+ "onedev.server.blobTextDiff.scrollTo($container, markRange);"
			+ "onedev.server.blobTextDiff.mark($container, markRange);", 
			getMarkupId(), convertToJson(markRange));
		target.appendJavaScript(script);
	}
	
	public void unmark(AjaxRequestTarget target) {
		String script = String.format(""
			+ "var $container = $('#%s');"
			+ "onedev.server.blobTextDiff.clearMark($container);"
			+ "$container.removeData('markRange');", 
			getMarkupId());
		target.appendJavaScript(script);
	}
	
	public void onUnblame(AjaxRequestTarget target) {
		blameInfo = null;
		target.add(this);
	}
	
	@Nullable
	public BlobAnnotationSupport getAnnotationSupport() {
		return null;
	} 

	private String getConfusableMark() {
		return String.format(
			"<a class='text-warning confusable' data-tippy-content='" + _T("This line has confusable unicode character modification") + "'><svg class='icon icon-sm'><use xlink:href='%s'/></svg></a>",
			SpriteImage.getVersionedHref("warning-o"));
	}

	private boolean isConfusable(String text1, String text2) {
		return confusableChecker.areConfusable(text1, text2) != 0;	
	}
	
	private static class BlameInfo implements Serializable {
		
		Map<Integer, BlameCommit> oldBlame = new HashMap<>();
		
		Map<Integer, BlameCommit> newBlame = new HashMap<>();
		
		String lastCommitHash;
		
		String lastOldCommitHash;
		
		String lastNewCommitHash;
		
	}

}
