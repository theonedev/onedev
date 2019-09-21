grammar InterpolativePatternSet;

interpolative: segment* EOF;
segment: literal|Variable;
literal: WS* pattern (WS+ pattern)* WS*;
pattern: Excluded? (Quoted|NQuoted);

Variable: '@' (AT_ESCAPE|~[@])+? '@';
Quoted: '"' (QUOTE_ESCAPE|AT_ESCAPE|~["@])+? '"';
NQuoted: (~[-"@ ]|QUOTE_ESCAPE|AT_ESCAPE)(~["@ ]|QUOTE_ESCAPE|AT_ESCAPE)*;
Excluded: '-';

WS: ' ';

fragment
QUOTE_ESCAPE: '\\"';
AT_ESCAPE: '\\@';
