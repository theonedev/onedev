package com.pmease.commons.lang.java;

import static com.pmease.commons.lang.java.JavaLexer.ASSIGN;
import static com.pmease.commons.lang.java.JavaLexer.AT;
import static com.pmease.commons.lang.java.JavaLexer.BOOLEAN;
import static com.pmease.commons.lang.java.JavaLexer.BYTE;
import static com.pmease.commons.lang.java.JavaLexer.CHAR;
import static com.pmease.commons.lang.java.JavaLexer.CLASS;
import static com.pmease.commons.lang.java.JavaLexer.COMMA;
import static com.pmease.commons.lang.java.JavaLexer.DEFAULT;
import static com.pmease.commons.lang.java.JavaLexer.DOT;
import static com.pmease.commons.lang.java.JavaLexer.DOUBLE;
import static com.pmease.commons.lang.java.JavaLexer.ELLIPSIS;
import static com.pmease.commons.lang.java.JavaLexer.ENUM;
import static com.pmease.commons.lang.java.JavaLexer.FLOAT;
import static com.pmease.commons.lang.java.JavaLexer.GT;
import static com.pmease.commons.lang.java.JavaLexer.IMPORT;
import static com.pmease.commons.lang.java.JavaLexer.INT;
import static com.pmease.commons.lang.java.JavaLexer.INTERFACE;
import static com.pmease.commons.lang.java.JavaLexer.Identifier;
import static com.pmease.commons.lang.java.JavaLexer.LBRACE;
import static com.pmease.commons.lang.java.JavaLexer.LBRACK;
import static com.pmease.commons.lang.java.JavaLexer.LONG;
import static com.pmease.commons.lang.java.JavaLexer.LPAREN;
import static com.pmease.commons.lang.java.JavaLexer.LT;
import static com.pmease.commons.lang.java.JavaLexer.PACKAGE;
import static com.pmease.commons.lang.java.JavaLexer.RBRACE;
import static com.pmease.commons.lang.java.JavaLexer.RBRACK;
import static com.pmease.commons.lang.java.JavaLexer.RPAREN;
import static com.pmease.commons.lang.java.JavaLexer.SEMI;
import static com.pmease.commons.lang.java.JavaLexer.SHORT;
import static com.pmease.commons.lang.java.JavaLexer.STATIC;
import static com.pmease.commons.lang.java.JavaLexer.StringLiteral;
import static com.pmease.commons.lang.java.JavaLexer.THIS;
import static com.pmease.commons.lang.java.JavaLexer.THROWS;
import static com.pmease.commons.lang.java.JavaLexer.VOID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.pmease.commons.lang.AbstractExtractor;
import com.pmease.commons.lang.LangStream;
import com.pmease.commons.lang.LangToken;
import com.pmease.commons.lang.Symbol;
import com.pmease.commons.lang.TokenFilter;
import com.pmease.commons.util.Pair;

public class JavaExtractor extends AbstractExtractor {
	
	private static final int[] PRIMITIVES = new int[]{
		FLOAT, INT, LONG, BOOLEAN, CHAR, BYTE, SHORT, DOUBLE, VOID};

	private static final Map<Integer, Integer> CLOSED_TYPES = 
			ImmutableMap.of(LBRACE, RBRACE, LBRACK, RBRACK, LPAREN, RPAREN, LT, GT);
	
	/* 
	 * Analyze specified text.
	 * 
	 * @before-token: start of file
	 * @after-token: EOF
	 * 
	 */
	public List<Symbol> extract(String text) {
		LangStream stream = new LangStream(
				new JavaLexer(new ANTLRInputStream(text)), TokenFilter.DEFAULT_CHANNEL);

		List<Symbol> symbols = new ArrayList<>();

		LangToken token = stream.next();
		
		while (token.is(AT) && !stream.lookAhead(1).is(INTERFACE)) {
			skipAnnotation(stream);
			token = stream.current();
		}
		
		CompilationUnit compilationUnit;
		if (token.is(PACKAGE)) {
			int lineNo = token.getLine(); 
			token = stream.next();
			String packageName = skipTypeName(stream);
			compilationUnit = new CompilationUnit(packageName, lineNo);
			token = stream.next();
		} else {
			compilationUnit = new CompilationUnit(null, -1);
		}
		
		symbols.add(compilationUnit);
		
		while (token.is(IMPORT)) {
			token = stream.nextType(SEMI);
			token = stream.next();
		}
		
		while(true) {
			while (token.is(SEMI))
				token = stream.next();
			if (!token.isEof()) {
				List<Modifier> modifiers = skipModifiers(stream);
				defineType(stream, symbols, compilationUnit, modifiers);
				token = stream.next();
			} else {
				break;
			}
		}
		
		return symbols;
	}
	
