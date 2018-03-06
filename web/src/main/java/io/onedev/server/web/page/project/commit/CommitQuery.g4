grammar CommitQuery;

query
	: ' '* criteria (' '+ criteria)* ' '* EOF
	;

criteria
	: fuzzyCriteria 
	| revisionCriteria 
	| beforeCriteria 
	| afterCriteria 
	| committerCriteria 
	| authorCriteria 
	| pathCriteria
	| messageCriteria
	;

fuzzyCriteria
	: ((UNTIL|SINCE) ' '+)? FuzzyValue
	;
	
revisionCriteria
	: ((UNTIL|SINCE) ' '+)? (BRANCH|TAG|REVISION) Value
	;
	
beforeCriteria: BEFORE Value;

afterCriteria: AFTER Value;

committerCriteria: COMMITTER Value;

authorCriteria: AUTHOR Value;

pathCriteria: PATH Value;

messageCriteria: MESSAGE Value;

SINCE: 'since';
UNTIL: 'until';
BRANCH: 'branch';
TAG: 'tag';
REVISION: 'revision';
BEFORE: 'before';
AFTER: 'after';
COMMITTER: 'committer';
AUTHOR: 'author';
PATH: 'path';
MESSAGE: 'message';

Value: '(' (ESCAPE|~[()\\])+? ')';
FuzzyValue: ~[ ()]+;

fragment
ESCAPE: '\\'[()\\];
