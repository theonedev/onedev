grammar ReviewAppointment;

expr: ' '* criteria (' '+ criteria)* ' '* EOF;

criteria: userCriteria | teamCriteria;

userCriteria: USER Value;
teamCriteria: TEAM Value (':' count)?;

count: 'all' | DIGIT;

DIGIT: [1-9][0-9]*;

USER: 'user';
TEAM: 'team';

Value: '(' (ESCAPE|~[()\\])+? ')';

fragment
ESCAPE: '\\'[()\\];
