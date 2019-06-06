package io.onedev.server.search.code.query.regex;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class LeafLiterals implements Literals {

	private final String literal;
	
	/**
	 * Construct a leaf literal.
	 * 
	 * @param literal
	 * 			string literal that must occur, or empty string to indicate an empty occurrence, 
	 * 			or <tt>null</tt> to indicate an opaque occurrence 
	 */
	public LeafLiterals(@Nullable String literal) {
		if (literal != null && literal.length()>1) {
			if (literal.equals("\\a")) {
				this.literal = "\u0007";
			} else if (literal.equals("\\n")) {
				this.literal = "\n";
			} else if (literal.equals("\\r")) {
				this.literal = "\r";
			} else if (literal.equals("\\t")) {
				this.literal = "\t";
			} else if (literal.equals("\\f")) {
				this.literal = "\u000C";
			} else if (literal.equals("\\e")) {
				this.literal = "\u001B";
			} else if (literal.length() == 2 && literal.charAt(0) == '\\') {
				this.literal = literal.substring(1);
			} else {
				this.literal = literal;
			}
		} else {
			this.literal = literal;
		}
	}
	
	@Nullable
	public String getLiteral() {
		return literal;
	}

	@Override
	public List<List<LeafLiterals>> flattern(boolean outmost) {
		List<List<LeafLiterals>> rows = new ArrayList<>();
		rows.add(new ArrayList<LeafLiterals>());
		rows.get(0).add(this);
		return rows;
	}

}
