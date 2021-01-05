grammar PullRequestQuery;

query
    : WS* criteria WS* (WS OrderBy WS+ order (WS+ And WS+ order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
	: operator=(Open|Merged|Discarded|AssignedToMe|SubmittedByMe|ToBeReviewedByMe|RequestedForChangesByMe|ApprovedByMe|DiscardedByMe|SomeoneRequestedForChanges|HasPendingReviews|HasFailedBuilds|ToBeVerifiedByBuilds|HasMergeConflicts) #OperatorCriteria
    | operator=(ToBeReviewedBy|AssignedTo|ApprovedBy|RequestedForChangesBy|SubmittedBy|DiscardedBy|IncludesCommit|IncludesIssue) WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsGreaterThan|IsLessThan|IsUntil|IsSince|Contains) WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria	#AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria 
    | LParens WS* criteria WS* RParens #ParensCriteria
    ;

order
	: orderField=Quoted WS* (WS+ direction=(Asc|Desc))?
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

AssignedToMe
	: 'assigned' WS+ 'to' WS+ 'me'
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

ToBeVerifiedByBuilds
    : 'to' WS+ 'be' WS+ 'verified' WS+ 'by' WS+ 'builds'
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

AssignedTo
	: 'assigned' WS+ 'to'
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
    
IncludesCommit
	: 'includes' WS+ 'commit'
	;    
    
IncludesIssue
	: 'includes' WS+ 'issue'
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

WS
    : ' '
    ;
    
Identifier
	: [a-zA-Z0-9:_/\\+\-;]+
	;    
