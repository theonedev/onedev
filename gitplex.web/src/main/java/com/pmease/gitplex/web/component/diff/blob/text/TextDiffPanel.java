package com.pmease.gitplex.web.component.diff.blob.text;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.git.BlobChange;
import com.pmease.commons.lang.diff.DiffBlock;
import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.tokenizers.CmToken;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.diff.diffstat.DiffStatBar;
import com.pmease.gitplex.web.component.diff.difftitle.BlobDiffTitle;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;

@SuppressWarnings("serial")
public class TextDiffPanel extends Panel {

	public static final int MAX_DISPLAY_SIZE = 5000;
	
	private static final int DEFAULT_CONTEXT_SIZE = 3;
	
	private static final int EXPAND_CONTEXT_SIZE = 15;
	
	private final IModel<Repository> repoModel;
	
	private final BlobChange change;
	
	private final Map<Integer, Integer> contextSizes = new HashMap<>();
	
	private final boolean unified;
	
	private final InlineCommentSupport commentSupport;
	
	public TextDiffPanel(String id, IModel<Repository> repoModel, BlobChange change, boolean unified, 
			@Nullable InlineCommentSupport commentSupport) {
		super(id);
		
		this.repoModel = repoModel;
		this.change = change;
		this.unified = unified;
		this.commentSupport = commentSupport;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		
		if (unified)
			container.add(AttributeAppender.append("class", " unified"));
		else
			container.add(AttributeAppender.append("class", " split"));
		
		container.add(new DiffStatBar("diffStat", change.getAdditions(), change.getDeletions(), true));
		container.add(new BlobDiffTitle("title", change));
		
		PageParameters params = RepoFilePage.paramsOf(repoModel.getObject(), change.getBlobIdent());
		container.add(new BookmarkablePageLink<Void>("viewFile", RepoFilePage.class, params));
		
		container.add(new Label("diffLines", renderDiffs()).setEscapeModelStrings(false));
		
		add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				int index = params.getParameterValue("index").toInt();
				Integer lastContextSize = contextSizes.get(index);
				if (lastContextSize == null)
					lastContextSize = DEFAULT_CONTEXT_SIZE;
				int contextSize = lastContextSize + EXPAND_CONTEXT_SIZE;
				contextSizes.put(index, contextSize);
				
				StringBuilder builder = new StringBuilder();
				appendEquals(builder, index, lastContextSize, contextSize);
				
				String script = String.format("$('#%s expander%d').replaceWith('%s');", 
						getMarkupId(), index, StringUtils.replace(builder.toString(), "'", "\\'"));
				target.appendJavaScript(script);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				String script = String.format("$('#%s')[0].expander = %s;", 
						getMarkupId(), getCallbackFunction(CallbackParameter.explicit("index")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	private void appendEquals(StringBuilder builder, int index, int lastContextSize, int contextSize) {
		DiffBlock block = change.getDiffBlocks().get(index);
		if (index == 0) {
			int start = block.getLines().size()-contextSize;
			if (start < 0)
				start=0;
			else if (start > 0)
				appendExpander(builder, index, start);
			for (int j=start; j<block.getLines().size()-lastContextSize; j++) 
				appendEqual(builder, block, j, lastContextSize);
		} else if (index == change.getDiffBlocks().size()-1) {
			int end = block.getLines().size();
			if (end > contextSize) {
				appendExpander(builder, index, end-contextSize);
				end = contextSize;
			}
			for (int j=lastContextSize; j<end; j++)
				appendEqual(builder, block, j, lastContextSize);
		} else if (2*contextSize < block.getLines().size()) {
			appendExpander(builder, index, block.getLines().size() - 2*contextSize);
			for (int j=lastContextSize; j<contextSize; j++)
				appendEqual(builder, block, j, lastContextSize);
			for (int j=block.getLines().size()-contextSize; j<block.getLines().size()-lastContextSize; j++)
				appendEqual(builder, block, j, lastContextSize);
		} else {
			for (int j=lastContextSize; j<block.getLines().size()-lastContextSize; j++)
				appendEqual(builder, block, j, lastContextSize);
		}
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
	}

	private String renderDiffs() {
		int contextSize = DEFAULT_CONTEXT_SIZE;
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<change.getDiffBlocks().size(); i++) {
			DiffBlock block = change.getDiffBlocks().get(i);
			if (block.getOperation() == Operation.EQUAL) {
				appendEquals(builder, i, 0, contextSize);
			} else if (block.getOperation() == Operation.DELETE) {
				if (unified || i+1 >= change.getDiffBlocks().size() 
						|| change.getDiffBlocks().get(i+1).getOperation() != Operation.INSERT) {
					for (int j=0; j<block.getLines().size(); j++) 
						appendDelete(builder, block, j);
				} else {
					int min = block.getLines().size();
					if (min > change.getDiffBlocks().get(i+1).getLines().size())
						min = change.getDiffBlocks().get(i+1).getLines().size();
					for (int j=0; j<min; j++)
						appendDeleteAndInsert(builder, block, change.getDiffBlocks().get(i+1), j);
					
					if (min == block.getLines().size()) {
						for (int j=min+1; j<change.getDiffBlocks().get(i+1).getLines().size(); j++)
							appendInsert(builder, change.getDiffBlocks().get(i+1), j);
					} else {
						for (int j=min+1; j<block.getLines().size(); j++)
							appendDelete(builder, block, j);
					}
					i++;
				}
			} else {
				for (int j=0; j<block.getLines().size(); j++) 
					appendInsert(builder, block, j);
			}
		}
		return builder.toString();
	}

	private void appendEqual(StringBuilder builder, DiffBlock block, int lineIndex, int lastContextSize) {
		if (lastContextSize != 0)
			builder.append("<tr class='line expanded'>");
		else
			builder.append("<tr class='line'>");

		StringBuilder contentBuilder = new StringBuilder();
		contentBuilder.append("<td class='content old").append(block.getOldStart()+lineIndex)
				.append(" new").append(block.getNewStart()+lineIndex).append("'> ");
		for (CmToken token: block.getLines().get(lineIndex))
			contentBuilder.append(token.toHtml());
		contentBuilder.append("</td>");
		
		if (unified) {
			builder.append("<td class='number'>").append(block.getOldStart() + lineIndex).append("</td>");
			builder.append("<td class='number'>").append(block.getNewStart() + lineIndex).append("</td>");
			builder.append(contentBuilder);
		} else {
			builder.append("<td class='number'>").append(block.getOldStart() + lineIndex).append("</td>");
			builder.append(contentBuilder);
			builder.append("<td class='number'>").append(block.getNewStart() + lineIndex).append("</td>");
			builder.append(contentBuilder);
		}
		builder.append("</tr>");
	}
	
	private void appendInsert(StringBuilder builder, DiffBlock block, int lineIndex) {
		builder.append("<tr class='line'>");
		
		StringBuilder contentBuilder = new StringBuilder();
		contentBuilder.append("<td class='content new new").append(block.getNewStart()+lineIndex).append("'><strong>+</strong>");
		for (CmToken token: block.getLines().get(lineIndex))
			contentBuilder.append(token.toHtml());
		contentBuilder.append("</td>");
		
		if (unified) {
			builder.append("<td class='number new'>&nbsp;</td>");
			builder.append("<td class='number new'>").append(block.getNewStart() + lineIndex).append("</td>");
			builder.append(contentBuilder);
		} else {
			builder.append("<td class='number'>&nbsp;</td><td class='content'>&nbsp;</td>");
			builder.append("<td class='number new'>").append(block.getNewStart() + lineIndex).append("</td>");
			builder.append(contentBuilder);
		}
		builder.append("</tr>");
	}
	
	private void appendDelete(StringBuilder builder, DiffBlock block, int lineIndex) {
		builder.append("<tr class='line'>");
		
		StringBuilder contentBuilder = new StringBuilder();
		contentBuilder.append("<td class='content old old").append(block.getOldStart()+lineIndex).append("'><strong>-</strong>");
		for (CmToken token: block.getLines().get(lineIndex))
			contentBuilder.append(token.toHtml());
		contentBuilder.append("</td>");
		
		if (unified) {
			builder.append("<td class='number old'>").append(block.getOldStart() + lineIndex).append("</td>");
			builder.append("<td class='number old'>&nbsp;</td>");
			builder.append(contentBuilder);
		} else {
			builder.append("<td class='number old'>").append(block.getNewStart() + lineIndex).append("</td>");
			builder.append(contentBuilder);
			builder.append("<td class='number'>&nbsp;</td><td class='content'>&nbsp;</td>");
		}
		builder.append("</tr>");
	}
	
	private void appendDeleteAndInsert(StringBuilder builder, DiffBlock delete, DiffBlock insert, int lineIndex) {
		builder.append("<tr class='line'>");
		
		builder.append("<td class='number old'>").append(delete.getOldStart() + lineIndex).append("</td>");
		builder.append("<td class='content old old").append(delete.getOldStart()+lineIndex).append("'><strong>-</strong>");
		for (CmToken token: delete.getLines().get(lineIndex))
			builder.append(token.toHtml());
		builder.append("</td>");
		
		builder.append("<td class='number new'>").append(insert.getNewStart() + lineIndex).append("</td>");
		builder.append("<td class='content new new").append(insert.getNewStart()+lineIndex).append("'><strong>+</strong>");
		for (CmToken token: insert.getLines().get(lineIndex))
			builder.append(token.toHtml());
		builder.append("</td>");
		
		builder.append("</tr>");
	}
	
	private void appendExpander(StringBuilder builder, int blockIndex, int skippedLines) {
		builder.append("<tr class='expander expander").append(blockIndex).append("'>");
		
		String script = String.format("javascript: $('#%s')[0].expander(%d);", getMarkupId(), blockIndex);
		if (unified) {
			builder.append("<td colspan='2' class='expander'><a class='expander' title='Show more lines' href='")
					.append(script).append("'><i class='fa fa-expand'></i></a></td>");
			builder.append("<td class='skipped'><i class='fa fa-ellipsis-h'></i> skipped ")
					.append(skippedLines).append(" lines <i class='fa fa-ellipsis-h'></i></td>");
		} else {
			builder.append("<td class='expander'><a class='expander' title='Show more lines' href='").append(script)
					.append("'><i class='fa fa-expand'></i></a></td>");
			builder.append("<td class='skipped' colspan='3'><i class='fa fa-ellipsis-h'></i> skipped ")
					.append(skippedLines).append(" lines <i class='fa fa-ellipsis-h'></i></td>");
		}
		builder.append("</tr>");
	}
	
	@Override
	protected void onDetach() {
		repoModel.detach();
		super.onDetach();
	}

}
