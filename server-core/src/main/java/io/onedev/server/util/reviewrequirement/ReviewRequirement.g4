grammar ReviewRequirement;

requirement: WS* criteria (WS+ 'and' WS+ criteria)* WS* EOF;

criteria: userCriteria | groupCriteria;

userCriteria: USER Value;
groupCriteria: GROUP Value (':' count)?;

count: 'all' | DIGIT;

DIGIT: [1-9][0-9]*;

USER: 'user';
GROUP: 'group';

WS: ' ';

Value: '(' ('\\'.|~[\\()])+? ')';
