package com.pmease.commons.lang.java;

import static com.pmease.commons.lang.java.JavaLexer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.pmease.commons.lang.AbstractAnalyzer;
import com.pmease.commons.lang.AnalyzeResult;
import com.pmease.commons.lang.LangStream;
import com.pmease.commons.lang.LangToken;
import com.pmease.commons.lang.TokenFilter;
import com.pmease.commons.util.Pair;

public class JavaAnalyzer extends AbstractAnalyzer {
	
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
	public AnalyzeResult analyze(String text) {
		LangStream stream = new LangStream(
				new JavaLexer(new ANTLRInputStream(text)), TokenFilter.DEFAULT_CHANNEL);
		
		JavaOutline outline = new JavaOutline();

		LangToken token = stream.next();
		
		while (token.is(AT) && !stream.lookAhead(1).is(INTERFACE)) {
			skipAnnotation(stream);
			token = stream.current();
		}
		
		if (token.is(PACKAGE)) {
			token = stream.next();
			outline.packageName = skipTypeName(stream);
			token = stream.next();
		}
		
		while (token.is(IMPORT)) {
			token = stream.nextType(SEMI);
			token = stream.next();
		}
		
		while(true) {
			while (token.is(SEMI))
				token = stream.next();
			if (!token.isEof()) {
				List<Modifier> modifiers = skipModifiers(stream);
				outline.typeDefs.add(defineType(stream, modifiers));
				token = stream.next();
			} else {
				break;
			}
		}
		
		return new AnalyzeResult(stream.allType(Identifier), outline);
	}
	
	/*
	 * Populate typeDef with various members in the type and then skip to end of the body.
	 * 
	 * @before-token: '{' or ';' (';' occurs inside of a enum definition)
	 * @after-token: '}'
	 */
	private void defineTypeBody(LangStream stream, TypeDef typeDef) {
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
					typeDef.typeDefs.add(defineType(stream, modifiers));
				} else {
					skipModifiers(stream); // skip annotations applied to method return type
					token = stream.current();
					if (token.getText().equals(typeDef.name) && stream.lookAhead(1).is(LPAREN)) { 
						// this is a constructor
						typeDef.methodDefs.add(defineMethod(stream, modifiers, null));
					} else {
						String typeRef = skipTypeRef(stream);
						token = stream.current();
						if (token.is(Identifier) && stream.lookAhead(1).is(LPAREN))
							typeDef.methodDefs.add(defineMethod(stream, modifiers, typeRef));
						else
							typeDef.fieldDefs.addAll(defineFields(stream, modifiers, typeRef));
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
	private MethodDef defineMethod(LangStream stream, List<Modifier> modifiers, @Nullable String typeRef) {
		LangToken token = stream.current();
		MethodDef methodDef = new MethodDef();
		methodDef.modifiers = modifiers;
		methodDef.name = token;
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
				if (methodDef.params != null)
					methodDef.params += ", " + paramType;
				else
					methodDef.params = paramType;
			}
			if (token.is(COMMA))
				token = stream.next();
		}
		token = stream.next();
		if (typeRef != null)
			methodDef.returnType = typeRef + skipDims(stream);

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
		
		return methodDef;
	}
	
	/*
	 * Define fields declared in a single statement. 
	 * 
	 * @before-token: identifier of field declaration statement
	 * @after-token: end of fields declaration statement, which is ';'
	 */
	private List<FieldDef> defineFields(LangStream stream, List<Modifier> modifiers, String typeRef) {
		LangToken token = stream.current();
		List<FieldDef> fieldDefs = new ArrayList<>();
		while (!token.is(SEMI)) {
			FieldDef fieldDef = new FieldDef();
			fieldDefs.add(fieldDef);
			fieldDef.name = token;
			fieldDef.modifiers = modifiers;
			stream.next();
			fieldDef.type = typeRef + skipDims(stream);
			token = stream.current();
			if (token.is(ASSIGN)) {
				stream.next();
				for (Pair<LangToken, String> fieldInfo: skipValue(stream)) {
					fieldDef = new FieldDef();
					if (fieldInfo.getSecond().length() != 0) {
						fieldDef.type = typeRef + fieldInfo.getSecond();
						fieldDef.name = fieldInfo.getFirst();
					} else {
						fieldDef.type = typeRef;
						fieldDef.name = fieldInfo.getFirst();
					}
					fieldDef.modifiers = modifiers;
					fieldDefs.add(fieldDef);
				}
				token = stream.current();
			} 
			if (token.is(COMMA))
				token = stream.next();
		}
		return fieldDefs;
	}
	
	/*
	 * Define a type. 
	 * 
	 * @before-token: 'class', 'interface', 'enum', or '@interface'
	 * @after-token: '}'
	 */
	private TypeDef defineType(LangStream stream, List<Modifier> modifiers) {
		TypeDef typeDef = new TypeDef();
		typeDef.modifiers = modifiers;
		LangToken token = stream.current();
		if (token.is(AT) && stream.lookAhead(1).is(INTERFACE)) {
			typeDef.kind = TypeDef.Kind.ANNOTATION;
			stream.next().checkType(INTERFACE); // 'interface'
			stream.next().checkType(Identifier); // identifier
			defineTypeHead(stream, typeDef);
			defineTypeBody(stream, typeDef);
			return typeDef;
		} else if (token.is(CLASS)) {
			typeDef.kind = TypeDef.Kind.CLASS;
			stream.next().checkType(Identifier); // identifier
			defineTypeHead(stream, typeDef);
			defineTypeBody(stream, typeDef);
			return typeDef;
		} else if (token.is(INTERFACE)) {
			typeDef.kind = TypeDef.Kind.INTERFACE;
			stream.next().checkType(Identifier); // identifier
			defineTypeHead(stream, typeDef);
			defineTypeBody(stream, typeDef);
			return typeDef;
		} else { 
			typeDef.kind = TypeDef.Kind.ENUM;
			stream.next().checkType(Identifier); // identifier
			defineTypeHead(stream, typeDef);
			
			// process enum constants
			token = stream.next();
			while (true) {
				if (token.is(SEMI, RBRACE)) {
					break;
				} else if (token.is(COMMA)) {
					token = stream.next();
				} else {
					FieldDef fieldDef = new FieldDef();
					skipModifiers(stream); // skip annotations
					fieldDef.modifiers = Lists.newArrayList(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
					fieldDef.name = stream.current();
					token = stream.next();
					if (token.is(LPAREN)) { // enum constant arguments
						stream.nextClosed(LPAREN, RPAREN);
						token = stream.next();
					}
					if (token.is(LBRACE)) { // enum constant class body
						stream.nextClosed(LBRACE, RBRACE);
						token = stream.next();
					}
					typeDef.fieldDefs.add(fieldDef);
				}
			}
			
			if (token.is(SEMI))
				defineTypeBody(stream, typeDef);
			
			return typeDef;
		}
	}
	
	/*
	 * Populate typeDef with identifier and then skip to type body.
	 *  
	 * @before-token: type identifier 
	 * @after-token: start of type body which is '{' 
	 */
	private void defineTypeHead(LangStream stream, TypeDef typeDef) {
		LangToken token = stream.current();
		typeDef.name = token;
		
		while (true) {
			token = stream.nextType(LBRACE, LPAREN);
			if (token.is(LPAREN)) {
				stream.nextClosed(LPAREN, RPAREN);
				token = stream.next();
			} else {
				break;
			}
		}
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
	public String getVersion() {
		return "1.0";
	}

	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "java");
	}
	
}

