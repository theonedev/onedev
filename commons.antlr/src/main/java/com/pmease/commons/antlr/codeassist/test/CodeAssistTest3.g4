grammar CodeAssistTest3;

query: criteria+ EOF;

criteria: titleCriteria|authorCriteria;

titleCriteria: 'title' ':' Value;
authorCriteria: 'author' ':' Value;

Value: QuotedValue|NQuotedValue;

QuotedValue: '"' (ESCAPE|~["\\])+? '"';
NQuotedValue: [a-zA-Z1-9]+;

fragment
ESCAPE: '\\'["\\];

WS: [ \t\r\n]+ -> skip;