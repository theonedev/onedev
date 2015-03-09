package com.pmease.commons.lang.analyzers.java;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.pmease.commons.lang.LineAwareToken;
import com.pmease.commons.lang.Outline;
import com.pmease.commons.lang.Token;
import com.pmease.commons.lang.TokenStream;
import com.pmease.commons.lang.TokenizedLine;

public class JavaAnalyzer {
	
	private static final String[] PRIMITIVES = new String[]{
		"float", "int", "long", "boolean", "char", "byte", "short", "double", "void"};

	public Outline analyze(List<TokenizedLine> lines) {
		List<LineAwareToken> tokens = new ArrayList<>();
		int linePos = 0;
		for (TokenizedLine line: lines) {
			for (Token token: line.getTokens()) {
				if (!token.isComment() && !token.isWhitespace()) {
					// Code mirror tokenize @ together with subsequent identifier as a meta 
					// if they are not separated by space 
					if (token.isMeta() && token.text().startsWith("@") && token.text().length() > 1) {
						tokens.add(new LineAwareToken("", "@", linePos));
						String rest = token.text().substring(1);
						if (rest.equals("interface"))
							tokens.add(new LineAwareToken("cm-keyword", rest, linePos));
						else
							tokens.add(new LineAwareToken("cm-variable", rest, linePos));
					} else {
						tokens.add(new LineAwareToken(token, linePos));
					}
				}
			}
			linePos++;
		}
		
		return analyze(new TokenStream(tokens));
	}

	/* 
	 * Analyze specified stream.
	 * 
	 * @before-token: start of file
	 * @after-token: EOF
	 * 
	 */
	private JavaOutline analyze(TokenStream stream) {
		JavaOutline outline = new JavaOutline();
		
		Token token = stream.current();
		
		while (token.is("@") && !stream.lookAhead(1).is("interface"))
			skipAnnotation(stream);
		
		if (token.is("package")) {
			token = stream.next();
			outline.packageName = skipTypeName(stream);
			token = stream.next();
		}
		
		while (token.is("import")) {
			token = stream.nextSymbol(";");
			token = stream.next();
		}
		
		while(true) {
			while (token.is(";"))
				token = stream.next();
			if (!token.isEof()) {
				List<Modifier> modifiers = skipModifiers(stream);
				outline.typeDefs.add(defineType(stream, modifiers));
				token = stream.next();
			} else {
				break;
			}
		}
		
		return outline;
	}
	