	/*
	 * Populate typeDef with various members in the type and then skip to end of the body.
	 * 
	 * @before-token: '{' or ';' (';' occurs inside of a enum definition)
	 * @after-token: '}'
	 */
	private void defineTypeBody(LangStream stream, List<Symbol> symbols, TypeDef parent) {
		LangToken token = stream.next();
		while(true) {
			while (token.is(SEMI))
				token = stream.next();
			if (token.is(LBRACE)) { // instance initializer
				stream.nextClosed(LBRACE, RBRACE);
				token = stream.next();
			} else if (token.is(STATIC) && stream.lookAhead(1).is(LBRACE)) { // static initializer
				stream.next();
				stream.nextClosed(LBRACE, RBRACE);
				token = stream.next();
			} else if (token.is(RBRACE)) {
				break;
			} else {
				List<Modifier> modifiers = skipModifiers(stream);
				token = stream.current();
				if (token.is(LT)) { // skip type params of constructor or method
					stream.nextClosed(LT, GT);
					token = stream.next();
				}
				if (token.is(CLASS, INTERFACE, ENUM) || token.is(AT) && stream.lookAhead(1).is(INTERFACE)) {
					defineType(stream, symbols, parent, modifiers);
				} else {
					skipModifiers(stream); // skip annotations applied to method return type
					token = stream.current();
					if (token.getText().equals(parent.getName()) && stream.lookAhead(1).is(LPAREN)) { 
						// this is a constructor
						defineMethod(stream, symbols, parent, modifiers, null);
					} else {
						String typeRef = skipTypeRef(stream);
						token = stream.current();
						if (token.is(Identifier) && stream.lookAhead(1).is(LPAREN))
							defineMethod(stream, symbols, parent, modifiers, typeRef);
						else
							defineFields(stream, symbols, parent, modifiers, typeRef);
					}
				}
				token = stream.next();
			}
		}
		
	}
	
	/*
	 * Skip value assigned to a field. We either need to skip to next ';' (end of current statement) 
	 * or ',' (separator of field declaration). After stripping all top scopes ('[...]', '{...}', 
	 * '(...)') before encountering ',' or ';', we can guarantee that ';' will not be contained in value, 
	 * however ',' can still appear in value in below form:
	 * ...new HashMap<String, String>...
	 * Also we can not strip scope '<...>' to remove this possibility as '<' '>' can also be used as 
	 * conditional operators in value expression (for instance: 2>1?a:b). So we assume encountered ',' 
	 * as field separator first, and continue with our analyzing until our assumption is proved to be 
	 * true or false. True assumption will result in over skipping and it will return a list of 
	 * subsequent encountered field names, while false assumption does no harm, as we simply move on 
	 * to next comma in the loop.       
	 * 
	 * @before-token: start of value
	 * @after-token: next token after value
	 * 
	 * @return 
	 * 			list of subsequent field names if skipping value caused multiple 
	 * 			fields to be consumed
	 * 
	 */
	private List<Pair<LangToken, String>> skipValue(LangStream stream) {
		List<Pair<LangToken, String>> fieldInfos = new ArrayList<>();
		LangToken token = stream.current();
		if (token.is(LBRACE)) { // array value
			stream.nextClosed(LBRACE, RBRACE);
			stream.next();
			return fieldInfos;
		} else {
			while (true) {
				token = stream.nextType(COMMA, SEMI, LBRACE, LBRACK, LPAREN);
				if (token.is(LBRACE, LBRACK, LPAREN)) { 
					// skip scopes and the only case for comma inside a value is inside type arguments like below:
					// ... new HashMap<String, String>...
					stream.nextClosed(token.getType(), CLOSED_TYPES.get(token.getType()));
				} else if (token.is(SEMI)) {
					break;
				} else { // token is ','
					token.checkType(COMMA);
					List<Pair<LangToken, String>> subsequentFieldTokens = assumeFieldSeparator(stream);
					if (subsequentFieldTokens != null) { // assumption is correct
						fieldInfos.addAll(subsequentFieldTokens);
						break;
					}
				}
			}
			return fieldInfos;
		}
	}

