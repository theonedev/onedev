grammar IssueQuery;

query
    : WS* criteria WS+ OrderBy WS+ order (WS+ 'and' WS+ order)* WS* EOF
    | WS* criteria WS* EOF
    | WS* OrderBy WS+ order (WS+ 'and' WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
    : All																			#AllCriteria
    | Mine																			#MineCriteria
    | criteriaField=Quoted WS+ operator=(IsMe|IsNotMe|IsEmpty|IsNotEmpty) #UnaryCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsNot|IsGreaterThan|IsLessThan|IsBefore|IsAfter|Contains|DoesNotContain) WS+ criteriaValue=Quoted #ValueCriteria
    | criteria WS+ 'and' WS+ criteria												#AndCriteria
    | criteria WS+ 'or' WS+ criteria												#OrCriteria
    | '(' WS* criteria WS* ')'														#BracedCriteria
    ;

order
	: orderField=Quoted WS* (WS+ direction=('asc'|'desc'))?
	;

Mine
	: 'mine'
	;
	
All
	: 'all'
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
	
IsMe
	: 'is' WS+  'me'
	;
	
IsNotMe
	: 'is' WS+ 'not' WS+ 'me'
	;		

Contains
	: 'contains'
	;

DoesNotContain
	: 'does' WS+ 'not' WS+ 'contain'
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

IsEmpty
	: 'is' WS+ 'empty'
	;

IsNotEmpty
	: 'is' WS+ 'not' WS+ 'empty'
	;

Quoted
    : '"' (ESCAPE|~["\\])+? '"'
    ;

/*
 * Use identifier to make the input "isnot" a whole token so that it becomes a "matchWith" 
 * to provide suggestion for "is not"   
 */
Identifier
	: [a-zA-Z]+
	;
	
WS
    : ' '
    ;

fragment
ESCAPE
    : '\\'["\\]
    ;
