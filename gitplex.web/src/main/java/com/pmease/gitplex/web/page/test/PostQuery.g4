grammar PostQuery;

query: criteria+ EOF;

criteria: key=('title'|'author') ':' value;

value: QuotedValue|NQuotedValue;

QuotedValue: '"' (ESCAPE|~["\\])+? '"';
NQuotedValue: [a-zA-Z1-9_]+;

fragment
ESCAPE: '\\'["\\];

WS: [ \t\r\n]+ -> skip;