package io.onedev.server.markdown;

import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.formatter.NodeFormatter;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import com.vladsch.flexmark.parser.Parser;

public class MarkdownFormatter {

	/** Rewrites Markdown with the output syntax specified by the node handlers. */
	public static String format(String markdown, Set<NodeFormattingHandler<?>> handlers) {
		var formatter = Formatter.builder().nodeFormatterFactory(options -> new NodeFormatter() {

			@Override
			public Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
				return handlers;
			}

			@Override
			public @Nullable Set<Class<?>> getNodeClasses() {
				return null;
			}

		}).build();
		return formatter.render(Parser.builder().build().parse(markdown));
	}

}
