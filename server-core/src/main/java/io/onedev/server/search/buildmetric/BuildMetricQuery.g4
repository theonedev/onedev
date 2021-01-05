grammar BuildMetricQuery;

query
    : WS* criteria WS* EOF
    ;

criteria
	: operator=(BuildIsSuccessful|BuildIsFailed) #OperatorCriteria
    | criteriaField=Quoted WS+ operator=IsEmpty #FieldOperatorCriteria
    | operator=(Since|Until) WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=Is WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria	#AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria 
    | LParens WS* criteria WS* RParens #ParensCriteria
    ;

BuildIsSuccessful
	: 'build' WS+ 'is' WS+ 'successful'
	;
	
BuildIsFailed
	: 'build' WS+ 'is' WS+ 'failed'
	;	

Is
	: 'is'
	;
	
IsEmpty
	: 'is' WS+ 'empty'
	;	
	
Since
	: 'since'
	;

Until
	: 'until'
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
