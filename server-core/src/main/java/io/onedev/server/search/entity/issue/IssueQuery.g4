grammar IssueQuery;

query
    : WS* criteria WS* (WS OrderBy WS+ order (WS+ And WS+ order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
    : operator=(SubmittedByMe|FixedInCurrentCommit|FixedInCurrentBuild|FixedInCurrentPullRequest) #OperatorCriteria
    | operator=(SubmittedBy|FixedInCommit|FixedInBuild|FixedInPullRequest) WS+ criteriaValue=Quoted #OperatorValueCriteria
    | FixedBetween WS+ revisionCriteria WS+ And WS+ revisionCriteria #FixedBetweenCriteria
    | criteriaField=Quoted WS+ operator=(IsMe|IsEmpty|IsCurrent|IsPrevious) #FieldOperatorCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsGreaterThan|IsLessThan|IsUntil|IsSince|Contains) WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria #AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria 
    | LParens WS* criteria WS* RParens #ParensCriteria
    ;

revisionCriteria
	: revisionType=(Build|Branch|Tag|Commit) WS+ revisionValue=Quoted 
	;
	
order
	: orderField=Quoted WS* (WS+ direction=(Asc|Desc))?
	;

OrderBy
    : 'order' WS+ 'by'
    ;

SubmittedBy
	: 'submitted' WS+ 'by'
	;
	
FixedInCommit
	: 'fixed' WS+ 'in' WS+ 'commit'
	;							

FixedInCurrentCommit
	: 'fixed' WS+ 'in' WS+ 'current' WS+ 'commit'
	;
	
FixedInBuild
	: 'fixed' WS+ 'in' WS+ 'build'
	;							

FixedInCurrentBuild
	: 'fixed' WS+ 'in' WS+ 'current' WS+ 'build'
	;

FixedInPullRequest
	: 'fixed' WS+ 'in' WS+ 'pull' WS+ 'request'
	;							

FixedInCurrentPullRequest
	: 'fixed' WS+ 'in' WS+ 'current' WS+ 'pull' WS+ 'request'
	;

IsCurrent
	: 'is' WS+ 'current' 
	;
	
IsPrevious
	: 'is' WS+ 'previous'
	;
			
FixedBetween
	: 'fixed' WS+ 'between' 
	;

SubmittedByMe
	: 'submitted' WS+ 'by' WS+ 'me'
	;
		
Is
	: 'is'
	;

IsMe
	: 'is' WS+  'me'
	;
	
Contains
	: 'contains'
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

IsEmpty
	: 'is' WS+ 'empty'
	;

Build
	: 'build'
	;
	
Branch
	: 'branch'
	;
	
Tag
	: 'tag'
	;
	
Commit
	: 'commit'
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
