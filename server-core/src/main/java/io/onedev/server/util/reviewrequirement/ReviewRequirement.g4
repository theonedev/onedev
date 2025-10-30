grammar ReviewRequirement;

requirement: WS* criteria (WS+ criteria)* WS* EOF;

criteria: userCriteria | groupCriteria;

userCriteria: USER WS* Value;
groupCriteria: GROUP WS* Value (WS*':' WS* DIGIT)?;

DIGIT: [1-9][0-9]*;

USER: 'user';
GROUP: 'group';

WS: ' ';

Value: '(' ('\\'.|~[\\()])+? ')';
