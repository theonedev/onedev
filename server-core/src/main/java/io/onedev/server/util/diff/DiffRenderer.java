package io.onedev.server.util.diff;

import io.onedev.server.util.diff.DiffMatchPatch.Operation;
import org.unbescape.html.HtmlEscape;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;

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
						LinkedHashMap<Integer, List<DiffBlock<String>>> lineDiffs = 
								DiffUtils.diffLines(block.getElements(), nextBlock.getElements());
						for (int j=0; j<block.getElements().size(); j++) { 
							List<DiffBlock<String>> lineDiff = lineDiffs.get(j);
							if (lineDiff != null) 
								appendDelete(builder, block, j, lineDiff);
							else 
								appendDelete(builder, block, j, null);
						}
						for (int j=0; j<nextBlock.getElements().size(); j++) {
							List<DiffBlock<String>> lineDiff = lineDiffs.get(j);
							if (lineDiff != null) 
								appendInsert(builder, nextBlock, j, lineDiff);
							else 
								appendInsert(builder, nextBlock, j, null);
						}
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
	
}