	/*
	 * Assume current token is field separator and continue to analyze the stream. Upon 
	 * analyzing surprise, we return a null value to indicate the assumption is incorrect. 
	 * 
	 * @before-token: ','
	 * @after-token: next field separator (',') or ';' if assumption is 
	 * 					correct; or any position in middle of value if 
	 * 					assumption is incorrect
	 * 
	 * @return 
	 * 			list of encountered subsequent field tokens and array indicators if assumption is correct, or 
	 * 			null if assumption is incorrect 
	 * 
	 */
	private List<Pair<LangToken, String>> assumeFieldSeparator(LangStream stream) {
		while (true) {
			LangToken token = stream.next();
			if (token.is(Identifier)) {
				List<Pair<LangToken, String>> fieldInfos = new ArrayList<>();
				stream.next();
				fieldInfos.add(new Pair<LangToken, String>(token, skipDims(stream)));
				token = stream.current();
				if (token.is(ASSIGN)) { // assumption convinced
					stream.next();
					fieldInfos.addAll(skipValue(stream));
					return fieldInfos;
				} else if (token.is(SEMI)) { // assumption convinced
					return fieldInfos;
				} else if (token.is(COMMA)) { // assumption still not convinced 
					List<Pair<LangToken, String>> subsequentFieldInfos = assumeFieldSeparator(stream);
					if (subsequentFieldInfos != null) {
						fieldInfos.addAll(subsequentFieldInfos);
						return fieldInfos;
					} else { 
						return null;
					}
				} else { // assumption is incorrect
					return null;
				}
			} else { // assumption is incorrect
				return null;
			}
		}
	}
	
	/*
	 * Skip possible dims.
	 * 
	 * @before-token: start of possible dims
	 * @after-token: next token after dims, or remain unchanged if no dims  
	 */
	private String skipDims(LangStream stream) {
		String dims = "";
		while (true) {
			skipAnnotations(stream);
			LangToken token = stream.current();
			if (token.is(LBRACK)) {
				dims += "[]";
				stream.nextClosed(LBRACK, RBRACK);
				stream.next();
			} else {
				break;
			}
		}
		return dims;
	}
	
	/*
	 * Skip type reference.
	 * 
	 * @before-token: start of type ref
	 * @after-token: next token after type ref
	 */
	private String skipTypeRef(LangStream stream) {
		LangToken token = stream.current();
		String typeRef = token.getText();
		if (token.is(PRIMITIVES)) {
			stream.next();
		} else { 
			typeRef = skipTypeRefSegment(stream);
			token = stream.current();
			while (token.is(DOT)) {
				typeRef += ".";
				stream.next();
				typeRef += skipTypeRefSegment(stream);
				token = stream.current();
			}
		}
		typeRef += skipDims(stream); 
		return typeRef;
	}

	/*
	 * Skip type ref segment.
	 * 
	 * @begin-token: start of a type ref segment including possible annotations
	 * @end-token: next token after type ref segment
	 */
	private String skipTypeRefSegment(LangStream stream) {
		skipModifiers(stream);
		String identifier = stream.current().getText();
		LangToken token = stream.next();
		if (token.is(LT)) {
			int tokenPos = stream.pos();
			stream.nextClosed(LT, GT);
			LangStream typeArgStream = new LangStream(stream.between(tokenPos, stream.pos()));
			token = typeArgStream.next();
			while (true) {
				skipModifiers(typeArgStream);
				token = typeArgStream.current();
				if (token.isEof())
					break;
				else
					identifier += token.getText();
				token = typeArgStream.next();
			}
			stream.next();
 		}
		return identifier;
	}
	
