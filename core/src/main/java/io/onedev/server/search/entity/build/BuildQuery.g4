grammar BuildQuery;

query
    : WS* (criteria|All) WS* (WS OrderBy WS+ order (WS+ And WS+ order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
	: operator=(Successful|Failed|InError|Cancelled|Running|Waiting|Queueing) #OperatorCriteria
	| operator=FixedIssue WS+ criteriaValue=Quoted #OperatorValueCriteria
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
		
Running
	: 'running'
	;
	
Waiting
	: 'waiting'
	;
	
Queueing
	: 'queueing'
	;		
				
FixedIssue
	: 'fixed' WS+ 'issue'
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
    : '"' (ESCAPE|~["\\])+? '"'
    ;

WS
    : ' '
    ;

Identifier
	: [a-zA-Z0-9:_/\\+\-;]+
	;    

fragment
ESCAPE
    : '\\'["\\]
    ;
