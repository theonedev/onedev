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
import org.eclipse.jgit.lib.ObjectId;
import org.unbescape.html.HtmlEscape;
import org.unbescape.javascript.JavaScriptEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import io.onedev.commons.jsyntax.TextToken;
import io.onedev.commons.jsyntax.TokenUtils;
import io.onedev.commons.jsyntax.Tokenized;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
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
import io.onedev.server.model.support.Mark;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.RangeUtils;
import io.onedev.server.util.diff.DiffBlock;
import io.onedev.server.util.diff.DiffMatchPatch.Operation;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.util.diff.LineDiff;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.blamemessage.BlameMessageBehavior;
import io.onedev.server.web.component.diff.blob.SourceAware;
import io.onedev.server.web.component.diff.blob.text.MarkAwareDiffBlock.Type;
import io.onedev.server.web.component.diff.diffstat.DiffStatBar;
import io.onedev.server.web.component.diff.difftitle.BlobDiffTitle;
import io.onedev.server.web.component.diff.revision.BlobCommentSupport;
import io.onedev.server.web.component.diff.revision.DiffViewMode;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.symboltooltip.SymbolTooltipPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.page.project.pullrequests.detail.mergepreview.MergePreviewPage;
import io.onedev.server.web.util.EditParamsAware;

@SuppressWarnings("serial")
public class TextDiffPanel extends Panel implements SourceAware {

	private final IModel<Project> projectModel;
	
	private final IModel<PullRequest> requestModel;
	
	private final BlobChange change;
	
	private final Map<Integer, Integer> contextSizes = new HashMap<>();
	
	private final DiffViewMode diffMode;

	private final IModel<Boolean> blameModel;
	
	private final BlobCommentSupport commentSupport;
	
	private final List<Mark> initialMarks;
	
	private Component symbolTooltip;
	
	private AbstractPostAjaxBehavior callbackBehavior;
	
	private BlameMessageBehavior blameMessageBehavior;
	
	private transient List<MarkAwareDiffBlock> diffBlocks;
	
	private BlameInfo blameInfo;
	
	public TextDiffPanel(String id, IModel<Project> projectModel, IModel<PullRequest> requestModel, 
			BlobChange change, DiffViewMode diffMode, @Nullable IModel<Boolean> blameModel, 
			@Nullable BlobCommentSupport commentSupport) {
		super(id);
		
		this.projectModel = projectModel;
		this.requestModel = requestModel;
		this.change = change;
		this.diffMode = diffMode;
		this.commentSupport = commentSupport;
		this.blameModel = blameModel;
		
		if (blameModel != null && blameModel.getObject()) {
			blameInfo = getBlameInfo();
		}
		
		initialMarks = new ArrayList<>();
		if (commentSupport != null) {
			Mark mark = commentSupport.getMark();
			if (mark != null) {
				initialMarks.add(mark);
			}
			for (CodeComment comment: commentSupport.getComments()) {
				mark = comment.getMark();
				initialMarks.add(mark);
			}
		}
	}

