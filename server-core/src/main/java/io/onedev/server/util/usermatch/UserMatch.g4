grammar UserMatch;

userMatch
    : WS* criteria (WS+ OR WS+ criteria)* (WS+ EXCEPT WS+ exceptCriteria (WS+ AND WS+ exceptCriteria)*)? WS* EOF
    ;

criteria
    : Anyone
    | userCriteria
    | groupCriteria
    ;
    
exceptCriteria
    : criteria
    ;

Anyone
    : 'anyone'
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
	: '(' ('\\'.|~[()\\])+? ')'
	;
