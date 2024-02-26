grammar JobMatch;

jobMatch
    : WS* criteria WS* EOF
    ;

criteria
    : NonPullRequestCommits #OperatorCriteria
	| operator=(OnBranch|SubmittedByUser|SubmittedByGroup) WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsNot) WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria	#AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria 
    | LParens WS* criteria WS* RParens #ParensCriteria
    ;

NonPullRequestCommits
    : 'non-pull-request' WS+ 'commits'
    ;

OnBranch
	: 'on' WS+ 'branch'
	;

SubmittedByUser
    : 'submitted' WS+ 'by' WS+ 'user'
    ;

SubmittedByGroup
    : 'submitted' WS+ 'by' WS+ 'group'
    ;

Is
	: 'is'
	;

IsNot
    : 'is' WS+ 'not'
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
