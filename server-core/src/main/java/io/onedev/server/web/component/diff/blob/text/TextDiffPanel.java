package io.onedev.server.web.component.diff.blob.text;

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
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.FileMode;
import org.unbescape.html.HtmlEscape;
import org.unbescape.javascript.JavaScriptEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import io.onedev.commons.jsyntax.TextToken;
import io.onedev.commons.jsyntax.TokenUtils;
import io.onedev.commons.jsyntax.Tokenized;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.code.CodeProblem;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.git.BlameBlock;
import io.onedev.server.git.BlameCommit;
import io.onedev.server.git.BlobChange;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.BlameCommand;
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
import io.onedev.server.util.diff.LineDiff;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.blamemessage.BlameMessageBehavior;
import io.onedev.server.web.component.diff.diffstat.DiffStatBar;
import io.onedev.server.web.component.diff.difftitle.BlobDiffTitle;
import io.onedev.server.web.component.diff.revision.DiffViewMode;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.symboltooltip.SymbolTooltipPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.util.AnnotationInfo;
import io.onedev.server.web.util.CodeCommentInfo;
import io.onedev.server.web.util.DiffPlanarRange;
import io.onedev.server.web.util.EditParamsAware;

@SuppressWarnings("serial")
public class TextDiffPanel extends Panel {

	private final BlobChange change;
	
	private final Map<Integer, Integer> contextSizes = new HashMap<>();
	
	private final DiffViewMode diffMode;

	private final IModel<Boolean> blameModel;
	
	private final IModel<DiffAnnotationInfo> annotationInfoModel = new LoadableDetachableModel<DiffAnnotationInfo>() {

		@Override
		protected DiffAnnotationInfo load() {
			if (change.getAnnotationSupport() != null) {
				Map<Integer, List<CodeCommentInfo>> oldCommentsByLine = 
						CodeCommentInfo.groupByLine(change.getAnnotationSupport().getOldComments());
				Map<Integer, List<CodeCommentInfo>> newCommentsByLine = 
						CodeCommentInfo.groupByLine(change.getAnnotationSupport().getNewComments());

				List<CodeProblem> oldProblems = new ArrayList<>();
				for (CodeProblem problem: change.getAnnotationSupport().getOldProblems()) 
					oldProblems.add(problem.normalizeRange(change.getOldText().getLines()));
				Map<Integer, List<CodeProblem>> oldProblemsByLine = CodeProblem.groupByLine(oldProblems);
				
				List<CodeProblem> newProblems = new ArrayList<>();
				for (CodeProblem problem: change.getAnnotationSupport().getNewProblems()) 
					newProblems.add(problem.normalizeRange(change.getNewText().getLines()));
				Map<Integer, List<CodeProblem>> newProblemsByLine = CodeProblem.groupByLine(newProblems);
				
				return new DiffAnnotationInfo(
						new AnnotationInfo(oldCommentsByLine, oldProblemsByLine, 
								change.getAnnotationSupport().getOldCoverages()), 
						new AnnotationInfo(newCommentsByLine, newProblemsByLine, 
								change.getAnnotationSupport().getNewCoverages()));
			} else {
				return new DiffAnnotationInfo(
						new AnnotationInfo(new HashMap<>(), new HashMap<>(), new HashMap<>()), 
						new AnnotationInfo(new HashMap<>(), new HashMap<>(), new HashMap<>()));
			}
		}
		
	};
	
	private Component symbolTooltip;
	
	private AbstractPostAjaxBehavior callbackBehavior;
	
	private BlameMessageBehavior blameMessageBehavior;
	
	private BlameInfo blameInfo;
	
	public TextDiffPanel(String id, BlobChange change, DiffViewMode diffMode, 
			@Nullable IModel<Boolean> blameModel) {
		super(id);
		
		this.change = change;
		this.diffMode = diffMode;
		this.blameModel = blameModel;
		
		if (blameModel != null && blameModel.getObject()) {
			blameInfo = getBlameInfo();
		}
		
	}

