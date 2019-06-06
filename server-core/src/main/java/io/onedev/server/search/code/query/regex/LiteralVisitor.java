package io.onedev.server.search.code.query.regex;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.search.code.query.regex.PCREParser.AlternationContext;
import io.onedev.server.search.code.query.regex.PCREParser.AtomContext;
import io.onedev.server.search.code.query.regex.PCREParser.Cc_atomContext;
import io.onedev.server.search.code.query.regex.PCREParser.ElementContext;
import io.onedev.server.search.code.query.regex.PCREParser.ExprContext;
import io.onedev.server.search.code.query.regex.PCREParser.ParseContext;
import io.onedev.server.search.code.query.regex.PCREParser.QuantifierContext;

public class LiteralVisitor extends PCREBaseVisitor<Literals> {

	@Override
	public Literals visitParse(ParseContext ctx) {
		return visitAlternation(ctx.alternation());
	}

	@Override
	public Literals visitAlternation(AlternationContext ctx) {
		List<Literals> elementLiterals = new ArrayList<>();
		for (ExprContext expr: ctx.expr())
			elementLiterals.add(visitExpr(expr));
		
		return new OrLiterals(elementLiterals);
	}

	@Override
	public Literals visitExpr(ExprContext ctx) {
		List<Literals> elementLiterals = new ArrayList<>();
		
		for (ElementContext element: ctx.element()) {
			Literals atomLiterals = visitAtom(element.atom());
			if (atomLiterals != null) {
				QuantifierContext quantifier = element.quantifier();
				if (quantifier != null) {
					if (quantifier.Plus() != null) {
						elementLiterals.add(oneOrMore(atomLiterals));
					} else if (quantifier.QuestionMark() != null) {
						elementLiterals.add(new OrLiterals(new LeafLiterals(""), atomLiterals));
					} else if (quantifier.Star() != null) {
						elementLiterals.add(new OrLiterals(new LeafLiterals(""), oneOrMore(atomLiterals)));
					} else if (quantifier.exact != null) {
						int times = Integer.parseInt(quantifier.exact.getText());
						elementLiterals.add(repeat(atomLiterals, times));
					} else if (quantifier.min != null) { 
						int times = Integer.parseInt(quantifier.min.getText());
						if (times != 0) 
							elementLiterals.add(oneOrMore(repeat(atomLiterals, times)));
						else
							elementLiterals.add(new OrLiterals(new LeafLiterals(""), oneOrMore(atomLiterals)));
					} else {
						elementLiterals.add(new LeafLiterals(null));
					}
				} else {
					elementLiterals.add(atomLiterals);
				}
			} else {
				elementLiterals.add(new LeafLiterals(null));
			}
		}
		return new AndLiterals(elementLiterals);
	}

	private AndLiterals oneOrMore(Literals literals) {
		// assume a regex ab+c, below code allows literals ab and bc, 
		// but disallows abc 
		List<Literals> elementLiterals = new ArrayList<>();
		elementLiterals.add(literals);
		elementLiterals.add(new LeafLiterals(null));
		elementLiterals.add(literals);
		return new AndLiterals(elementLiterals);
	}
	
	private AndLiterals repeat(Literals literals, int times) {
		List<Literals> elementLiterals = new ArrayList<>();
		for (int i=0; i<times; i++)
			elementLiterals.add(literals);
		return new AndLiterals(elementLiterals);
	}
	
	@Nullable
	@Override
	public Literals visitAtom(AtomContext ctx) {
		if (ctx.literal() != null) {
			return new LeafLiterals(ctx.literal().getText());
		} else if (ctx.capture() != null) {
			return visitAlternation(ctx.capture().alternation());
		} else if (ctx.non_capture() != null) {
			return visitAlternation(ctx.non_capture().alternation());
		} else if (ctx.character_class() != null && ctx.character_class().simple_character_class() != null) {
			List<Literals> literals = new ArrayList<>();
			for (Cc_atomContext cc_atom: ctx.character_class().simple_character_class().cc_atom()) {
				if (cc_atom.cc_atom_literal != null && cc_atom.cc_atom_literal.shared_literal() != null) 
					literals.add(new LeafLiterals(cc_atom.cc_atom_literal.shared_literal().getText()));
				else
					return null;
			}
			return new OrLiterals(literals);
		} else {
			return null;
		}
	}

}