	private String getJson(Mark mark) {
		try {
			MarkInfo markInfo = new MarkInfo(mark, getOldCommit().name());
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(markInfo);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private BlameInfo getBlameInfo() {
		blameInfo = new BlameInfo();
		BlameCommand cmd = new BlameCommand(projectModel.getObject().getGitDir());
		String oldPath = change.getOldBlobIdent().path;
		if (oldPath != null) {
			cmd.commitHash(getOldCommit().name()).file(oldPath);
			for (BlameBlock blame: cmd.call()) {
				for (LinearRange range: blame.getRanges()) {
					for (int i=range.getFrom(); i<=range.getTo(); i++) 
						blameInfo.oldBlame.put(i, blame.getCommit());
				}
			}
		}
		String newPath = change.getNewBlobIdent().path;
		if (newPath != null) {
			cmd.commitHash(getNewCommit().name()).file(newPath);
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
		
		Project project = projectModel.getObject();
		PullRequest request = requestModel.getObject();
		ProjectBlobPage.State viewState = new ProjectBlobPage.State(change.getBlobIdent());

		viewState.requestId = PullRequest.idOf(request);
		PageParameters params = ProjectBlobPage.paramsOf(project, viewState);
		actions.add(new ViewStateAwarePageLink<Void>("viewFile", ProjectBlobPage.class, params));
		
		if (change.getType() != ChangeType.DELETE) {
			if (request != null 
					&& request.getSource() != null 
					&& request.getSource().getObjectName(false) != null
					&& SecurityUtils.canModify(request.getSourceProject(), request.getSourceBranch(), change.getPath())
					&& (getPage() instanceof PullRequestChangesPage || getPage() instanceof MergePreviewPage)) { 
				// we are in context of a pull request and pull request source branch exists, so we edit in source branch instead
				Link<Void> editLink = new Link<Void>("editFile") {

					@Override
					public void onClick() {
						BlobIdent blobIdent = new BlobIdent(request.getSourceBranch(), change.getPath(), 
								FileMode.REGULAR_FILE.getBits());
						ProjectBlobPage.State editState = new ProjectBlobPage.State(blobIdent);
						editState.requestId = request.getId();
						editState.mode = Mode.EDIT;
						editState.urlBeforeEdit = EditParamsAware.getUrlBeforeEdit(getPage());
						editState.urlAfterEdit = EditParamsAware.getUrlAfterEdit(getPage());
						PageParameters params = ProjectBlobPage.paramsOf(request.getSourceProject(), editState);
						setResponsePage(ProjectBlobPage.class, params);
					}
					
				};
				editLink.add(AttributeAppender.append("title", "Edit on source branch"));
				actions.add(editLink);
			} else if (SecurityUtils.canModify(project, change.getBlobIdent().revision, change.getPath()) 
					&& project.getBranchRef(change.getBlobIdent().revision) != null) {
				// we are on a branch 
				Link<Void> editLink = new Link<Void>("editFile") {

					@Override
					public void onClick() {
						ProjectBlobPage.State editState = new ProjectBlobPage.State(change.getBlobIdent());
						editState.mode = Mode.EDIT;
						editState.urlBeforeEdit = EditParamsAware.getUrlBeforeEdit(getPage());
						editState.urlAfterEdit = EditParamsAware.getUrlAfterEdit(getPage());
						PageParameters params = ProjectBlobPage.paramsOf(project, editState);
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
			
			private String getMarkedText(Mark mark) {
				List<String> lines;
				if (mark.getCommitHash().equals(getOldCommit().name()))
					lines = change.getOldText().getLines();
				else 
					lines = change.getNewText().getLines();
				StringBuilder builder = new StringBuilder();
				if (mark.getRange().getFromRow() != mark.getRange().getToRow()) {
					String line = lines.get(mark.getRange().getFromRow());
					int beginChar = mark.getRange().getFromColumn();
					if (beginChar < line.length())
						builder.append(line.substring(beginChar));
					for (int i=mark.getRange().getFromRow()+1; i<mark.getRange().getToRow(); i++) {
						if (builder.length() != 0)
							builder.append("\n");
						builder.append(lines.get(i));
					}
					line = lines.get(mark.getRange().getToRow());
					int endChar = mark.getRange().getToColumn();
					if (endChar > 0) {
						if (endChar > line.length())
							endChar = line.length();
						if (builder.length() != 0)
							builder.append("\n");
						builder.append(line.substring(0, endChar));
					}
				} else {
					String line = lines.get(mark.getRange().getFromRow());
					int beginChar = mark.getRange().getFromColumn();
					int endChar = mark.getRange().getToColumn();
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
					Mark mark = getMark(params, "param3", "param4", "param5", "param6", "param7");
					
					String markUrl;
					if (commentSupport != null) {
						markUrl = commentSupport.getMarkUrl(mark);
						if (markUrl != null) 
							markUrl = "'" + JavaScriptEscape.escapeJavaScript(markUrl) + "'";
						else 
							markUrl = "undefined";
					} else {
						markUrl = "undefined";
					}
					script = String.format("onedev.server.textDiff.openSelectionPopover('%s', %s, %s, %s, '%s', %s);", 
							getMarkupId(), jsonOfPosition, getJson(mark), markUrl, 
							JavaScriptEscape.escapeJavaScript(getMarkedText(mark)),
							SecurityUtils.getUser()!=null);
					target.appendJavaScript(script);
					break;
				case "addComment":
					Preconditions.checkNotNull(SecurityUtils.getUser());
					
					mark = getMark(params, "param1", "param2", "param3", "param4", "param5");
					commentSupport.onAddComment(target, mark);
					script = String.format("onedev.server.textDiff.onAddComment($('#%s'), %s);", 
							getMarkupId(), getJson(mark));
					target.appendJavaScript(script);
					break;
				case "openComment": 
					Long commentId = params.getParameterValue("param1").toLong();
					CodeComment comment = OneDev.getInstance(CodeCommentManager.class).load(commentId);
					commentSupport.onOpenComment(target, comment);
					script = String.format("onedev.server.textDiff.onOpenComment($('#%s'), %s);", 
							getMarkupId(), getJsonOfComment(comment));
					target.appendJavaScript(script);
					break;
				}
			}

		});
		
		add(blameMessageBehavior = new BlameMessageBehavior() {
			
			@Override
			protected Project getProject() {
				return projectModel.getObject();
			}
		});
		
		symbolTooltip = new SymbolTooltipPanel("symbolTooltip", projectModel) {

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
	
	private Mark getMark(IRequestParameters params, String leftSideParam, 
			String beginLineParam, String beginCharParam, 
			String endLineParam, String endCharParam) {
		boolean leftSide = params.getParameterValue(leftSideParam).toBoolean();
		int beginLine = params.getParameterValue(beginLineParam).toInt();
		int beginChar = params.getParameterValue(beginCharParam).toInt();
		int endLine = params.getParameterValue(endLineParam).toInt();
		int endChar = params.getParameterValue(endCharParam).toInt();
		String commit = leftSide?getOldCommit().name():getNewCommit().name();
		return new Mark(commit, change.getPath(), new PlanarRange(beginLine, beginChar, endLine, endChar));
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
	
	private ObjectId getOldCommit() {
		String oldRev = change.getOldBlobIdent().revision;
		if (oldRev.equals(ObjectId.zeroId().name().toString())) {
			return ObjectId.zeroId();
		} else {
			return projectModel.getObject().getRevCommit(oldRev, true);
		}
	}
	
	private ObjectId getNewCommit() {
		String newRev = change.getNewBlobIdent().revision;
		if (newRev.equals(ObjectId.zeroId().name().toString())) {
			return ObjectId.zeroId();
		} else {
			return projectModel.getObject().getRevCommit(newRev, true);
		}
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
		if (commentSupport != null) {
			String oldCommitHash = getOldCommit().name();
			Map<Integer, List<CommentInfo>> oldCommentInfos = new HashMap<>(); 
			Map<Integer, List<CommentInfo>> newCommentInfos = new HashMap<>(); 
			for (CodeComment comment: commentSupport.getComments()) {
				if (comment.getMark().getRange() != null) {
					int line = comment.getMark().getRange().getFromRow();
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
			
			ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
			try {
				jsonOfOldCommentInfos = mapper.writeValueAsString(oldCommentInfos);
				jsonOfNewCommentInfos = mapper.writeValueAsString(newCommentInfos);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
			
			Mark mark = commentSupport.getMark();
			if (mark != null) {
				jsonOfMark = getJson(mark);
			} else {
				jsonOfMark = "undefined";
			}
			CodeComment comment = commentSupport.getOpenComment();
			if (comment != null) {
				jsonOfCommentInfo = getJsonOfComment(comment);
			} else {
				jsonOfCommentInfo = "undefined";
			}
			dirtyContainerId = "'" + commentSupport.getDirtyContainer().getMarkupId() + "'";
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
		
		String script = String.format("onedev.server.textDiff.onDomReady('%s', '%s', '%s', '%s', %s, %s, %s,"
				+ "%s, %s, %s, %s, '%s');", getMarkupId(), symbolTooltip.getMarkupId(), 
				change.getOldBlobIdent().revision, change.getNewBlobIdent().revision,
				callback, blameMessageBehavior.getCallback(),
				jsonOfMark, jsonOfCommentInfo, jsonOfOldCommentInfos, jsonOfNewCommentInfos, 
				dirtyContainerId, OneDev.getInstance().getDocRoot());
		
		response.render(OnDomReadyHeaderItem.forScript(script));

		script = String.format("onedev.server.textDiff.onWindowLoad('%s', %b, %s);", 
				getMarkupId(), RequestCycle.get().find(AjaxRequestTarget.class) != null, jsonOfMark);
		response.render(OnLoadHeaderItem.forScript(script));
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
					lastContextSize = WebConstants.DIFF_CONTEXT_SIZE;
				appendEquals(builder, i, 0, lastContextSize);
			} else if (block.getType() == Type.MARKED_EQUAL) {
				appendMarkedEquals(builder, i);
			} else if (block.getType () == Type.DELETE) {
				if (i+1<getDiffBlocks().size()) {
					MarkAwareDiffBlock nextBlock = getDiffBlocks().get(i+1);
					if (nextBlock.getType() == Type.INSERT) {
						LinkedHashMap<Integer, LineDiff> lineChanges = 
								DiffUtils.align(block.getLines(), nextBlock.getLines(), false);
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
								
								appendModification(builder, block, nextBlock, deleteLineIndex, insertLineIndex, 
										lineChange.getTokenDiffs()); 
								
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
			PageParameters params = CommitDetailPage.paramsOf(projectModel.getObject(), state);
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
			appendTokenized(builder, block.getLines().get(lineIndex));
			builder.append("</td>");
		} else {
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, -1);
			}
			builder.append("<td class='number noselect'>").append(oldLineNo+1).append("</td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content left' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			appendTokenized(builder, block.getLines().get(lineIndex));
			builder.append("</td>");
			if (blameInfo != null) {
				appendBlame(builder, -1, newLineNo);
			}
			builder.append("<td class='number noselect'>").append(newLineNo+1).append("</td>");
			builder.append("<td class='operation noselect'>&nbsp;</td>");
			builder.append("<td class='content right' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
			appendTokenized(builder, block.getLines().get(lineIndex));
			builder.append("</td>");
		}
		builder.append("</tr>");
	}
	
	private void appendInsert(StringBuilder builder, MarkAwareDiffBlock block, int lineIndex, 
			@Nullable List<DiffBlock<TextToken>> tokenDiffs) {
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
					for (DiffBlock<TextToken> tokenBlock: tokenDiffs) { 
						for (TextToken token: tokenBlock.getUnits()) {
							if (tokenBlock.getOperation() != Operation.DELETE) 
								builder.append(TokenUtils.toHtml(token, getOperationClass(tokenBlock.getOperation()), null));
						}
					}
				}			
			} else {
				appendTokenized(builder, block.getLines().get(lineIndex));
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
			appendTokenized(builder, block.getLines().get(lineIndex));
			builder.append("</td>");
		}
		builder.append("</tr>");
	}
	
	private void appendDelete(StringBuilder builder, MarkAwareDiffBlock block, int lineIndex, 
			@Nullable List<DiffBlock<TextToken>> tokenDiffs) {
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
					for (DiffBlock<TextToken> tokenBlock: tokenDiffs) { 
						for (TextToken token: tokenBlock.getUnits()) {
							if (tokenBlock.getOperation() != Operation.INSERT) 
								builder.append(TokenUtils.toHtml(token, getOperationClass(tokenBlock.getOperation()), null));
						}
					}
				}
			} else {
				appendTokenized(builder, block.getLines().get(lineIndex));
			}
			builder.append("</td>");
		} else {
			if (blameInfo != null) {
				appendBlame(builder, oldLineNo, -1);
			}
			builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("</td>");
			builder.append("<td class='operation noselect old'>-</td>");
			builder.append("<td class='content left old' data-old='").append(oldLineNo).append("'>");
			appendTokenized(builder, block.getLines().get(lineIndex));
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
		appendTokenized(builder, deleteBlock.getLines().get(deleteLineIndex));
		builder.append("</td>");
		
		int newLineNo = insertBlock.getNewStart()+insertLineIndex;
		if (blameInfo != null) {
			appendBlame(builder, -1, newLineNo);
		}
		builder.append("<td class='number noselect new'>").append(newLineNo+1).append("</td>");
		builder.append("<td class='operation noselect new'>+</td>");
		builder.append("<td class='content right new' data-new='").append(newLineNo).append("'>");
		appendTokenized(builder, insertBlock.getLines().get(insertLineIndex));
		builder.append("</td>");
		
		builder.append("</tr>");
	}

	private void appendModification(StringBuilder builder, MarkAwareDiffBlock deleteBlock, 
			MarkAwareDiffBlock insertBlock, int deleteLineIndex, int insertLineIndex, 
			List<DiffBlock<TextToken>> tokenDiffs) {
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
			builder.append("<td class='number noselect old'>").append(oldLineNo+1).append("</td>");
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
			builder.append("<td class='number noselect new'>").append(newLineNo+1).append("</td>");
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
	
	private List<MarkAwareDiffBlock> getDiffBlocks() {
		if (diffBlocks == null) {
			diffBlocks = new ArrayList<>();
			List<LinearRange> oldRanges = new ArrayList<>();
			List<LinearRange> newRanges = new ArrayList<>();
			if (commentSupport != null) {
				String oldCommitHash = getOldCommit().name();
				for (Mark each: initialMarks) {
					LinearRange range = new LinearRange(each.getRange().fromRow, each.getRange().toRow+1);
					if (each.getCommitHash().equals(oldCommitHash)) {
						oldRanges.add(range);
					} else {
						newRanges.add(range);
					}
				}
			}
			
			for (DiffBlock<Tokenized> diffBlock: change.getDiffBlocks()) {
				if (diffBlock.getOperation() == Operation.DELETE) {
					diffBlocks.add(new MarkAwareDiffBlock(Type.DELETE, diffBlock.getUnits(), 
							diffBlock.getOldStart(), diffBlock.getNewStart()));
				} else if (diffBlock.getOperation() == Operation.INSERT) {
					diffBlocks.add(new MarkAwareDiffBlock(Type.INSERT, diffBlock.getUnits(), 
							diffBlock.getOldStart(), diffBlock.getNewStart()));
				} else {
					List<LinearRange> ranges = new ArrayList<>();
					for (LinearRange range: oldRanges) {
						ranges.add(new LinearRange(range.getFrom()-diffBlock.getOldStart(), range.getTo()-diffBlock.getOldStart()));
					}
					for (LinearRange range: newRanges) {
						ranges.add(new LinearRange(range.getFrom()-diffBlock.getNewStart(), range.getTo()-diffBlock.getNewStart()));
					}
					ranges = RangeUtils.merge(ranges);
					
					int lastIndex = 0;
					for (LinearRange range: ranges) {
						int from = range.getFrom();
						int to = range.getTo();
						if (from < diffBlock.getUnits().size() && to > 0) {
							if (from < lastIndex)
								from = lastIndex;
							if (to > diffBlock.getUnits().size())
								to = diffBlock.getUnits().size();
							if (lastIndex < from) {
								List<Tokenized> lines = diffBlock.getUnits().subList(lastIndex, from);
								diffBlocks.add(new MarkAwareDiffBlock(Type.EQUAL, lines, 
										diffBlock.getOldStart()+lastIndex, diffBlock.getNewStart()+lastIndex));
							}
							if (from < to) {
								List<Tokenized> lines = diffBlock.getUnits().subList(from, to);
								diffBlocks.add(new MarkAwareDiffBlock(Type.MARKED_EQUAL, lines, 
										diffBlock.getOldStart()+from, diffBlock.getNewStart()+from));
							}
							lastIndex = to;
						}
					}
					
					if (lastIndex < diffBlock.getUnits().size()) {
						List<Tokenized> lines = diffBlock.getUnits().subList(lastIndex, diffBlock.getUnits().size());
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
		projectModel.detach();
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
			jsonOfCommentInfo = OneDev.getInstance(ObjectMapper.class).writeValueAsString(commentInfo);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return jsonOfCommentInfo;
	}
	
	@Override
	public void onCommentDeleted(AjaxRequestTarget target, CodeComment comment) {
		String script = String.format("onedev.server.textDiff.onCommentDeleted($('#%s'), %s);", 
				getMarkupId(), getJsonOfComment(comment));
		target.appendJavaScript(script);
		unmark(target);
	}

	@Override
	public void onCommentClosed(AjaxRequestTarget target, CodeComment comment) {
		String script = String.format("onedev.server.textDiff.onCloseComment($('#%s'));", getMarkupId());
		target.appendJavaScript(script);
		unmark(target);
	}

	@Override
	public void onCommentAdded(AjaxRequestTarget target, CodeComment comment) {
		String script = String.format("onedev.server.textDiff.onCommentAdded($('#%s'), %s);", 
				getMarkupId(), getJsonOfComment(comment));
		target.appendJavaScript(script);
	}

	@Override
	public void mark(AjaxRequestTarget target, Mark mark) {
		String script = String.format(""
			+ "var $container = $('#%s');"
			+ "var mark = %s;"
			+ "onedev.server.textDiff.scrollTo($container, mark);"
			+ "onedev.server.textDiff.mark($container, mark);", 
			getMarkupId(), getJson(mark));
		target.appendJavaScript(script);
	}
	
	@Override
	public void unmark(AjaxRequestTarget target) {
		String script = String.format(""
			+ "var $container = $('#%s');"
			+ "onedev.server.textDiff.clearMark($container);"
			+ "$container.removeData('mark');", 
			getMarkupId());
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
	private static class MarkInfo extends PlanarRange {
		
		String path;
		
		boolean leftSide;

		public MarkInfo() {
			
		}
		
		public MarkInfo(CodeComment comment, String oldCommitHash) {
			super(comment.getMark().getRange());
			path = comment.getMark().getPath();
			leftSide = comment.getMark().getCommitHash().equals(oldCommitHash);
		}
		
		public MarkInfo(Mark mark, String oldCommitHash) {
			super(mark.getRange());
			path = mark.getPath();
			leftSide = mark.getCommitHash().equals(oldCommitHash);
		}
		
	}
	
	private static class BlameInfo implements Serializable {
		
		Map<Integer, BlameCommit> oldBlame = new HashMap<>();
		
		Map<Integer, BlameCommit> newBlame = new HashMap<>();
		
		String lastCommitHash;
		
		String lastOldCommitHash;
		
		String lastNewCommitHash;
		
	}

}
