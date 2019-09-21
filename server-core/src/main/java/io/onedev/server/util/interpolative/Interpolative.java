package io.onedev.server.util.interpolative;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.commons.lang3.StringUtils;

import io.onedev.server.OneException;
import io.onedev.server.util.interpolative.InterpolativeParser.InterpolativeContext;
import io.onedev.server.util.interpolative.InterpolativeParser.SegmentContext;
import io.onedev.server.util.interpolative.Segment.Type;

public class Interpolative implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final char MARK = '@';
	
	private final List<Segment> segments;
	
	public Interpolative(List<Segment> segments) {
		this.segments = segments;
	}
	
	public static Interpolative fromString(String interpolativeString) {
		List<Segment> segments = new ArrayList<>();
		for (SegmentContext segment: parse(interpolativeString).segment()) {
			if (segment.Literal() != null) 
				segments.add(new Segment(Type.LITERAL, unescape(segment.Literal().getText())));
			else 
				segments.add(new Segment(Type.VARIABLE, unescape(segment.Variable().getText())));
		}
		return new Interpolative(segments);
	}

	public List<Segment> getSegments() {
		return segments;
	}
	
	public String interpolateWith(Function<String, String> interpolator) {
		StringBuilder builder = new StringBuilder();
		for (Segment segment: segments) {
			if (segment.getType() == Type.LITERAL)
				builder.append(segment.getContent());
			else
				builder.append(interpolator.apply(segment.getContent()));
		}
		return builder.toString();
	}
	
	public static String unescape(String literal) {
		String markString = String.valueOf(MARK);
		if (literal.startsWith(markString))
			literal = literal.substring(1, literal.length()-1);
		return StringUtils.replace(literal, "\\" + markString, markString);
	}

	public static InterpolativeContext parse(String interpolativeString) {
		CharStream is = CharStreams.fromString(interpolativeString); 
		InterpolativeLexer lexer = new InterpolativeLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new OneException("Malformed interpolative");
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		InterpolativeParser parser = new InterpolativeParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.interpolative();
	}

	public static String escape(String literal) {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<literal.length(); i++) {
			char ch = literal.charAt(i);
			if (ch == MARK)
				builder.append("\\");
			builder.append(ch);
		}
		return builder.toString();
	}

}