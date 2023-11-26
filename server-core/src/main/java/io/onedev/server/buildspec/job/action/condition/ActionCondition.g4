grammar ActionCondition;

condition
    : WS* (criteria|Always) WS* EOF
    ;

criteria
    : operator=(Successful|Failed|Cancelled|TimedOut|PreviousIsSuccessful|PreviousIsFailed|PreviousIsCancelled|PreviousIsTimedOut) #OperatorCriteria
    | criteriaField=Quoted WS+ operator=(IsEmpty|IsNotEmpty) #FieldOperatorCriteria
    | operator=OnBranch WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Contains|Is|IsNot) WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria	#AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria
    | LParens WS* criteria WS* RParens #ParensCriteria
    ;

Always
	: 'always'
	;
	
Successful
	: 'successful'
	;
	
Failed
	: 'failed'
	;
	
Cancelled
	: 'cancelled'
	;
	
TimedOut
	: 'timed' WS+ 'out'
	;
	
PreviousIsSuccessful
	: 'previous' WS+ 'is' WS+ 'successful'
	;

PreviousIsFailed
	: 'previous' WS+ 'is' WS+ 'failed'
	;

PreviousIsNotFailed
	: 'previous' WS+ 'is' WS+ 'failed'
	;

PreviousIsCancelled
	: 'previous' WS+ 'is' WS+ 'cancelled'
	;
	
PreviousIsTimedOut
	: 'previous' WS+ 'is' WS+ 'timed' WS+ 'out'
	;
	
OnBranch
	: 'on' WS+ 'branch'
	;
	
Contains
	: 'contains'
	;
	
Is
	: 'is'
	;

IsNot
    : 'is' WS+ 'not'
    ;
	
IsEmpty
	: 'is' WS+ 'empty'
	;

IsNotEmpty
	: 'is' WS+ 'not' WS+ 'empty'
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
