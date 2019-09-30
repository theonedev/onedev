grammar BuildQuery;

query
    : WS* (criteria|All) WS* (WS OrderBy WS+ order (WS+ And WS+ order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
	: operator=(Successful|Failed|InError|Cancelled|Running|Waiting|Pending|TimedOut|SubmittedByMe|CancelledByMe) #OperatorCriteria
	| operator=(FixedIssue|SubmittedBy|CancelledBy|DependsOn|DependenciesOf|RequiredByPullRequest) WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsGreaterThan|IsLessThan|IsBefore|IsAfter) WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria	#AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria 
    | LParens WS* criteria WS* RParens #ParensCriteria
    ;

order
	: orderField=Quoted WS* (WS+ direction=(Asc|Desc))?
	;

All
	: 'all'
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
		
Running
	: 'running'
	;
	
Waiting
	: 'waiting'
	;
	
Pending
	: 'pending'
	;		
				
SubmittedByMe
	: 'submitted' WS+ 'by' WS+ 'me'
	;
	
SubmittedBy
	: 'submitted' WS+ 'by'
	;
						
CancelledByMe
	: 'cancelled' WS+ 'by' WS+ 'me'
	;
	
CancelledBy
	: 'cancelled' WS+ 'by'
	;
	
DependsOn
	: 'depends' WS+ 'on'
	;
	
DependenciesOf
	: 'dependencies' WS+ 'of'
	;
			
FixedIssue
	: 'fixed' WS+ 'issue'
	;
	
RequiredByPullRequest
	: 'required' WS+ 'by' WS+ 'pull' WS+ 'request'
	;
	
OrderBy
    : 'order by'
    ;

Is
	: 'is'
	;
	
IsGreaterThan
	: 'is' WS+ 'greater' WS+ 'than'
	;

IsLessThan
	: 'is' WS+ 'less' WS+ 'than'
	;
	
IsAfter
	: 'is after'
	;

IsBefore
	: 'is before'
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

Asc
	: 'asc'
	;

Desc
	: 'desc'
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