	/*
	 * Populate typeDef with various members in the type and then skip to end of the body.
	 * 
	 * @before-token: '{' or ';' (';' occurs inside of a enum definition)
	 * @after-token: '}'
	 */
	private void defineTypeBody(TokenStream stream, TypeDef typeDef) {
		Token token = stream.next();
		while(true) {
			while (token.is(";"))
				token = stream.next();
			if (token.is("{")) { // instance initializer
				stream.nextBalanced(token);
				stream.next();
			} else if (token.is("static") && stream.lookAhead(1).is("{")) { // static initializer
				stream.nextBalanced(stream.next());
				stream.next();
			} else if (token.is("}") || token.isEof()) {
				break;
			} else {
				List<Modifier> modifiers = skipModifiers(stream);
				if (token.is("<")) { // skip type params of constructor or method
					stream.nextBalanced(token);
					token = stream.next();
				}
				if (token.is("class", "interface", "enum") || token.is("@") && stream.lookAhead(1).is("interface")) {
					typeDef.typeDefs.add(defineType(stream, modifiers));
				} else {
					skipModifiers(stream); // skip annotations applied to method return type
					token = stream.current();
					if (token.text().equals(typeDef.name) && stream.lookAhead(1).is("(")) { 
						// this is a constructor
						typeDef.methodDefs.add(defineMethod(stream, modifiers, null));
					} else {
						String typeRef = skipTypeRef(stream);
						token = stream.current();
						if (token.isIdentifier() && stream.lookAhead(1).is("("))
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
	private List<String> skipValue(TokenStream stream) {
		List<String> fieldNames = new ArrayList<>();
		Token token = stream.current();
		if (token.is("{")) { // array value
			stream.nextBalanced(token);
			stream.next();
			return fieldNames;
		} else {
			while (true) {
				token = stream.nextSymbol(",", ";", "{", "[", "(");
				if (token.is("{", "[", "(")) { 
					// skip scopes and the only case for comma inside a value is inside type arguments like below:
					// ... new HashMap<String, String>...
					stream.nextBalanced(token);
					stream.next();
				} else if (token.is(";") || token.isEof()) {
					break;
				} else { // token is ','
					List<String> subsequentFieldNames = assumeFieldSeparator(stream);
					if (subsequentFieldNames != null) { // assumption is correct
						fieldNames.addAll(subsequentFieldNames);
						break;
					}
				}
			}
			return fieldNames;
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
	 * 			list of encountered subsequent field names if assumption is correct, or 
	 * 			null if assumption is incorrect 
	 * 
	 */
	private List<String> assumeFieldSeparator(TokenStream stream) {
		while (true) {
			Token token = stream.next();
			if (token.isIdentifier()) {
				List<String> fieldNames = new ArrayList<>();
				String identifier = token.text();
				stream.next();
				identifier += skipDims(stream);
				fieldNames.add(identifier);
				token = stream.current();
				if (token.is("=")) { // assumption convinced
					stream.next();
					fieldNames.addAll(skipValue(stream));
					return fieldNames;
				} else if (token.is(";")) { // assumption convinced
					return fieldNames;
				} else if (token.is(",")) { // assumption still not convinced 
					List<String> subsequentFieldNames = assumeFieldSeparator(stream);
					if (subsequentFieldNames != null)
						fieldNames.addAll(subsequentFieldNames);
					else 
						return null;
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
	private String skipDims(TokenStream stream) {
		String dims = "";
		while (true) {
			skipModifiers(stream);
			Token token = stream.current();
			if (token.is("[")) {
				dims += "[]";
				stream.nextBalanced(token);
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
	private String skipTypeRef(TokenStream stream) {
		LineAwareToken token = stream.current();
		String typeRef = token.text();
		if (token.is(PRIMITIVES)) {
			stream.next();
		} else { 
			typeRef = skipTypeRefSegment(stream);
			while (token.is(".")) {
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
	private String skipTypeRefSegment(TokenStream stream) {
		skipModifiers(stream);
		String identifier = stream.current().text();
		Token token = stream.next();
		if (token.is("<")) {
			int tokenPos = stream.tokenPos();
			stream.nextBalanced(token);
			TokenStream typeArgStream = new TokenStream(stream.tokens(tokenPos, stream.tokenPos()));
			token = typeArgStream.current();
			while (!token.isEof()) {
				skipModifiers(typeArgStream);
				identifier += token.text();
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
	private MethodDef defineMethod(TokenStream stream, List<Modifier> modifiers, @Nullable String typeRef) {
		Token token = stream.current();
		MethodDef methodDef = new MethodDef();
		methodDef.modifiers = modifiers;
		methodDef.name = token.text();
		stream.next(); // '('
		token = stream.next();
		while (!token.is(")") && !token.isEof()) {
			skipModifiers(stream);
			String paramType = skipTypeRef(stream);
			skipModifiers(stream);
			token = stream.current();
			if (token.is("...")) { // varargs
				paramType += "...";
				stream.next(); // identifier
				token = stream.next();
			} if (token.is("this")) { // Receiver parameter
				token = stream.next();
				paramType = null;
			} else if (token.isIdentifier() && stream.lookAhead(1).is(".")) { // Receiver parameter
				stream.next(); // '.'
				stream.next(); // 'this'
				token = stream.next();
				paramType = null;
			} else { // normal parameter
				stream.next(); // identifier
				stream.next();
				paramType += skipDims(stream);
				token = stream.current();
			}
			if (paramType != null) {
				if (methodDef.params != null)
					methodDef.params += ", " + paramType;
				else
					methodDef.params = paramType;
			}
			if (token.is(","))
				token = stream.next();
		}
		token = stream.next();
		if (typeRef != null)
			methodDef.returnType = typeRef + skipDims(stream);

		token = stream.current();
		if (token.is("throws")) { 
			while (true) {
				token = stream.nextSymbol(";", "{", "(");
				if (token.is("(")) {
					stream.nextBalanced(token);
					token = stream.next();
				} else {
					break;
				}
			}
		} else if (token.is("default")) {
			stream.nextSymbol(";");
		}

		token = stream.current();
		
		if (token.is("{"))
			stream.nextBalanced(token);
		
		return methodDef;
	}
	
	/*
	 * Define fields declared in a single statement. 
	 * 
	 * @before-token: identifier of field declaration statement
	 * @after-token: end of fields declaration statement, which is ';'
	 */
	private List<FieldDef> defineFields(TokenStream stream, List<Modifier> modifiers, String typeRef) {
		Token token = stream.current();
		List<FieldDef> fieldDefs = new ArrayList<>();
		while (!token.is(";") && !token.isEof()) {
			FieldDef fieldDef = new FieldDef();
			fieldDefs.add(fieldDef);
			fieldDef.name = token.text();
			fieldDef.modifiers = modifiers;
			stream.next();
			fieldDef.type = typeRef + skipDims(stream);
			token = stream.current();
			if (token.is("=")) {
				stream.next();
				for (String fieldName: skipValue(stream)) {
					fieldDef = new FieldDef();
					int index = fieldName.indexOf('[');
					if (index != -1) {
						fieldDef.type = typeRef + fieldName.substring(index);
						fieldDef.name = fieldName.substring(0, index);
					} else {
						fieldDef.type = typeRef;
						fieldDef.name = fieldName;
					}
					fieldDef.modifiers = modifiers;
					fieldDefs.add(fieldDef);
				}
				token = stream.current();
			} 
			if (token.is(","))
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
	private TypeDef defineType(TokenStream stream, List<Modifier> modifiers) {
		TypeDef typeDef = new TypeDef();
		typeDef.modifiers = modifiers;
		Token token = stream.current();
		if (token.is("@") && stream.lookAhead(1).is("interface")) {
			typeDef.kind = TypeDef.Kind.ANNOTATION;
			stream.next(); // 'interface'
			stream.next(); // identifier
			defineTypeHead(stream, typeDef);
			defineTypeBody(stream, typeDef);
			return typeDef;
		} else if (token.is("class")) {
			typeDef.kind = TypeDef.Kind.CLASS;
			stream.next(); // identifier
			defineTypeHead(stream, typeDef);
			defineTypeBody(stream, typeDef);
			return typeDef;
		} else if (token.is("interface")) {
			typeDef.kind = TypeDef.Kind.INTERFACE;
			stream.next(); // identifier
			defineTypeHead(stream, typeDef);
			defineTypeBody(stream, typeDef);
			return typeDef;
		} else { 
			typeDef.kind = TypeDef.Kind.ENUM;
			stream.next(); // identifier
			defineTypeHead(stream, typeDef);
			
			// process enum constants
			token = stream.next();
			while (true) {
				if (token.is(";", "}") || token.isEof()) {
					break;
				} else if (token.is(",")) {
					token = stream.next();
				} else {
					FieldDef fieldDef = new FieldDef();
					skipModifiers(stream); // skip annotations
					fieldDef.modifiers = Lists.newArrayList(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
					fieldDef.name = stream.current().text();
					token = stream.next();
					if (token.is("(")) { // enum constant arguments
						stream.nextBalanced(token);
						token = stream.next();
					}
					if (token.is("{")) { // enum constant class body
						stream.nextBalanced(token);
						token = stream.next();
					}
					typeDef.fieldDefs.add(fieldDef);
				}
			}
			
			if (token.is(";"))
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
	private void defineTypeHead(TokenStream stream, TypeDef typeDef) {
		Token token = stream.current();
		typeDef.name = token.text();
		
		while (true) {
			token = stream.nextSymbol("{", "(");
			if (token.is("(")) {
				stream.nextBalanced(token);
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
	private List<Modifier> skipModifiers(TokenStream stream) {
		List<Modifier> modifiers = new ArrayList<>();
		Token token = stream.current();
		while (true) {
			if (token.is("@") && !stream.lookAhead(1).is("interface")) {
				skipAnnotation(stream);
				token = stream.current();
			} else if (!token.isComment() && !token.isString()) {
				Modifier modifier = null;
				for (Modifier each: Modifier.values()) {
					if (each.name().toLowerCase().equals(token.text())) {
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
	 * Skip a single annotation.
	 * 
	 * @before-token: '@'
	 * @after-token: token after the annotation
	 */
	private void skipAnnotation(TokenStream stream) {
		stream.next();
		skipTypeName(stream);
		Token token = stream.current();
		if (token.is("(")) {
			token = stream.nextBalanced(token);
			token = stream.next();
		}
	}
	
	/*
	 * Skip type name sections. 
	 * 
	 * @before-token: first section of type name
	 * @after-token: token after type name  
	 */
	private String skipTypeName(TokenStream stream) {
		String typeName = stream.current().text();
		Token token = stream.next();
		while (token.is(".")) {
			typeName += ".";
			token = stream.next();
			typeName += token.text();
			token = stream.next();
		}
		return typeName;
	}
	
}

