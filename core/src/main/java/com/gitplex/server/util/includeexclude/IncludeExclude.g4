grammar IncludeExclude;

match: ' '* criteria (' '+ criteria)* ' '* EOF;

criteria: includeMatch | excludeMatch;

includeMatch: INCLUDE Value;
excludeMatch: EXCLUDE Value;

INCLUDE: 'include';
EXCLUDE: 'exclude';

Value: '(' (ESCAPE|~[()\\])+? ')';

fragment
ESCAPE: '\\'[()\\];
