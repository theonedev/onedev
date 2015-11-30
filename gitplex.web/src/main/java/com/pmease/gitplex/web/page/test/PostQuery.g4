grammar PostQuery;

query: (criteria|sentence)+ EOF;
criteria: Criteria|ExcludedCriteria;
sentence: Word+;

Criteria: TitleCriteria|AuthorCriteria|IsCriteria;
TitleCriteria: 'title:' (Word|QuotedValue);
AuthorCriteria: 'author:' (Word|QuotedValue);
IsCriteria: 'is:' ('open'|'close');
ExcludedCriteria: '-' Criteria;
QuotedValue: '"' (ESCAPE|~["\\])+ '"';
Word: ~[ \t\r\n]+;

fragment
ESCAPE: '\\'["\\];

WS: [ \t\r\n]+ -> skip;