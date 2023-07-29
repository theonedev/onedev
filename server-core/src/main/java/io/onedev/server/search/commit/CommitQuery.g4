grammar CommitQuery;

query
	: WS* criteria (WS+ criteria)* WS* EOF
	| WS* EOF
	;

criteria
	: revisionCriteria 
	| beforeCriteria 
	| afterCriteria 
	| committerCriteria 
	| authorCriteria 
	| pathCriteria
	| messageCriteria
	| fuzzyCriteria
	;

revisionCriteria
	: ((UNTIL|SINCE) WS+)? ((BRANCH|TAG|COMMIT|BUILD) Value | DefaultBranch)
	;
	
beforeCriteria
	: BEFORE Value
	;

afterCriteria
	: AFTER Value
	;

committerCriteria
	: COMMITTER Value
	| CommittedByMe
	;

authorCriteria
	: AUTHOR Value
	| AuthoredByMe
	;

pathCriteria
	: PATH Value
	;

messageCriteria
	: MESSAGE Value
	;

SINCE
	: 'since'
	;
	
UNTIL
	: 'until'
	;
	
BRANCH
	: 'branch'
	;
	
TAG
	: 'tag'
	;
	
COMMIT
	: 'commit'
	;
	
BUILD
	: 'build'
	;
		
BEFORE
	: 'before'
	;
	
AFTER
	: 'after'
	;
	
COMMITTER
	: 'committer'
	;
	
AUTHOR
	: 'author'
	;
	
PATH
	: 'path'
	;
	
MESSAGE
	: 'message'
	;
	
AuthoredByMe
	: 'authored-by-me'
	;		
	
CommittedByMe
	: 'committed-by-me'
	;

DefaultBranch
	: 'default-branch'
	;

WS
	: ' '
	;

Value
	: '(' ('\\'.|~[\\()])+? ')'
	;

fuzzyCriteria
    : Fuzzy
    ;

Fuzzy
    : '~' ('\\'.|~[~])+? '~'
    ;

Identifier
	: [a-zA-Z0-9:_/\\+\-;]+
	;
