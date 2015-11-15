package com.pmease.commons.antlr.grammar;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

import com.google.common.base.Preconditions;
import com.pmease.commons.antlr.ANTLRv4Lexer;
import com.pmease.commons.antlr.ANTLRv4Parser;

public class Grammar implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String PARSER_SUFFIX = "parser";

	private final Map<String, Rule> rules = new HashMap<>();
	
	private final Map<String, Integer> tokenTypesByValue = new HashMap<>();

	private final Map<String, Integer> tokenTypesByName = new HashMap<>();
	
	public Grammar(Class<? extends Parser> parserClass, String entryRule) {
		String parserName = parserClass.getSimpleName();
		Preconditions.checkArgument(parserName.endsWith(PARSER_SUFFIX));
		String grammarName = parserName.substring(0, parserName.length() - PARSER_SUFFIX.length());
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(grammarName + ".tokens")) {
			Properties props = new Properties();
			props.load(is);
			for (Map.Entry<Object, Object> entry: props.entrySet()) {
				String key = (String) entry.getKey();
				Integer value = Integer.valueOf((String) entry.getValue());
				if (key.startsWith("'"))
					tokenTypesByValue.put(key.substring(1, key.length()-1), value);
				else
					tokenTypesByName.put(key, value);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try (InputStream is = parserClass.getResourceAsStream(grammarName + ".g4")) {
			ANTLRv4Lexer lexer = new ANTLRv4Lexer(new ANTLRInputStream(is));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ANTLRv4Parser parser = new ANTLRv4Parser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			ParseTree tree = parser.rules();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Map<String, Rule> getRules() {
		return rules;
	}

}
