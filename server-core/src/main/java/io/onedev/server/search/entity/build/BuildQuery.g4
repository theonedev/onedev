grammar BuildQuery;

query
    : WS* criteria WS* (WS OrderBy WS+ order (WS* orderOperator=Comma WS* order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS* orderOperator=Comma WS* order)* WS* EOF
    | WS* EOF
    ;

criteria
	: operator=(Successful|Failed|Cancelled|Running|Finished|Waiting|Pending|TimedOut|SubmittedByMe|CancelledByMe) #OperatorCriteria
    | criteriaField=Quoted WS+ operator=(IsEmpty|IsNotEmpty) #FieldOperatorCriteria
	| operator=(FixedIssue|SubmittedBy|CancelledBy|DependsOn|DependenciesOf|RanOn) WS+ criteriaValue=multipleQuoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsNot|IsGreaterThan|IsLessThan|IsUntil|IsSince) WS+ criteriaValue=multipleQuoted #FieldOperatorValueCriteria
    | Reference #ReferenceCriteria
    | criteria WS+ And WS+ criteria	#AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria 
    | LParens WS* criteria WS* RParens #ParensCriteria
    | Fuzzy #FuzzyCriteria
    ;

order
	: orderField=Quoted WS* (WS+ direction=(Asc|Desc))?
	;

multipleQuoted
    : Quoted(WS* Comma WS* Quoted)*
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

Finished
	: 'finished'
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

RanOn
	: 'ran' WS+ 'on'
	;
			
FixedIssue
	: 'fixed' WS+ 'issue'
	;
	
OrderBy
    : 'order' WS+ 'by'
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

IsGreaterThan
	: 'is' WS+ 'greater' WS+ 'than'
	;

IsLessThan
	: 'is' WS+ 'less' WS+ 'than'
	;
	
IsSince
	: 'is' WS+ 'since'
	;

IsUntil
	: 'is' WS+ 'until'
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

Reference
    : ([a-zA-Z0-9_]([a-zA-Z0-9_\-/.]*[a-zA-Z0-9_])?)? '#' [0-9]+ | [A-Z][A-Z]+ '-' [0-9]+
    ;

Comma
	: ','
	;

WS
    : ' '
    ;

Fuzzy
    : '~' ('\\'.|~[~])+? '~'
    ;

Identifier
	: [a-zA-Z0-9:_/\\+\-;]+
	;    
