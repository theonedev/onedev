grammar PullRequestQuery;

query
    : WS* (criteria|All) WS* (WS OrderBy WS+ order (WS+ And WS+ order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
	: operator=(Open|Merged|Discarded|SubmittedByMe|ToBeReviewedByMe|RequestedForChangesByMe|ApprovedByMe|DiscardedByMe|SomeoneRequestedForChanges|HasPendingReviews|HasFailedBuilds|HasPendingBuilds|HasMergeConflicts) #OperatorCriteria
    | operator=(ToBeReviewedBy|ApprovedBy|RequestedForChangesBy|SubmittedBy|DiscardedBy) WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsGreaterThan|IsLessThan|IsBefore|IsAfter|Contains) WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
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

Open
	: 'open'
	;

Merged
    : 'merged'
    ;

Discarded
    : 'discarded'
    ;

ToBeReviewedByMe
    : 'to' WS+ 'be' WS+ 'reviewed' WS+ 'by' WS+ 'me'
    ;

RequestedForChangesByMe
    : 'requested' WS+ 'for' WS+ 'changes' WS+ 'by' WS+ 'me'
    ;

ApprovedByMe
    : 'approved' WS+ 'by' WS+ 'me'
    ;

SubmittedByMe
	: 'submitted' WS+ 'by' WS+ 'me'
	;
	
DiscardedByMe
    : 'discarded' WS+ 'by' WS+ 'me'
    ;
    
SomeoneRequestedForChanges
    : 'someone' WS+ 'requested' WS+ 'for' WS+ 'changes'
    ;

HasPendingReviews
    : 'has' WS+ 'pending' WS+ 'reviews'
    ;

HasFailedBuilds
    : 'has' WS+ 'failed' WS+ 'builds'
    ;

HasPendingBuilds
    : 'has' WS+ 'pending' WS+ 'builds'
    ;

HasMergeConflicts
    : 'has' WS+ 'merge' WS+ 'conflicts'
    ;

ToBeReviewedBy
    : 'to' WS+ 'be' WS+ 'reviewed' WS+ 'by'
    ;

RequestedForChangesBy
    : 'requested' WS+ 'for' WS+ 'changes' WS+ 'by'
    ;

ApprovedBy
    : 'approved' WS+ 'by'
    ;

SubmittedBy
	: 'submitted' WS+ 'by'
	;
	
DiscardedBy
    : 'discarded' WS+ 'by'
    ;
    
OrderBy
    : 'order' WS+ 'by'
    ;

Is
	: 'is'
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

IsAfter
	: 'is' WS+ 'after'
	;

IsBefore
	: 'is' WS+ 'before'
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
	: [a-zA-Z0-9:_/\\+-;]+
	;    

fragment
ESCAPE
    : '\\'["\\]
    ;
