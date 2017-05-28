grammar ReviewAppointment;

expr: ' '* criteria (' '+ criteria)* ' '* EOF;

criteria: userCriteria | groupCriteria;

userCriteria: USER Value;
groupCriteria: GROUP Value (':' count)?;

count: 'all' | DIGIT;

DIGIT: [1-9][0-9]*;

USER: 'user';
GROUP: 'group';

Value: '(' (ESCAPE|~[()\\])+? ')';

fragment
ESCAPE: '\\'[()\\];
