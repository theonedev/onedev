grammar InterpolativePatternSet;

interpolative: segment* EOF;
segment: literal|Variable|WS+;
literal: pattern (WS+ pattern)*;
pattern: Excluded? (Quoted|NQuoted);

Variable: '@' (~[@])+? '@';
Quoted: '"' ('@@'|'\\'~[@]|~[\\"@])+? '"';
NQuoted: ('@@'|~[-\\"@ ]|'\\'~[@])('@@'|~["\\@ ]|'\\'~[@])*;
Excluded: '-';

WS: ' ';
