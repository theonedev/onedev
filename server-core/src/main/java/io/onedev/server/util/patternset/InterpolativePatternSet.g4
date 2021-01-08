grammar InterpolativePatternSet;

interpolative: segment* EOF;
segment: literal|Variable;
literal: WS* pattern (WS+ pattern)* WS*;
pattern: Excluded? (Quoted|NQuoted);

Variable: '@' (~[@])+? '@';
Quoted: '"' ('@@'|'\\'~[@]|~[\\"@])+? '"';
NQuoted: ('@@'|~[-\\"@ ]|'\\'~[@])('@@'|~["\\@ ]|'\\'~[@])*;
Excluded: '-';

WS: ' ';