	private String convertToJson(Object obj) {
		try {
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Project getProject() {
		return change.getProject();
	}
	
	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}
	
	private BlameInfo getBlameInfo() {
		blameInfo = new BlameInfo();
		BlameCommand cmd = new BlameCommand(getProject().getGitDir());
		String oldPath = change.getOldBlobIdent().path;
		if (oldPath != null) {
			cmd.commitHash(change.getOldCommitId().name()).file(oldPath);
			for (BlameBlock blame: cmd.call()) {
				for (LinearRange range: blame.getRanges()) {
					for (int i=range.getFrom(); i<=range.getTo(); i++) 
						blameInfo.oldBlame.put(i, blame.getCommit());
				}
			}
		}
		String newPath = change.getNewBlobIdent().path;
		if (newPath != null) {
			cmd.commitHash(change.getNewCommitId().name()).file(newPath);
			for (BlameBlock blame: cmd.call()) {
				for (LinearRange range: blame.getRanges()) {
					for (int i=range.getFrom(); i<=range.getTo(); i++) 
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
				((BasePage)getPage()).resizeWindow(target);
			}
			
		}.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return blameInfo!=null? "active": "";
			}
			
		})).setOutputMarkupId(true));
		
		ProjectBlobPage.State viewState = new ProjectBlobPage.State(change.getBlobIdent());

		viewState.requestId = PullRequest.idOf(getPullRequest());
		PageParameters params = ProjectBlobPage.paramsOf(getProject(), viewState);
		actions.add(new ViewStateAwarePageLink<Void>("viewFile", ProjectBlobPage.class, params));
		
		if (change.getType() != ChangeType.DELETE) {
			if (getPullRequest() != null 
					&& getPullRequest().getSource() != null 
					&& getPullRequest().getSource().getObjectName(false) != null
					&& SecurityUtils.canModify(getPullRequest().getSourceProject(), getPullRequest().getSourceBranch(), change.getPath())) { 
				// we are in context of a pull request and pull request source branch exists, so we edit in source branch instead
				Link<Void> editLink = new Link<Void>("editFile") {

					@Override
					public void onClick() {
						BlobIdent blobIdent = new BlobIdent(getPullRequest().getSourceBranch(), change.getPath(), 
								FileMode.REGULAR_FILE.getBits());
						ProjectBlobPage.State editState = new ProjectBlobPage.State(blobIdent);
						editState.requestId = getPullRequest().getId();
						editState.mode = Mode.EDIT;
						editState.urlBeforeEdit = EditParamsAware.getUrlBeforeEdit(getPage());
						editState.urlAfterEdit = EditParamsAware.getUrlAfterEdit(getPage());
						PageParameters params = ProjectBlobPage.paramsOf(getPullRequest().getSourceProject(), editState);
						setResponsePage(ProjectBlobPage.class, params);
					}
					
				};
				editLink.add(AttributeAppender.append("title", "Edit on source branch"));
				actions.add(editLink);
			} else if (SecurityUtils.canModify(getProject(), change.getBlobIdent().revision, change.getPath()) 
					&& getProject().getBranchRef(change.getBlobIdent().revision) != null) {
				// we are on a branch 
				Link<Void> editLink = new Link<Void>("editFile") {

					@Override
					public void onClick() {
						ProjectBlobPage.State editState = new ProjectBlobPage.State(change.getBlobIdent());
						editState.mode = Mode.EDIT;
						editState.urlBeforeEdit = EditParamsAware.getUrlBeforeEdit(getPage());
						editState.urlAfterEdit = EditParamsAware.getUrlAfterEdit(getPage());
						PageParameters params = ProjectBlobPage.paramsOf(getProject(), editState);
						setResponsePage(ProjectBlobPage.class, params);
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
					
					String expanded = StringUtils.replace(builder.toString(), "\"", "\\\"");
					expanded = StringUtils.replace(expanded, "\n", "");
					String script = String.format("onedev.server.textDiff.expand('%s', %d, \"%s\");",
							getMarkupId(), index, expanded);
					target.appendJavaScript(script);
					break;
				case "openSelectionPopover":
					String jsonOfPosition = String.format("{left: %d, top: %d}", 
							params.getParameterValue("param1").toInt(), 
							params.getParameterValue("param2").toInt());
					DiffPlanarRange commentRange = getRange(params, "param3", "param4", "param5", "param6", "param7");
					
					String markUrl;
					if (change.getAnnotationSupport() != null) {
						markUrl = change.getAnnotationSupport().getMarkUrl(commentRange);
						if (markUrl != null) 
							markUrl = "'" + JavaScriptEscape.escapeJavaScript(markUrl) + "'";
						else 
							markUrl = "undefined";
					} else {
						markUrl = "undefined";
					}
					script = String.format("onedev.server.textDiff.openSelectionPopover('%s', %s, %s, %s, '%s', %s);", 
							getMarkupId(), jsonOfPosition, convertToJson(commentRange), markUrl, 
							JavaScriptEscape.escapeJavaScript(getMarkedText(commentRange)),
							SecurityUtils.getUser()!=null);
					target.appendJavaScript(script);
					break;
				case "addComment":
					Preconditions.checkNotNull(SecurityUtils.getUser());
					
					commentRange = getRange(params, "param1", "param2", "param3", "param4", "param5");
					change.getAnnotationSupport().onAddComment(target, commentRange);
					script = String.format("onedev.server.textDiff.onAddComment($('#%s'), %s);", 
							getMarkupId(), convertToJson(commentRange));
					target.appendJavaScript(script);
					break;
				case "openComment": 
					Long commentId = params.getParameterValue("param1").toLong();
					commentRange = getRange(params, "param2", "param3", "param4", "param5", "param6");
					CodeComment comment = OneDev.getInstance(CodeCommentManager.class).load(commentId);
					change.getAnnotationSupport().onOpenComment(target, comment, commentRange);
					script = String.format("onedev.server.textDiff.onCommentOpened($('#%s'), %s);", 
							getMarkupId(), convertToJson(new DiffCodeCommentInfo(comment, commentRange)));
					target.appendJavaScript(script);
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
				return blameInfo!=null? "need-width": "";
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
	
	private void appendEquals(StringBuilder builder, int index, int lastContextSize, int contextSize) {
		DiffBlock<Tokenized> block = change.getDiffBlocks().get(index);
		if (index == 0) {
			int start = block.getUnits().size()-contextSize;
			if (start < 0)
				start=0;
			else if (start > 0)
				appendExpander(builder, index, start);
			for (int j=start; j<block.getUnits().size()-lastContextSize; j++) 
				appendEqual(builder, block, j, lastContextSize);
		} else if (index == change.getDiffBlocks().size()-1) {
			int end = block.getUnits().size();
			int skipped = 0;
			if (end > contextSize) {
				skipped = end-contextSize;
				end = contextSize;
			}
			for (int j=lastContextSize; j<end; j++)
				appendEqual(builder, block, j, lastContextSize);
			if (skipped != 0)
				appendExpander(builder, index, skipped);
		} else if (2*contextSize < block.getUnits().size()) {
			for (int j=lastContextSize; j<contextSize; j++)
				appendEqual(builder, block, j, lastContextSize);
			appendExpander(builder, index, block.getUnits().size() - 2*contextSize);
			for (int j=block.getUnits().size()-contextSize; j<block.getUnits().size()-lastContextSize; j++)
				appendEqual(builder, block, j, lastContextSize);
		} else {
			for (int j=lastContextSize; j<block.getUnits().size()-lastContextSize; j++)
				appendEqual(builder, block, j, lastContextSize);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new TextDiffResourceReference()));
		
		DiffCodeCommentInfo openCommentInfo;
		DiffPlanarRange markRange;
		String commentContainerId;
		if (change.getAnnotationSupport() != null) {
			markRange = change.getAnnotationSupport().getMarkRange();
			Pair<CodeComment, DiffPlanarRange> openCommentPair = change.getAnnotationSupport().getOpenComment();
			if (openCommentPair != null) 
				openCommentInfo = new DiffCodeCommentInfo(openCommentPair.getFirst(), openCommentPair.getSecond());
			else 
				openCommentInfo = null;
			commentContainerId = "'" + change.getAnnotationSupport().getCommentContainer().getMarkupId() + "'";
		} else {
			markRange = null;
			openCommentInfo = null;
			commentContainerId = null;
		}
		
		CharSequence callback = callbackBehavior.getCallbackFunction(
				explicit("action"), explicit("param1"), explicit("param2"), 
				explicit("param3"), explicit("param4"), explicit("param5"),
				explicit("param6"), explicit("param7"), explicit("param8")); 
		
		String script = String.format("onedev.server.textDiff.onDomReady('%s', '%s', '%s', '%s', %s, %s, %s,"
				+ "%s, %s, %s, '%s');", getMarkupId(), symbolTooltip.getMarkupId(), 
				change.getOldBlobIdent().revision, change.getNewBlobIdent().revision,
				callback, blameMessageBehavior.getCallback(),
				convertToJson(markRange), convertToJson(openCommentInfo), 
				convertToJson(annotationInfoModel.getObject()), 
				commentContainerId, OneDev.getInstance().getDocRoot());
		
		response.render(OnDomReadyHeaderItem.forScript(script));

		if (markRange != null && RequestCycle.get().find(AjaxRequestTarget.class) == null) {
			script = String.format("onedev.server.textDiff.onWindowLoad('%s', %s);", 
					getMarkupId(), convertToJson(markRange));
			response.render(OnLoadHeaderItem.forScript(script));
		}
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
			DiffBlock<Tokenized> block = change.getDiffBlocks().get(i);
			if (block.getOperation() == Operation.EQUAL) {
				Integer lastContextSize = contextSizes.get(i);
				if (lastContextSize == null)
					lastContextSize = WebConstants.DIFF_CONTEXT_SIZE;
				appendEquals(builder, i, 0, lastContextSize);
			} else if (block.getOperation() == Operation.DELETE) {
				if (i+1<change.getDiffBlocks().size()) {
					DiffBlock<Tokenized> nextBlock = change.getDiffBlocks().get(i+1);
					if (nextBlock.getOperation() == Operation.INSERT) {
						LinkedHashMap<Integer, LineDiff> lineChanges = 
								DiffUtils.align(block.getUnits(), nextBlock.getUnits(), false);
						if (blameInfo != null && diffMode == DiffViewMode.UNIFIED) {
							for (int j=0; j<block.getUnits().size(); j++) { 
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
							for (int j=0; j<nextBlock.getUnits().size(); j++) {
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
								
								appendModification(builder, block, nextBlock, deleteLineIndex, insertLineIndex, 
										lineChange.getTokenDiffs()); 
								
								prevDeleteLineIndex = deleteLineIndex+1;
								prevInsertLineIndex = insertLineIndex+1;
							}
							appendDeletesAndInserts(builder, block, nextBlock, 
									prevDeleteLineIndex, block.getUnits().size(), 
									prevInsertLineIndex, nextBlock.getUnits().size());
						}
						i++;
					} else {
						for (int j=0; j<block.getUnits().size(); j++) 
							appendDelete(builder, block, j, null);
					}
				} else {
					for (int j=0; j<block.getUnits().size(); j++) 
						appendDelete(builder, block, j, null);
				}
			} else {
				for (int j=0; j<block.getUnits().size(); j++) 
					appendInsert(builder, block, j, null);
			}
		}
		return builder.toString();
	}

	private void appendDeletesAndInserts(StringBuilder builder, DiffBlock<Tokenized> deleteBlock, 
			DiffBlock<Tokenized> insertBlock, int fromDeleteLineIndex, int toDeleteLineIndex, 
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
		BlameCommit commit;
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
			PageParameters params = CommitDetailPage.paramsOf(getProject(), state);
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
	}
	
	private String getOperationClass(Operation operation) {
		if (operation == Operation.INSERT)
			return "insert";
		else if (operation == Operation.DELETE)
			return "delete";
		else
			return null;
	}
	
	private void appendTokenized(StringBuilder builder, Tokenized tokenized) {
		if (tokenized.getTokens().length == 0) {
			builder.append("&nbsp;");
		} else {
			for (long token: tokenized.getTokens()) {
				builder.append(TokenUtils.toHtml(tokenized.getText(), token, null, null));
			}
		}
	}
	
	private void appendEqual(StringBuilder builder, DiffBlock<Tokenized> block, int lineIndex, int lastContextSize) {
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
			builder.append("<td class='content' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			appendTokenized(builder, block.getUnits().get(lineIndex));
			builder.append("</td>");
		} else {
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, -1);
			}
			builder.append("<td class='number noselect'>").append(oldLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content left' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			appendTokenized(builder, block.getUnits().get(lineIndex));
			builder.append("</td>");
			if (blameInfo != null) {
				appendBlame(builder, -1, newLineNo);
			}
			builder.append("<td class='number noselect'>").append(newLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content right' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			appendTokenized(builder, block.getUnits().get(lineIndex));
			builder.append("</td>");
		}
		builder.append("</tr>");
	}
	
	private void appendInsert(StringBuilder builder, DiffBlock<Tokenized> block, int lineIndex, 
			@Nullable List<DiffBlock<TextToken>> tokenDiffs) {
		builder.append("<tr class='code original'>");

		int newLineNo = block.getNewStart() + lineIndex;
		if (diffMode == DiffViewMode.UNIFIED) {
			if (blameInfo != null) {
				appendBlame(builder, -1, newLineNo);
			}
			builder.append("<td class='number noselect new'><a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='number noselect new'>").append(newLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect new'>+</td>");
			builder.append("<td class='content new' data-new='").append(newLineNo).append("'>");
			if (tokenDiffs != null) {
				if (tokenDiffs.isEmpty()) {
					builder.append("&nbsp;");
				} else {
					for (DiffBlock<TextToken> tokenBlock: tokenDiffs) { 
						for (TextToken token: tokenBlock.getUnits()) {
							if (tokenBlock.getOperation() != Operation.DELETE) 
								builder.append(TokenUtils.toHtml(token, getOperationClass(tokenBlock.getOperation()), null));
						}
					}
				}			
			} else {
				appendTokenized(builder, block.getUnits().get(lineIndex));
			}
			builder.append("</td>");
		} else {
			if (blameInfo != null) {
				builder.append("<td class='blame noselect'>&nbsp;</td>");
			}
			builder.append("<td class='number noselect'><a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect'></td>");
			builder.append("<td class='content left'>&nbsp;</td>");
			if (blameInfo != null) {
				appendBlame(builder, -1, newLineNo);
			}
			builder.append("<td class='number noselect new'>").append(newLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect new'>+</td>");
			builder.append("<td class='content right new' data-new='").append(newLineNo).append("'>");
			appendTokenized(builder, block.getUnits().get(lineIndex));
			builder.append("</td>");
		}
		builder.append("</tr>");
	}
	
	private void appendDelete(StringBuilder builder, DiffBlock<Tokenized> block, int lineIndex, 
			@Nullable List<DiffBlock<TextToken>> tokenDiffs) {
		builder.append("<tr class='code original'>");
		
		int oldLineNo = block.getOldStart() + lineIndex;
		if (diffMode == DiffViewMode.UNIFIED ) {
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
					for (DiffBlock<TextToken> tokenBlock: tokenDiffs) { 
						for (TextToken token: tokenBlock.getUnits()) {
							if (tokenBlock.getOperation() != Operation.INSERT) 
								builder.append(TokenUtils.toHtml(token, getOperationClass(tokenBlock.getOperation()), null));
						}
					}
				}
			} else {
				appendTokenized(builder, block.getUnits().get(lineIndex));
			}
			builder.append("</td>");
		} else {
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, -1);
			}
			builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect old'>-</td>");
			builder.append("<td class='content left old' data-old='").append(oldLineNo).append("'>");
			appendTokenized(builder, block.getUnits().get(lineIndex));
			builder.append("</td>");
			if (blameInfo != null) {
				builder.append("<td class='blame noselect'>&nbsp;</td>");
			}
			builder.append("<td class='number noselect'><a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content right'>&nbsp;</td>");
		}
		builder.append("</tr>");
	}
	
	private void appendSideBySide(StringBuilder builder, DiffBlock<Tokenized> deleteBlock, 
			DiffBlock<Tokenized> insertBlock, int deleteLineIndex, int insertLineIndex) {
		builder.append("<tr class='code original'>");

		int oldLineNo = deleteBlock.getOldStart()+deleteLineIndex;
		if (blameInfo != null) {
			appendBlame(builder, oldLineNo, -1);
		}
		builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='operation noselect old'>-</td>");
		builder.append("<td class='content left old' data-old='").append(oldLineNo).append("'>");
		appendTokenized(builder, deleteBlock.getUnits().get(deleteLineIndex));
		builder.append("</td>");
		
		int newLineNo = insertBlock.getNewStart()+insertLineIndex;
		if (blameInfo != null) {
			appendBlame(builder, -1, newLineNo);
		}
		builder.append("<td class='number noselect new'>").append(newLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
		builder.append("<td class='operation noselect new'>+</td>");
		builder.append("<td class='content right new' data-new='").append(newLineNo).append("'>");
		appendTokenized(builder, insertBlock.getUnits().get(insertLineIndex));
		builder.append("</td>");
		
		builder.append("</tr>");
	}

	private void appendModification(StringBuilder builder, DiffBlock<Tokenized> deleteBlock, 
			DiffBlock<Tokenized> insertBlock, int deleteLineIndex, int insertLineIndex, 
			List<DiffBlock<TextToken>> tokenDiffs) {
		builder.append("<tr class='code original'>");

		int oldLineNo = deleteBlock.getOldStart() + deleteLineIndex;
		int newLineNo = insertBlock.getNewStart() + insertLineIndex;
		if (diffMode == DiffViewMode.UNIFIED) {
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, newLineNo);
			}
			builder.append("<td class='number noselect old new'>").append(oldLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='number noselect old new'>").append(newLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect old new'>*</td>");
			builder.append("<td class='content old new' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			if (tokenDiffs.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (DiffBlock<TextToken> tokenBlock: tokenDiffs) { 
					for (TextToken token: tokenBlock.getUnits()) 
						builder.append(TokenUtils.toHtml(token, getOperationClass(tokenBlock.getOperation()), null));
				}
			}
			builder.append("</td>");
		} else {
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, -1);
			}
			builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect old'>-</td>");
			builder.append("<td class='content left old' data-old='").append(oldLineNo).append("'>");
			if (tokenDiffs.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (DiffBlock<TextToken> tokenBlock: tokenDiffs) { 
					for (TextToken token: tokenBlock.getUnits()) {
						if (tokenBlock.getOperation() != Operation.INSERT) 
							builder.append(TokenUtils.toHtml(token, getOperationClass(tokenBlock.getOperation()), null));
					}
				}
			}
			builder.append("</td>");
			
			if (blameInfo != null) {
				appendBlame(builder, -1, newLineNo);
			}
			builder.append("<td class='number noselect new'>").append(newLineNo+1).append("<a class='coverage'>&nbsp;</a></td>");
			builder.append("<td class='operation noselect new'>+</td>");
			builder.append("<td class='content right new' data-new='").append(newLineNo).append("'>");
			if (tokenDiffs.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (DiffBlock<TextToken> tokenBlock: tokenDiffs) { 
					for (TextToken token: tokenBlock.getUnits()) {
						if (tokenBlock.getOperation() != Operation.DELETE) 
							builder.append(TokenUtils.toHtml(token, getOperationClass(tokenBlock.getOperation()), null));
					}
				}
			}			
			builder.append("</td>");
		}
		
		builder.append("</tr>");
	}
	
	private void appendExpander(StringBuilder builder, int blockIndex, int skippedLines) {
		builder.append("<tr class='expander expander").append(blockIndex).append("'>");
		
		String expandSvg = String.format("<svg class='icon'><use xlink:href='%s'/></svg>", 
				SpriteImage.getVersionedHref(IconScope.class, "expand2"));
		String ellipsisSvg = String.format("<svg class='icon'><use xlink:href='%s'/></svg>", 
				SpriteImage.getVersionedHref(IconScope.class, "ellipsis"));
		
		String script = String.format("javascript:$('#%s').data('callback')('expand', %d);", getMarkupId(), blockIndex);
		if (diffMode == DiffViewMode.UNIFIED) {
			if (blameInfo != null) {
				builder.append("<td colspan='3' class='expander noselect'><a title='Show more lines' href=\"")
						.append(script).append("\">").append(expandSvg).append("</a></td>");
				blameInfo.lastCommitHash = null;
				blameInfo.lastOldCommitHash = null;
				blameInfo.lastNewCommitHash = null;
			} else {
				builder.append("<td colspan='2' class='expander noselect'><a title='Show more lines' href=\"")
						.append(script).append("\">").append(expandSvg).append("</a></td>");
			}
			builder.append("<td colspan='2' class='skipped noselect'>").append(ellipsisSvg).append(" skipped ")
					.append(skippedLines).append(" lines ").append(ellipsisSvg).append("</td>");
		} else {
			if (blameInfo != null) {
				builder.append("<td colspan='2' class='expander noselect'><a title='Show more lines' href=\"").append(script)
						.append("\">").append(expandSvg).append("</a></td>");
				builder.append("<td class='skipped noselect' colspan='6'>").append(ellipsisSvg).append(" skipped ")
						.append(skippedLines).append(" lines ").append(ellipsisSvg).append("</td>");
				blameInfo.lastCommitHash = null;
				blameInfo.lastOldCommitHash = null;
				blameInfo.lastNewCommitHash = null;
			} else {
				builder.append("<td class='expander noselect'><a title='Show more lines' href=\"").append(script)
						.append("\">").append(expandSvg).append("</a></td>");
				builder.append("<td class='skipped noselect' colspan='5'>").append(ellipsisSvg).append(" skipped ")
						.append(skippedLines).append(" lines ").append(ellipsisSvg).append("</td>");
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
		String script = String.format("onedev.server.textDiff.onCommentDeleted($('#%s'));", getMarkupId());
		target.appendJavaScript(script);
		unmark(target);
	}

	public void onCommentClosed(AjaxRequestTarget target) {
		String script = String.format("onedev.server.textDiff.onCommentClosed($('#%s'));", getMarkupId());
		target.appendJavaScript(script);
		unmark(target);
	}

	public void onCommentAdded(AjaxRequestTarget target, CodeComment comment, DiffPlanarRange range) {
		String script = String.format("onedev.server.textDiff.onCommentAdded($('#%s'), %s);", 
				getMarkupId(), convertToJson(new DiffCodeCommentInfo(comment, range)));
		target.appendJavaScript(script);
	}
	
	public void mark(AjaxRequestTarget target, DiffPlanarRange markRange) {
		String script = String.format(""
			+ "var $container = $('#%s');"
			+ "var markRange = %s;"
			+ "onedev.server.textDiff.scrollTo($container, markRange);"
			+ "onedev.server.textDiff.mark($container, markRange);", 
			getMarkupId(), convertToJson(markRange));
		target.appendJavaScript(script);
	}
	
	public void unmark(AjaxRequestTarget target) {
		String script = String.format(""
			+ "var $container = $('#%s');"
			+ "onedev.server.textDiff.clearMark($container);"
			+ "$container.removeData('markRange');", 
			getMarkupId());
		target.appendJavaScript(script);
	}
	
	public void onUnblame(AjaxRequestTarget target) {
		blameInfo = null;
		target.add(this);
	}
	
	private static class BlameInfo implements Serializable {
		
		Map<Integer, BlameCommit> oldBlame = new HashMap<>();
		
		Map<Integer, BlameCommit> newBlame = new HashMap<>();
		
		String lastCommitHash;
		
		String lastOldCommitHash;
		
		String lastNewCommitHash;
		
	}

}
