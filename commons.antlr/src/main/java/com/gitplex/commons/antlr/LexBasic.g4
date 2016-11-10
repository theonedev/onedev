/*
 * [The "BSD license"]
 *  Copyright (c) 2014-2015 Gerald Rosenberg
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/** 
 * A generally reusable set of fragments for import in to Lexer grammars.
 *
 *	Modified 2015.06.16 gbr - 
 *	-- generalized for inclusion into the ANTLRv4 grammar distribution
 * 
 */
  
lexer grammar LexBasic;

import LexUnicode;	// Formal set of Unicode ranges


// ======================================================
// Lexer fragments
//


// -----------------------------------
// Whitespace & Comments

fragment Ws				: Hws | Vws	;
fragment Hws			: [ \t]		;
fragment Vws			: [\r\n\f]	;

fragment DocComment		: '/**' .*? ('*/' | EOF)	;
fragment BlockComment	: '/*'  .*? ('*/' | EOF)	;

fragment LineComment	: '//' ~[\r\n]* 							;
fragment LineCommentExt	: '//' ~'\n'* ( '\n' Hws* '//' ~'\n'* )*	;


// -----------------------------------
// Escapes

// Any kind of escaped character that we can embed within ANTLR literal strings.
fragment EscSeq
	:	Esc
		( [btnfr"'\\]	// The standard escaped character set such as tab, newline, etc.
		| UnicodeEsc	// A Unicode escape sequence
		| .				// Invalid escape character
		| EOF			// Incomplete at EOF
		)
	;

fragment EscAny
	:	Esc .
	;

fragment UnicodeEsc
	:	'u' (HexDigit (HexDigit (HexDigit HexDigit?)?)?)?
	;

fragment OctalEscape
	:	OctalDigit
	|	OctalDigit OctalDigit
	|	[0-3] OctalDigit OctalDigit
	;


// -----------------------------------
// Numerals

fragment HexNumeral
	:	'0' [xX] HexDigits
	;

fragment OctalNumeral
	:	'0' '_' OctalDigits
	;

fragment DecimalNumeral
	:	'0'
	|	[1-9] DecDigit*
	;

fragment BinaryNumeral
	:	'0' [bB] BinaryDigits
	;


// -----------------------------------
// Digits

fragment HexDigits		: HexDigit+		;
fragment DecDigits		: DecDigit+		;
fragment OctalDigits	: OctalDigit+	;
fragment BinaryDigits	: BinaryDigit+	;

fragment HexDigit		: [0-9a-fA-F]	;
fragment DecDigit		: [0-9]			;
fragment OctalDigit		: [0-7]			;
fragment BinaryDigit	: [01]			;


// -----------------------------------
// Literals

fragment BoolLiteral	: True | False								;

fragment CharLiteral	: SQuote ( EscSeq | ~['\r\n\\] )  SQuote	;
fragment SQuoteLiteral	: SQuote ( EscSeq | ~['\r\n\\] )* SQuote	;
fragment DQuoteLiteral	: DQuote ( EscSeq | ~["\r\n\\] )* DQuote	;
fragment USQuoteLiteral	: SQuote ( EscSeq | ~['\r\n\\] )* 			;

fragment DecimalFloatingPointLiteral
	:   DecDigits DOT DecDigits? ExponentPart? FloatTypeSuffix?
	|   DOT DecDigits ExponentPart? FloatTypeSuffix?
	|	DecDigits ExponentPart FloatTypeSuffix?
	|	DecDigits FloatTypeSuffix
	;

fragment ExponentPart
	:	[eE] [+-]? DecDigits
	;

fragment FloatTypeSuffix
	:	[fFdD]
	;

fragment HexadecimalFloatingPointLiteral
	:	HexSignificand BinaryExponent FloatTypeSuffix?
	;

fragment HexSignificand
	:   HexNumeral DOT?
	|   '0' [xX] HexDigits? DOT HexDigits
	;

fragment BinaryExponent
	:	[pP] [+-]? DecDigits
	;


// -----------------------------------
// Character ranges

fragment NameChar
	:	NameStartChar
	|	'0'..'9'
	|	Underscore
	|	'\u00B7'
	|	'\u0300'..'\u036F'
	|	'\u203F'..'\u2040'
	;

fragment NameStartChar
	:	'A'..'Z'
	|	'a'..'z'
	|	'\u00C0'..'\u00D6'
	|	'\u00D8'..'\u00F6'
	|	'\u00F8'..'\u02FF'
	|	'\u0370'..'\u037D'
	|	'\u037F'..'\u1FFF'
	|	'\u200C'..'\u200D'
	|	'\u2070'..'\u218F'
	|	'\u2C00'..'\u2FEF'
	|	'\u3001'..'\uD7FF'
	|	'\uF900'..'\uFDCF'
	|	'\uFDF0'..'\uFFFD'
	;	// ignores | ['\u10000-'\uEFFFF] ;


fragment JavaLetter
	:   [a-zA-Z$_] // "java letters" below 0xFF
	|	JavaUnicodeChars
	;

fragment JavaLetterOrDigit
	:   [a-zA-Z0-9$_] // "java letters or digits" below 0xFF
	|	JavaUnicodeChars
	;

// covers all characters above 0xFF which are not a surrogate
// and UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
fragment JavaUnicodeChars
	: ~[\u0000-\u00FF\uD800-\uDBFF]		{Character.isJavaIdentifierPart(_input.LA(-1))}?
	|  [\uD800-\uDBFF] [\uDC00-\uDFFF]	{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;


// -----------------------------------
// Types

fragment Boolean		: 'boolean'	;
fragment Byte			: 'byte'	;
fragment Short			: 'short'	;
fragment Int			: 'int'		;
fragment Long			: 'long'	;
fragment Char			: 'char'	;
fragment Float			: 'float'	;
fragment Double 		: 'double'	;

fragment True		 	: 'true'	;
fragment False			: 'false'	;


// -----------------------------------
// Symbols

fragment Esc			: '\\'	;
fragment Colon			: ':'	;
fragment DColon			: '::'	;
fragment SQuote			: '\''	;
fragment DQuote			: '"'	;
fragment BQuote			: '`'	;
fragment LParen			: '('	;
fragment RParen			: ')'	;
fragment LBrace			: '{'	;
fragment RBrace			: '}'	;
fragment LBrack			: '['	;
fragment RBrack			: ']'	;
fragment RArrow			: '->'	;
fragment Lt				: '<'	;
fragment Gt				: '>'	;
fragment Lte			: '<='	;
fragment Gte			: '>='	;
fragment Equal			: '='	;
fragment NotEqual		: '!='	;
fragment Question		: '?'	;
fragment Bang			: '!'	;
fragment Star			: '*'	;
fragment Slash			: '/'	;
fragment Percent		: '%'	;
fragment Caret			: '^'	;
fragment Plus			: '+'	;
fragment Minus			: '-'	;
fragment PlusAssign		: '+='	;
fragment MinusAssign	: '-='	;
fragment MulAssign		: '*='	;
fragment DivAssign		: '/='	;
fragment AndAssign		: '&='	;
fragment OrAssign		: '|='	;
fragment XOrAssign		: '^='	;
fragment ModAssign		: '%='	;
fragment LShiftAssign	: '<<='	;
fragment RShiftAssign	: '>>='	;
fragment URShiftAssign	: '>>>=';
fragment Underscore		: '_'	;
fragment Pipe			: '|'	;
fragment Amp			: '&'	;
fragment And			: '&&'	;
fragment Or				: '||'	;
fragment Inc			: '++'	;
fragment Dec			: '--'	;
fragment LShift			: '<<'	;
fragment RShift			: '>>'	;
fragment Dollar			: '$'	;
fragment Comma			: ','	;
fragment Semi			: ';'	;
fragment Dot			: '.'	;
fragment Range			: '..'	;
fragment Ellipsis		: '...'	;
fragment At				: '@'	;
fragment Pound			: '#'	;
fragment Tilde			: '~'	;
