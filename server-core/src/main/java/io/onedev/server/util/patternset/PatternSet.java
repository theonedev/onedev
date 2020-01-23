package io.onedev.server.util.patternset;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.tools.ant.DirectoryScanner;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.patternset.PatternSetParser.PatternContext;
import io.onedev.server.util.patternset.PatternSetParser.PatternsContext;

public class PatternSet implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Set<String> includes;
	
	private final Set<String> excludes;
	
	public PatternSet(Set<String> includes, Set<String> excludes) {
		this.includes = includes;
		this.excludes = excludes;
	}

	public Set<String> getIncludes() {
		return includes;
	}

	public Set<String> getExcludes() {
		return excludes;
	}

	public boolean matches(Matcher matcher, String value) {
		for (String exclude: excludes) {
			if (matcher.matches(exclude, value))
				return false;
		}
		for (String include: includes) {
			if (matcher.matches(include, value))
				return true;
		}
		if (excludes.isEmpty()) 
			return false;
		else 
			return includes.isEmpty();
	}
	
	public Collection<File> listFiles(File dir) {
    	Collection<File> files = new ArrayList<File>();
    	
    	DirectoryScanner scanner = new DirectoryScanner();
    	scanner.setBasedir(dir);
    	scanner.setIncludes(includes.toArray(new String[0]));
    	scanner.setExcludes(excludes.toArray(new String[0]));
    	scanner.scan();
    	
    	for (String path: scanner.getIncludedFiles()) 
    		files.add(new File(dir, path));
    	
		return files;
	}
	
	public static PatternSet parse(@Nullable String patternSetString) {
		Set<String> includes = new HashSet<>();
		Set<String> excludes = new HashSet<>();
		
		if (patternSetString != null) {
			CharStream is = CharStreams.fromString(patternSetString); 
			PatternSetLexer lexer = new PatternSetLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed pattern set");
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			PatternSetParser parser = new PatternSetParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			
			PatternsContext patternsContext = parser.patterns();
			
			for (PatternContext pattern: patternsContext.pattern()) {
				String value;
				if (pattern.Quoted() != null) 
					value = FenceAware.unfence(pattern.Quoted().getText());
				else 
					value = pattern.NQuoted().getText();
				value = StringUtils.unescape(value);
				if (pattern.Excluded() != null)
					excludes.add(value);
				else 
					includes.add(value);
			}			
		}
		
		return new PatternSet(includes, excludes);
	}

	public static String quoteIfNecessary(String pattern) {
		pattern = StringUtils.escape(pattern, "\"");
		if (pattern.indexOf(" ") != -1 || pattern.startsWith("-"))
			pattern = "\"" + pattern + "\"";
		return pattern;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (String include: getIncludes())
			builder.append(include).append(" ");
		for (String exclude: getExcludes())
			builder.append("-").append(exclude).append(" ");
		return builder.toString().trim();
	}
	
}