package io.onedev.server.web.component.diff.plain;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.jsyntax.TextToken;
import io.onedev.jsyntax.TokenUtils;
import io.onedev.jsyntax.Tokenized;
import io.onedev.server.util.diff.DiffBlock;
import io.onedev.server.util.diff.DiffMatchPatch.Operation;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.util.diff.LineDiff;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public class PlainDiffPanel extends Panel {

	private final Map<Integer, Integer> contextSizes = new HashMap<>();
	
	private AbstractPostAjaxBehavior callbackBehavior;
	
	private List<DiffBlock<Tokenized>> diffBlocks;
	
	public PlainDiffPanel(String id, List<String> oldLines, List<String> newLines) {
		super(id);
		diffBlocks = DiffUtils.diff(oldLines, null, newLines, null, WhitespaceOption.IGNORE_CHANGE);
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
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				int index = params.getParameterValue("index").toInt();
				Integer lastContextSize = contextSizes.get(index);
				if (lastContextSize == null)
					lastContextSize = WebConstants.DIFF_CONTEXT_SIZE;
				int contextSize = lastContextSize + WebConstants.DIFF_EXPAND_SIZE;
				contextSizes.put(index, contextSize);
				
				StringBuilder builder = new StringBuilder();
				appendEquals(builder, index, lastContextSize, contextSize);
				
				String expanded = StringUtils.replace(builder.toString(), "\"", "\\\"");
				expanded = StringUtils.replace(expanded, "\n", "");
				String script = String.format("onedev.server.plainDiff.expand('%s', %d, \"%s\");",
						getMarkupId(), index, expanded);
				target.appendJavaScript(script);
			}

		});
	}
	
	private void appendEquals(StringBuilder builder, int index, int lastContextSize, int contextSize) {
		DiffBlock<Tokenized> block = diffBlocks.get(index);
		if (index == 0) {
			int start = block.getUnits().size()-contextSize;
			if (start < 0)
				start=0;
			else if (start > 0)
				appendExpander(builder, index, start);
			for (int j=start; j<block.getUnits().size()-lastContextSize; j++) 
				appendEqual(builder, block, j, lastContextSize);
		} else if (index == diffBlocks.size()-1) {
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
		
		response.render(JavaScriptHeaderItem.forReference(new PlainDiffResourceReference()));
		
		CharSequence callback = callbackBehavior.getCallbackFunction(explicit("index")); 
		String script = String.format("onedev.server.plainDiff.onDomReady('%s', %s);", getMarkupId(), callback);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	private String renderDiffs() {
		StringBuilder builder = new StringBuilder();
		builder.append(""
				+ "<colgroup>"
				+ "<col width='40'></col>"
				+ "<col width='40'></col>"
				+ "<col width='15'></col>"
				+ "<col></col>"
				+ "</colgroup>");
		for (int i=0; i<diffBlocks.size(); i++) {
			DiffBlock<Tokenized> block = diffBlocks.get(i);
			if (block.getOperation() == Operation.EQUAL) {
				Integer lastContextSize = contextSizes.get(i);
				if (lastContextSize == null)
					lastContextSize = WebConstants.DIFF_CONTEXT_SIZE;
				appendEquals(builder, i, 0, lastContextSize);
			} else if (block.getOperation() == Operation.DELETE) {
				if (i+1<diffBlocks.size()) {
					DiffBlock<Tokenized> nextBlock = diffBlocks.get(i+1);
					if (nextBlock.getOperation() == Operation.INSERT) {
						LinkedHashMap<Integer, LineDiff> lineChanges = 
								DiffUtils.align(block.getUnits(), nextBlock.getUnits());
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
		for (int i=fromDeleteLineIndex; i<toDeleteLineIndex; i++)
			appendDelete(builder, deleteBlock, i, null);
		for (int i=fromInsertLineIndex; i<toInsertLineIndex; i++)
			appendInsert(builder, insertBlock, i, null);
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
				builder.append(TokenUtils.toHtml(tokenized.getText(), token, null));
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
		
		builder.append("<td class='number noselect'>").append(oldLineNo+1).append("</td>");
		builder.append("<td class='number noselect'>").append(newLineNo+1).append("</td>");
		builder.append("<td class='operation noselect'>&nbsp;</td>");
		builder.append("<td class='content' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
		appendTokenized(builder, block.getUnits().get(lineIndex));
		builder.append("</td>");
		builder.append("</tr>");
	}
	
	private void appendInsert(StringBuilder builder, DiffBlock<Tokenized> block, int lineIndex, 
			@Nullable List<DiffBlock<TextToken>> tokenDiffs) {
		builder.append("<tr class='code original'>");

		int newLineNo = block.getNewStart() + lineIndex;
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
							builder.append(TokenUtils.toHtml(token, getOperationClass(tokenBlock.getOperation())));
					}
				}
			}			
		} else {
			appendTokenized(builder, block.getUnits().get(lineIndex));
		}
		builder.append("</td>");
		builder.append("</tr>");
	}
	
	private void appendDelete(StringBuilder builder, DiffBlock<Tokenized> block, int lineIndex, 
			@Nullable List<DiffBlock<TextToken>> tokenDiffs) {
		builder.append("<tr class='code original'>");
		
		int oldLineNo = block.getOldStart() + lineIndex;
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
							builder.append(TokenUtils.toHtml(token, getOperationClass(tokenBlock.getOperation())));
					}
				}
			}
		} else {
			appendTokenized(builder, block.getUnits().get(lineIndex));
		}
		builder.append("</td>");
		builder.append("</tr>");
	}
	
	private void appendModification(StringBuilder builder, DiffBlock<Tokenized> deleteBlock, 
			DiffBlock<Tokenized> insertBlock, int deleteLineIndex, int insertLineIndex, 
			List<DiffBlock<TextToken>> tokenDiffs) {
		builder.append("<tr class='code original'>");

		int oldLineNo = deleteBlock.getOldStart() + deleteLineIndex;
		int newLineNo = insertBlock.getNewStart() + insertLineIndex;
		builder.append("<td class='number noselect old new'>").append(oldLineNo+1).append("</td>");
		builder.append("<td class='number noselect old new'>").append(newLineNo+1).append("</td>");
		builder.append("<td class='operation noselect old new'>*</td>");
		builder.append("<td class='content old new' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
		if (tokenDiffs.isEmpty()) {
			builder.append("&nbsp;");
		} else {
			for (DiffBlock<TextToken> tokenBlock: tokenDiffs) { 
				for (TextToken token: tokenBlock.getUnits()) 
					builder.append(TokenUtils.toHtml(token, getOperationClass(tokenBlock.getOperation())));
			}
		}
		builder.append("</td>");
		builder.append("</tr>");
	}
	
	private void appendExpander(StringBuilder builder, int blockIndex, int skippedLines) {
		builder.append("<tr class='expander expander").append(blockIndex).append("'>");
		
		String script = String.format("javascript:$('#%s').data('callback')(%d);", getMarkupId(), blockIndex);
		builder.append("<td colspan='2' class='expander noselect'><a title='Show more lines' href=\"")
				.append(script).append("\"><i class='fa fa-sort'></i></a></td>");
		builder.append("<td colspan='2' class='skipped noselect'><i class='fa fa-ellipsis-h'></i> skipped ")
				.append(skippedLines).append(" lines <i class='fa fa-ellipsis-h'></i></td>");
		builder.append("</tr>");
	}
	
}
