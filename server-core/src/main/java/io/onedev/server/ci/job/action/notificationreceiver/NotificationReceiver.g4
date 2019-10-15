grammar NotificationReceiver;

receiver: WS* criteria (WS+ 'and' WS+ criteria)* WS* EOF;

criteria: userCriteria | groupCriteria | Committers | Authors | Submitter | AuthorsSincePreviousSuccessful | CommittersSincePreviousSuccessful;

userCriteria: USER Value;
groupCriteria: GROUP Value;

Committers: 'committers';

CommittersSincePreviousSuccessful: 'committers-since-previous-successful';

Authors: 'authors';

AuthorsSincePreviousSuccessful: 'authors-since-previous-successful';

Submitter: 'submitter';

USER: 'user';
GROUP: 'group';

WS: ' ';

Value: '(' ('\\'.|~[\\()])+? ')';

Identifier
	: [a-zA-Z0-9:_/\\+\-;]+
	;    
