package io.onedev.server.web.component.diff.blob.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.jsymbol.java.JavaExtractor;
import io.onedev.commons.jsymbol.java.symbols.JavaSymbol;
import io.onedev.commons.utils.PlanarRange;

public class DiffSymbolContextTest {

	@Test
	public void resolvesNestedScopesAndReturnsToParent() {
		var symbols = new JavaExtractor().extract("Example.java", "package demo;\nclass Example {\n"
				+ " void first() {\n  work();\n }\n void second() {\n  work();\n }\n}\n");
		assertEquals(List.of(List.of(1, 2, "Example"), List.of(2, 5, "Example > first"),
				List.of(5, 8, "Example > second"), List.of(8, 9, "Example")),
				DiffSymbolContext.build(new ArrayList<>(symbols), 10));
	}

	@Test
	public void handlesOverlappingScopesAndExclusiveEndLines() {
		var outer = symbol(null, "Outer", 0, 20, 1);
		var first = symbol(outer, "first", 2, 8, 0);
		var overlap = symbol(outer, "overlap", 5, 12, 1);
		assertEquals(List.of(List.of(0, 2, "Outer"), List.of(2, 8, "Outer > first"),
				List.of(8, 13, "Outer > overlap"), List.of(13, 21, "Outer")),
				DiffSymbolContext.build(List.of(overlap, first, outer), 21));
	}

	@Test
	public void skipsMissingAndOversizedSymbolLists() {
		assertTrue(DiffSymbolContext.build(null, 100).isEmpty());
		assertTrue(DiffSymbolContext.build(java.util.Collections.nCopies(DiffSymbolContext.MAX_SYMBOLS + 1,
				symbol(null, "Generated", 0, 99, 1)), 100).isEmpty());
	}

	@Test
	public void boundsDeepHierarchiesAndLabels() {
		JavaSymbol parent = null;
		for (int i = 0; i < 65; i++)
			parent = symbol(parent, "Nested", 0, 100, 1);
		assertTrue(DiffSymbolContext.build(List.of(parent), 101).isEmpty());
		assertTrue(DiffSymbolContext.build(List.of(symbol(null, "x".repeat(513), 0, 1, 1)), 2).isEmpty());
	}

	@Test
	public void mapsSymbolHeavyFile() {
		var symbols = new ArrayList<Symbol>();
		var outer = symbol(null, "Generated", 0, 60000, 1);
		symbols.add(outer);
		for (int i = 0; i < 9000; i++)
			symbols.add(symbol(outer, "m" + i, i * 6 + 1, i * 6 + 3, 1));
		var ranges = DiffSymbolContext.build(symbols, 60001);
		// This payload exceeds the display budget and should be omitted, not partially shown.
		assertTrue(ranges.isEmpty());
	}

	private JavaSymbol symbol(JavaSymbol parent, String name, int from, int to, int column) {
		return new JavaSymbol(parent, name, null, new PlanarRange(from, 0, to, column)) {
			@Override public boolean isLocal() { return false; }
			@Override public boolean isPrimary() { return true; }
			@Override public org.apache.wicket.markup.html.image.Image renderIcon(String id) { return null; }
			@Override public org.apache.wicket.Component render(String id, io.onedev.commons.utils.LinearRange range) { return null; }
		};
	}
}
