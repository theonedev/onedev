package com.gitplex.search.query.regex;

import static com.gitplex.search.IndexConstants.NGRAM_SIZE;

import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.search.query.NGramLuceneQuery;
import com.gitplex.search.query.TooGeneralQueryException;
import com.google.common.base.Strings;
import com.gitplex.search.query.regex.PCRELexer;
import com.gitplex.search.query.regex.PCREParser;

public class RegexLiterals {
	
	private static final Logger logger = LoggerFactory.getLogger(RegexLiterals.class);
	
	private final List<List<LeafLiterals>> rows;
	
	public RegexLiterals(String regex) {
		ANTLRInputStream stream = new ANTLRInputStream(regex);
		PCRELexer lexer = new PCRELexer(stream);
		lexer.removeErrorListeners();
		lexer.addErrorListener(ErrorListener.INSTANCE);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		PCREParser parser = new PCREParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(ErrorListener.INSTANCE);
		
		OrLiterals orLiterals = (OrLiterals) new LiteralVisitor().visit(parser.parse());
		rows = orLiterals.flattern(true);
	}

	/**
	 * @param fieldName
	 * @param gramSize
	 * @return
	 * @throws TooGeneralQueryException
	 */
	public Query asNGramQuery(String fieldName, int gramSize) throws TooGeneralQueryException {
		BooleanQuery orQuery = new BooleanQuery();
		for (List<LeafLiterals> row: rows) {
			BooleanQuery andQuery = new BooleanQuery();
			for (LeafLiterals literals: row) {
				if (literals.getLiteral() != null && literals.getLiteral().length()>=NGRAM_SIZE)
					andQuery.add(new NGramLuceneQuery(fieldName, literals.getLiteral(), gramSize), Occur.MUST);
			}
			if (andQuery.getClauses().length != 0)
				orQuery.add(andQuery, Occur.SHOULD);
		}
		if (orQuery.getClauses().length != 0)
			return orQuery;
		else
			throw new TooGeneralQueryException();
	}

	@Override
	public String toString() {
		StringBuilder orBuilder = new StringBuilder();
		for (List<LeafLiterals> row: rows) {
			StringBuilder andBuilder = new StringBuilder(); 
			for (LeafLiterals literals: row) {
				if (!Strings.isNullOrEmpty(literals.getLiteral())) {
					if (andBuilder.length() != 0)
						andBuilder.append("&");
					andBuilder.append(literals.getLiteral());
				}
			}
			if (orBuilder.length() != 0)
				orBuilder.append("|");
			orBuilder.append(andBuilder);
		}
		return orBuilder.toString();
	}

	private static class ErrorListener extends BaseErrorListener {

		private static final ErrorListener INSTANCE = new ErrorListener();
		
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer,
								Object offendingSymbol,
								int line,
								int charPositionInLine,
								String msg,
								RecognitionException e) {
			logger.error("line " + line + ":" + charPositionInLine + " " + msg);
		}
		
	}
}
