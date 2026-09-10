package io.onedev.server.web.component.diff.blob.text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;

import org.jspecify.annotations.Nullable;

import io.onedev.commons.jsymbol.Symbol;

/** Disjoint line ranges, resolved once so browser lookups can use binary search. */
public class DiffSymbolContext {

	static final int MAX_SYMBOLS = 10000;

	static final long MAX_PROCESSING_NANOS = 100_000_000;

	private static class Scope {
		int from;
		int to;
		int depth;
		int order;
		String label;
	}

	/** Returns [first line, exclusive last line, parent > symbol] tuples. */
	public static List<List<Object>> build(@Nullable List<Symbol> symbols, int lineCount) {
		if (symbols == null || symbols.size() > MAX_SYMBOLS)
			return List.of();
		long started = System.nanoTime();
		var starts = new TreeMap<Integer, List<Scope>>();
		var boundaries = new java.util.TreeSet<Integer>();
		int order = 0;
		for (var symbol : symbols) {
			if (System.nanoTime() - started > MAX_PROCESSING_NANOS)
				return List.of();
			var range = symbol.getScope();
			if (range == null || !symbol.isDisplayInOutline() || !symbol.isSearchable())
				continue;
			var scope = new Scope();
			scope.from = Math.max(0, range.getFromRow());
			scope.to = Math.min(lineCount, range.getToRow()
					+ (range.getToColumn() == 0 && range.getToRow() > range.getFromRow() ? 0 : 1));
			if (scope.from >= scope.to)
				continue;
			var names = new ArrayList<String>();
			var parent = symbol;
			while (parent != null && scope.depth < 64) {
				if (parent.isDisplayInOutline() && parent.isSearchable() && parent.getName() != null)
					names.add(parent.getName());
				parent = parent.getParent();
				scope.depth++;
			}
			// Bound malformed/cyclic hierarchies and unusually long generated names.
			if (parent != null || names.isEmpty())
				continue;
			java.util.Collections.reverse(names);
			scope.label = String.join(" > ", names);
			if (scope.label.length() > 512)
				continue;
			scope.order = order++;
			starts.computeIfAbsent(scope.from, it -> new ArrayList<>()).add(scope);
			boundaries.add(scope.from);
			boundaries.add(scope.to);
		}
		var active = new PriorityQueue<>(Comparator.<Scope>comparingInt(it -> it.to - it.from)
				.thenComparingInt(it -> -it.depth).thenComparingInt(it -> it.order));
		var result = new ArrayList<List<Object>>();
		Integer previous = null;
		String label = null;
		int labelCharacters = 0;
		for (int line : boundaries) {
			if (System.nanoTime() - started > MAX_PROCESSING_NANOS)
				return List.of();
			if (previous != null && label != null) {
				var last = result.isEmpty() ? null : result.get(result.size() - 1);
				if (last != null && last.get(1).equals(previous) && last.get(2).equals(label)) {
					last.set(1, line);
				} else {
					labelCharacters += label.length();
					if (labelCharacters > 128 * 1024)
						return List.of();
					result.add(new ArrayList<>(List.of(previous, line, label)));
				}
			}
			var starting = starts.get(line);
			if (starting != null)
				active.addAll(starting);
			while (!active.isEmpty() && active.peek().to <= line)
				active.remove();
			label = active.isEmpty() ? null : active.peek().label;
			previous = line;
		}
		return result;
	}
}
