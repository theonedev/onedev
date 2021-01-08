grammar Interpolative;

interpolative: segment* EOF;

segment: Literal|Variable;

Variable: '@' VariableChar+? '@';
Literal: LiteralChar+;
LiteralChar: '@@'|~[@];
VariableChar: ~[@];