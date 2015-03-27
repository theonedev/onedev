package com.pmease.gitplex.search.query.regex;

import static com.pmease.gitplex.search.IndexConstants.NGRAM_SIZE;

import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import com.google.common.base.Strings;
import com.pmease.gitplex.search.query.NGramLuceneQuery;

public class RegexLiterals {
	
	private final List<List<LeafLiterals>> rows;
	
	public RegexLiterals(String regex) {
		ANTLRInputStream stream = new ANTLRInputStream(regex);
		PCRELexer lexer = new PCRELexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		OrLiterals orLiterals = (OrLiterals) new LiteralVisitor().visit(new PCREParser(tokens).parse());
		rows = orLiterals.flattern(true);
	}
	
	@Nullable
	public Query asLuceneQuery(String fieldName) {
		BooleanQuery orQuery = new BooleanQuery();
		for (List<LeafLiterals> row: rows) {
			BooleanQuery andQuery = new BooleanQuery();
			for (LeafLiterals literals: row) {
				if (literals.getLiteral() != null && literals.getLiteral().length()>=NGRAM_SIZE)
					andQuery.add(new NGramLuceneQuery(fieldName, literals.getLiteral()), Occur.MUST);
			}
			if (andQuery.getClauses().length != 0)
				orQuery.add(andQuery, Occur.SHOULD);
		}
		if (orQuery.getClauses().length != 0)
			return orQuery;
		else
			return null;
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

}
