package io.onedev.server.util.patternset;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import org.junit.Test;

import com.google.common.collect.Sets;

import io.onedev.server.util.interpolative.VariableInterpolator;

public class InterpolativePatternSetGrammarTest {

	@Test
	public void test() {
		Function<String, String> variableResolver = t -> t;
		
		PatternSet expected, actual;
		String interpolated;
		
		expected = new PatternSet(Sets.newHashSet("\"hello\""), Sets.newHashSet("\"hello world\"")); 
		interpolated = new VariableInterpolator(variableResolver).interpolate("\\\"hello\\\" -@\"\\\"hello world\\\"\"@");
		actual = PatternSet.parse(interpolated);
		assertEquals(expected.getIncludes(), actual.getIncludes());
		assertEquals(expected.getExcludes(), actual.getExcludes());
		
		expected = new PatternSet(Sets.newHashSet("hello world"), Sets.newHashSet()); 
		interpolated = new VariableInterpolator(variableResolver).interpolate("@\"@hello world\"");
		actual = PatternSet.parse(interpolated);
		assertEquals(expected.getIncludes(), actual.getIncludes());
		assertEquals(expected.getExcludes(), actual.getExcludes());
		
		expected = new PatternSet(Sets.newHashSet("@robin", "-@alive"), Sets.newHashSet("\"@")); 
		interpolated = new VariableInterpolator(variableResolver).interpolate("@@robin \"-@@alive\" -\\\"@@");
		actual = PatternSet.parse(interpolated);
		assertEquals(expected.getIncludes(), actual.getIncludes());
		assertEquals(expected.getExcludes(), actual.getExcludes());
		
		expected = new PatternSet(Sets.newHashSet("@robin", "hello world", "world"), Sets.newHashSet("hello")); 
		interpolated = new VariableInterpolator(variableResolver).interpolate("@@robin @\"hello world\"@ -@hello world@");
		actual = PatternSet.parse(interpolated);
		assertEquals(expected.getIncludes(), actual.getIncludes());
		assertEquals(expected.getExcludes(), actual.getExcludes());
	}

}
