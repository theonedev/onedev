grammar Interpolative;

interpolative: segment* EOF;

segment: Literal|Variable;

Variable: '@' (ESCAPE|~[@])+? '@';
Literal: (ESCAPE|~[@])+;

fragment
ESCAPE: '\\@';
