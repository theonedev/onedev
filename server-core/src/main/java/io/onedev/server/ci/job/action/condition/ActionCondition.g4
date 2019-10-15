grammar ActionCondition;

condition
    : WS* (criteria|Always) WS* EOF
    ;

criteria
    : operator=(Successful|Failed|InError|Cancelled|TimedOut|PreviousIsSuccessful|PreviousIsFailed|PreviousIsInError|PreviousIsCancelled|PreviousIsTimedOut|WillRetry|AssociatedWithPullRequests|RequiredByPullRequests) #OperatorCriteria
    | operator=OnBranch WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=Is WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
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
	
InError
	: 'in' WS+ 'error'
	;
	
Cancelled
	: 'cancelled'
	;
	
TimedOut
	: 'timed' WS+ 'out'
	;
	
PreviousIsSuccessful
	: 'previous' WS+  'is' WS+ 'successful'
	;
	
PreviousIsFailed
	: 'previous' WS+ 'is' WS+ 'failed'
	;
	
PreviousIsInError
	: 'previous' WS+ 'is' WS+ 'in' WS+ 'error'
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
	
WillRetry
	: 'will' WS+ 'retry'
	;
	
AssociatedWithPullRequests
	: 'associated' WS+ 'with' WS+ 'pull' WS+ 'requests'
	;
	
RequiredByPullRequests
	: 'required' WS+ 'by' WS+ 'pull' WS+ 'requests'
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
