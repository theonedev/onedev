grammar PatternSet;

patterns: WS* pattern (WS+ pattern)* WS* EOF;

pattern: Excluded? (Quoted|NQuoted);

Quoted: '"' (ESCAPE|~["])+? '"';
NQuoted: (~[-" ]|ESCAPE)(~[" ]|ESCAPE)*;
Excluded: '-';

WS: ' ';

fragment
ESCAPE: '\\"';
