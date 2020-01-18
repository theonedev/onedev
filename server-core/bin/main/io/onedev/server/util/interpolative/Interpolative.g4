grammar Interpolative;

interpolative: segment* EOF;

segment: Literal|Variable;

Variable: '@' CHAR+? '@';
Literal: CHAR+;
CHAR: ('\\'.|~[@\\]);