	/*
	 * Define a method or constructor.
	 * 
	 * @before-token: identifier of the method
	 * @after-token: '}' for class method, ';' for interface method or annotation attribute
	 */
	private void defineMethod(LangStream stream, List<Symbol> symbols, TypeDef parent, 
			List<Modifier> modifiers, @Nullable String typeRef) {
		LangToken token = stream.current();
		String name = token.getText();
		int lineNo = token.getLine();
		String params = null;
		String type = null;
		
		stream.next().checkType(LPAREN); // '('
		token = stream.next();
		while (!token.is(RPAREN)) {
			skipModifiers(stream);
			String paramType = skipTypeRef(stream);
			skipModifiers(stream); // skip possible annotation applied to '...'
			token = stream.current();
			if (token.is(ELLIPSIS)) { // varargs
				paramType += "...";
				token = stream.next();
				if (token.is(Identifier))
					token = stream.next();
			} else if (token.is(THIS)) { // Receiver parameter
				token = stream.next();
				paramType = null;
			} else if (token.is(Identifier) && stream.lookAhead(1).is(DOT)) { // Receiver parameter
				stream.next().checkType(DOT); // '.'
				stream.next().checkType(THIS); // 'this'
				token = stream.next();
				paramType = null;
			} else if (token.is(Identifier)) {
				token = stream.next();
				paramType += skipDims(stream);
				token = stream.current();
			} else { // No identifier, this is toString() version of MethodDef
				
			}
			if (paramType != null) {
				if (params != null)
					params += ", " + paramType;
				else
					params = paramType;
			}
			if (token.is(COMMA))
				token = stream.next();
		}
		token = stream.next();
		if (typeRef != null)
			type = typeRef + skipDims(stream);

		token = stream.current();
		if (token.is(THROWS)) { 
			while (true) {
				token = stream.nextType(SEMI, LBRACE, LPAREN);
				if (token.is(LPAREN)) {
					stream.nextClosed(LPAREN, RPAREN);
					token = stream.next();
				} else {
					break;
				}
			}
		} else if (token.is(DEFAULT)) {
			stream.nextType(SEMI);
		}

		token = stream.current();
		
		if (token.is(LBRACE))
			stream.nextClosed(LBRACE, RBRACE);
		
		symbols.add(new MethodDef(parent, name, lineNo, type, params, modifiers));
	}
	
	/*
	 * Define fields declared in a single statement. 
	 * 
	 * @before-token: identifier of field declaration statement
	 * @after-token: end of fields declaration statement, which is ';'
	 */
	private void defineFields(LangStream stream, List<Symbol> symbols, TypeDef parent, 
			List<Modifier> modifiers, String typeRef) {
		LangToken token = stream.current();
		while (!token.is(SEMI)) {
			stream.next();
			symbols.add(new FieldDef(parent, token.getText(), token.getLine(), typeRef + skipDims(stream), modifiers));
			token = stream.current();
			if (token.is(ASSIGN)) {
				stream.next();
				for (Pair<LangToken, String> fieldInfo: skipValue(stream)) {
					symbols.add(new FieldDef(parent, fieldInfo.getFirst().getText(), fieldInfo.getFirst().getLine(), 
							typeRef + fieldInfo.getSecond(), modifiers));
				}
				token = stream.current();
			} 
			if (token.is(COMMA))
				token = stream.next();
		}
	}
	
