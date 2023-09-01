grammar IssueQuery;

query
    : WS* criteria WS* (WS OrderBy WS+ order (WS+ And WS+ order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
    : operator=(Confidential|SubmittedByMe|WatchedByMe|CommentedByMe|MentionedMe|FixedInCurrentCommit|FixedInCurrentBuild|FixedInCurrentPullRequest|CurrentIssue) #OperatorCriteria
    | operator=(SubmittedBy|WatchedBy|CommentedBy|Mentioned|FixedInCommit|FixedInBuild|FixedInPullRequest|HasAny) WS+ criteriaValue=Quoted #OperatorValueCriteria
    | FixedBetween WS+ revisionCriteria WS+ And WS+ revisionCriteria #FixedBetweenCriteria
    | criteriaField=Quoted WS+ operator=(IsMe|IsEmpty|IsCurrent|IsPrevious) #FieldOperatorCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsGreaterThan|IsLessThan|IsUntil|IsSince|IsAfter|IsBefore|Contains) WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | Hash? number=Number #NumberCriteria
    | criteria WS+ And WS+ criteria #AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria
    | scope=(Any|All) WS+ linkSpec=Quoted WS+ Matching WS* LParens WS* criteria WS* RParens #LinkMatchCriteria
    | LParens WS* criteria WS* RParens #ParensCriteria
    | Fuzzy #FuzzyCriteria
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

WatchedBy
    : 'watched' WS+ 'by'
    ;

CommentedBy
    : 'commented' WS+ 'by'
    ;

Mentioned
    : 'mentioned'
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

WatchedByMe
    : 'watched' WS+ 'by' WS+ 'me'
    ;

CommentedByMe
    : 'commented' WS+ 'by' WS+ 'me'
    ;

MentionedMe
    : 'mentioned' WS+ 'me'
    ;

Confidential
	: 'confidential'
	;

CurrentIssue
	: 'current' WS+ 'issue'
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

IsAfter
	: 'is' WS+ 'after'
	;

IsBefore
	: 'is' WS+ 'before'
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

Any
	: 'any'
	;

All
	: 'all'
	;

HasAny
	: 'has' WS+ 'any'
	;

Matching
    : 'matching'
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

Hash
    : '#'
    ;

Quoted
    : '"' ('\\'.|~[\\"])+? '"'
    ;

Number
    : [0-9]+
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
