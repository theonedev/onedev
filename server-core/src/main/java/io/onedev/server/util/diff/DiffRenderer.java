package io.onedev.server.util.diff;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.unbescape.html.HtmlEscape;

import io.onedev.server.util.diff.DiffMatchPatch.Operation;

public class DiffRenderer {

	private final List<DiffBlock<String>> diffBlocks;

	public DiffRenderer(List<DiffBlock<String>> diffBlocks) {
		this.diffBlocks = diffBlocks;
	}
	
	public String renderDiffs() {
		StringBuilder builder = new StringBuilder();
		int operationColumnWidth = 15;
		
		builder.append("<table class='text-diff'><colgroup>");
		builder.append(String.format("<col width='%d'></col><col></col></colgroup>", operationColumnWidth));
		for (int i=0; i<diffBlocks.size(); i++) {
			DiffBlock<String> block = diffBlocks.get(i);
			if (block.getOperation() == Operation.EQUAL) {
				for (int j=0; j<block.getElements().size(); j++) 
					appendEqual(builder, block, j);
			} else if (block.getOperation() == Operation.DELETE) {
				if (i+1<diffBlocks.size()) {
					DiffBlock<String> nextBlock = diffBlocks.get(i+1);
					if (nextBlock.getOperation() == Operation.INSERT) {
						LinkedHashMap<Integer, LineDiff> lineChanges = 
								DiffUtils.align(block.getElements(), nextBlock.getElements());
						int prevDeleteLineIndex = 0;
						int prevInsertLineIndex = 0;
						for (Map.Entry<Integer, LineDiff> entry: lineChanges.entrySet()) {
							int deleteLineIndex = entry.getKey();
							LineDiff lineChange = entry.getValue();
							int insertLineIndex = lineChange.getCompareLine();
							
							appendDeletesAndInserts(builder, block, nextBlock, prevDeleteLineIndex, deleteLineIndex, 
									prevInsertLineIndex, insertLineIndex);
							
							appendModification(builder, block, nextBlock, deleteLineIndex, insertLineIndex, 
									lineChange.getDiffBlocks()); 
							
							prevDeleteLineIndex = deleteLineIndex+1;
							prevInsertLineIndex = insertLineIndex+1;
						}
						appendDeletesAndInserts(builder, block, nextBlock, 
								prevDeleteLineIndex, block.getElements().size(), 
								prevInsertLineIndex, nextBlock.getElements().size());
						i++;
					} else {
						for (int j=0; j<block.getElements().size(); j++) 
							appendDelete(builder, block, j, null);
					}
				} else {
					for (int j=0; j<block.getElements().size(); j++) 
						appendDelete(builder, block, j, null);
				}
			} else {
				for (int j=0; j<block.getElements().size(); j++) 
					appendInsert(builder, block, j, null);
			}
		}
		builder.append("</table>");
		return builder.toString();
	}

	private void appendDeletesAndInserts(StringBuilder builder, DiffBlock<String> deleteBlock, 
		DiffBlock<String> insertBlock, int fromDeleteLineIndex, int toDeleteLineIndex, 
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
	
	private void appendLine(StringBuilder builder, String line) {
		if (line.length() == 0) 
			builder.append("&nbsp;");
		else 
			builder.append(toHtml(line, null));
	}
	
	private void appendEqual(StringBuilder builder, DiffBlock<String> block, int lineIndex) {
		builder.append("<tr class='code original'>");
		int oldLineNo = block.getOldStart() + lineIndex;
		int newLineNo = block.getNewStart() + lineIndex;
		
		builder.append("<td class='operation'>&nbsp;</td>");
		builder.append("<td class='content equal' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
		appendLine(builder, block.getElements().get(lineIndex));
		builder.append("</td>");
		builder.append("</tr>");
	}
	
	private void appendInsert(StringBuilder builder, DiffBlock<String> block, int lineIndex, 
			@Nullable List<DiffBlock<String>> tokenDiffs) {
		builder.append("<tr class='code original'>");

		int newLineNo = block.getNewStart() + lineIndex;
		builder.append("<td class='operation new'>+</td>");
		builder.append("<td class='content new' data-new='").append(newLineNo).append("'>");
		if (tokenDiffs != null) {
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
		} else {
			appendLine(builder, block.getElements().get(lineIndex));
		}
		builder.append("</td>");
	}
	
	public static String toHtml(String text, @Nullable String cssClasses) {
		String escapedText;
		if (text.equals("\r")) {
			escapedText = " ";
		} else {
			escapedText = "";
			for (int i=0; i<text.length(); i++) {
				char ch = text.charAt(i);
				if (ch == ' ' || ch == '\t' || !Character.isWhitespace(ch))
					escapedText += ch;
			}
			escapedText = HtmlEscape.escapeHtml5(escapedText);
		}

		if (cssClasses != null) {
			StringBuilder htmlBuilder = new StringBuilder("<span ");
			htmlBuilder.append("class='").append(cssClasses).append("'");
			htmlBuilder.append(">").append(escapedText).append("</span>");
			return htmlBuilder.toString();
		} else {
			return escapedText;
		}
	}
	
	private void appendDelete(StringBuilder builder, DiffBlock<String> block, int lineIndex, 
			@Nullable List<DiffBlock<String>> tokenDiffs) {
		builder.append("<tr class='code original'>");
		
		int oldLineNo = block.getOldStart() + lineIndex;
		builder.append("<td class='operation old'>-</td>");
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
		builder.append("</td>");
		builder.append("</tr>");
	}
	
	private void appendModification(StringBuilder builder, DiffBlock<String> deleteBlock, 
			DiffBlock<String> insertBlock, int deleteLineIndex, int insertLineIndex, 
			List<DiffBlock<String>> tokenDiffs) {
		builder.append("<tr class='code original'>");

		int oldLineNo = deleteBlock.getOldStart() + deleteLineIndex;
		int newLineNo = insertBlock.getNewStart() + insertLineIndex;
		builder.append("<td class='operation old new'>*</td>");
		builder.append("<td class='content old new' data-old='").append(oldLineNo).append("' data-new='").append(newLineNo).append("'>");
		if (tokenDiffs.isEmpty()) {
			builder.append("&nbsp;");
		} else {
			for (DiffBlock<String> tokenBlock: tokenDiffs) { 
				for (String token: tokenBlock.getElements()) 
					builder.append(toHtml(token, getOperationClass(tokenBlock.getOperation())));
			}
		}
		builder.append("</td>");
		builder.append("</tr>");
	}	
		
}
