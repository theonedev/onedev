grammar UserMatcher;

userMatcher
    : WS* criteria (WS+ OR WS+ criteria)* (WS+ EXCEPT WS+ exceptCriteria (WS+ AND WS+ exceptCriteria)*)? WS* EOF
    ;

criteria
    : Anyone
    | ProjectAdministrators
    | CodeWriters
    | CodeReaders
    | IssueReaders
    | userCriteria
    | groupCriteria
    ;
    
exceptCriteria
    : criteria
    ;

Anyone
    : 'anyone'
    ;

ProjectAdministrators
    : 'project' WS+ 'administrators'
    ;

CodeWriters
    : 'code' WS+ 'writers'
    ;

CodeReaders
    : 'code' WS+ 'readers'
    ;

IssueReaders
    : 'issue' WS+ 'readers'
    ;

userCriteria
    : USER Value
    ;

groupCriteria
    : GROUP Value
    ;

AND
    : 'and'
    ;

OR
    : 'or'
    ;
    
USER
	: 'user'
	;
	
GROUP
	: 'group'
	;	   

EXCEPT
    : 'except'
    ;

WS
    : ' '
    ;

Value
	: '(' (ESCAPE|~[()\\])+? ')'
	;

fragment

ESCAPE
	: '\\'[()\\]
	;
