package io.onedev.server.util.patternset;

import static org.junit.Assert.*;

import java.util.function.Function;

import org.junit.Test;

import com.google.common.collect.Sets;

import io.onedev.server.util.interpolative.Interpolative;

public class InterpolativePatternSetGrammarTest {

	@Test
	public void test() {
		Function<String, String> interpolator = new Function<String, String>() {

			@Override
			public String apply(String t) {
				return t;
			}
			
		};
		
		PatternSet expected, actual;
		String interpolated;
		
		expected = new PatternSet(Sets.newHashSet("\"hello\""), Sets.newHashSet("\"hello world\"")); 
		interpolated = Interpolative.parse("\\\"hello\\\" -@\"\\\"hello world\\\"\"@").interpolateWith(interpolator);
		actual = PatternSet.parse(interpolated);
		assertEquals(expected.getIncludes(), actual.getIncludes());
		assertEquals(expected.getExcludes(), actual.getExcludes());
		
		expected = new PatternSet(Sets.newHashSet("hello world"), Sets.newHashSet()); 
		interpolated = Interpolative.parse("@\"@hello world\"").interpolateWith(interpolator);
		actual = PatternSet.parse(interpolated);
		assertEquals(expected.getIncludes(), actual.getIncludes());
		assertEquals(expected.getExcludes(), actual.getExcludes());
		
		expected = new PatternSet(Sets.newHashSet("@robin", "-@alive"), Sets.newHashSet("\"@")); 
		interpolated = Interpolative.parse("@@robin \"-@@alive\" -\\\"@@").interpolateWith(interpolator);
		actual = PatternSet.parse(interpolated);
		assertEquals(expected.getIncludes(), actual.getIncludes());
		assertEquals(expected.getExcludes(), actual.getExcludes());
		
		expected = new PatternSet(Sets.newHashSet("@robin", "hello world", "world"), Sets.newHashSet("hello")); 
		interpolated = Interpolative.parse("@@robin @\"hello world\"@ -@hello world@").interpolateWith(interpolator);
		actual = PatternSet.parse(interpolated);
		assertEquals(expected.getIncludes(), actual.getIncludes());
		assertEquals(expected.getExcludes(), actual.getExcludes());
	}

}