	/*
	 * Define a type. 
	 * 
	 * @before-token: 'class', 'interface', 'enum', or '@interface'
	 * @after-token: '}'
	 */
	private void defineType(LangStream stream, List<Symbol> symbols, Symbol parent, List<Modifier> modifiers) {
		LangToken token = stream.current();
		if (token.is(AT) && stream.lookAhead(1).is(INTERFACE)) {
			stream.next().checkType(INTERFACE); // 'interface'
			stream.next().checkType(Identifier); // identifier
			token = defineTypeHead(stream);
			TypeDef typeDef = new TypeDef(parent, token.getText(), token.getLine(), TypeDef.Kind.ANNOTATION, modifiers);			
			symbols.add(typeDef);
			defineTypeBody(stream, symbols, typeDef);
		} else if (token.is(CLASS)) {
			stream.next().checkType(Identifier); // identifier
			token = defineTypeHead(stream);
			TypeDef typeDef = new TypeDef(parent, token.getText(), token.getLine(), TypeDef.Kind.CLASS, modifiers);			
			symbols.add(typeDef);
			defineTypeBody(stream, symbols, typeDef);
		} else if (token.is(INTERFACE)) {
			stream.next().checkType(Identifier); // identifier
			token = defineTypeHead(stream);
			TypeDef typeDef = new TypeDef(parent, token.getText(), token.getLine(), TypeDef.Kind.INTERFACE, modifiers);			
			symbols.add(typeDef);
			defineTypeBody(stream, symbols, typeDef);
		} else { 
			stream.next().checkType(Identifier); // identifier
			token = defineTypeHead(stream);
			TypeDef typeDef = new TypeDef(parent, token.getText(), token.getLine(), TypeDef.Kind.ENUM, modifiers);			
			symbols.add(typeDef);
			
			// process enum constants
			token = stream.next();
			while (true) {
				if (token.is(SEMI, RBRACE)) {
					break;
				} else if (token.is(COMMA)) {
					token = stream.next();
				} else {
					skipModifiers(stream); // skip annotations
					
					symbols.add(new FieldDef(typeDef, stream.current().getText(), stream.current().getLine(), 
							null, Lists.newArrayList(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)));
					token = stream.next();
					if (token.is(LPAREN)) { // enum constant arguments
						stream.nextClosed(LPAREN, RPAREN);
						token = stream.next();
					}
					if (token.is(LBRACE)) { // enum constant class body
						stream.nextClosed(LBRACE, RBRACE);
						token = stream.next();
					}
				}
			}
			
			if (token.is(SEMI))
				defineTypeBody(stream, symbols, typeDef);
		}
	}
	
	/*
	 * Populate typeDef with identifier and then skip to type body.
	 *  
	 * @before-token: type identifier 
	 * @after-token: start of type body which is '{' 
	 */
	private LangToken defineTypeHead(LangStream stream) {
		LangToken typeHeadToken = stream.current();
		
		while (true) {
			LangToken token = stream.nextType(LBRACE, LPAREN);
			if (token.is(LPAREN)) {
				stream.nextClosed(LPAREN, RPAREN);
				token = stream.next();
			} else {
				break;
			}
		}
		
		return typeHeadToken;
	}
	
	/*
	 * This method skips possible modifiers from current stream position. 
	 * 
	 * @before-token: possible start of modifiers
	 * @after-token: remain unchanged or token after the modifiers if there are modifiers 
	 */
	private List<Modifier> skipModifiers(LangStream stream) {
		List<Modifier> modifiers = new ArrayList<>();
		LangToken token = stream.current();
		while (true) {
			if (token.is(AT) && !stream.lookAhead(1).is(INTERFACE)) {
				skipAnnotation(stream);
				token = stream.current();
			} else if (!token.is(StringLiteral)) {
				Modifier modifier = null;
				for (Modifier each: Modifier.values()) {
					if (each.name().toLowerCase().equals(token.getText())) {
						modifier = each;
						break;
					}
				}
				if (modifier != null) {
					modifiers.add(modifier);
					token = stream.next();
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return modifiers;
	}
	
	/*
	 * This method skips possible annotations from current stream position. 
	 * 
	 * @before-token: possible start of annotations
	 * @after-token: remain unchanged or token after the annotations if there are annotations
	 */
	private void skipAnnotations(LangStream stream) {
		LangToken token = stream.current();
		while (true) {
			if (token.is(AT) && !stream.lookAhead(1).is(INTERFACE)) {
				skipAnnotation(stream);
				token = stream.current();
			} else {
				break;
			}
		}
	}
	
	/*
	 * Skip a single annotation.
	 * 
	 * @before-token: '@'
	 * @after-token: token after the annotation
	 */
	private void skipAnnotation(LangStream stream) {
		stream.next();
		skipTypeName(stream);
		LangToken token = stream.current();
		if (token.is(LPAREN)) {
			token = stream.nextClosed(LPAREN, RPAREN);
			token = stream.next();
		}
	}
	
	/*
	 * Skip type name sections. 
	 * 
	 * @before-token: first section of type name
	 * @after-token: token after type name  
	 */
	private String skipTypeName(LangStream stream) {
		String typeName = stream.current().getText();
		LangToken token = stream.next();
		while (token.is(DOT)) {
			typeName += ".";
			token = stream.next();
			typeName += token.getText();
			token = stream.next();
		}
		return typeName;
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "java");
	}
	
}

