grammar PatternSet;

patterns: WS* pattern (WS+ pattern)* WS* EOF;

pattern: Excluded? (Quoted|NQuoted);

Quoted: '"' (ESCAPE|~["\\])+? '"';
NQuoted: ~[-" ]~[" ]*;
Excluded: '-';

WS: ' ';

fragment
ESCAPE: '\\'["\\];
