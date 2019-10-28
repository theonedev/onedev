grammar RetryCondition;

condition
    : WS* (criteria|Never) WS* EOF
    ;

criteria
    : operator=(Failed|InError|Cancelled|TimedOut) #OperatorCriteria
    | criteriaField=Quoted WS+ operator=Contains WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria	#AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria
    | LParens WS* criteria WS* RParens #ParensCriteria
    ;
	
Never
	: 'never'
	;
	
Failed
	: 'failed'
	;
	
InError
	: 'in' WS+ 'error'
	;
	
Cancelled
	: 'cancelled'
	;
	
TimedOut
	: 'timed' WS+ 'out'
	;
	
Contains
	: 'contains'
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
