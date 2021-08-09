grammar JobMatch;

jobMatch
    : WS* (criteria|All) WS* EOF
    ;

criteria
	: operator=OnBranch WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=Is WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria	#AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria 
    | LParens WS* criteria WS* RParens #ParensCriteria
    ;

All
	: 'all'
	;

OnBranch
	: 'on' WS+ 'branch'
	;
	
Is
	: 'is'
	;
		
And
	: 'and'
	;

Or
	: 'or'
	;
	
Not
	: 'not'
	;

LParens
	: '('
	;

RParens
	: ')'
	;

Quoted
    : '"' ('\\'.|~[\\"])+? '"'
    ;

WS
    : ' '
    ;

Identifier
	: [a-zA-Z0-9:_/\\+\-;]+
	;    
