grammar IssueQuery;

query
    : WS* (criteria|All) WS+ OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* (criteria|All) WS* EOF
    | WS* OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
    : Mine																			#MineCriteria
    | criteriaField=Quoted WS+ operator=(IsMe|IsNotMe|IsEmpty|IsNotEmpty) #UnaryCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsNot|IsGreaterThan|IsLessThan|IsBefore|IsAfter|Contains|DoesNotContain) WS+ criteriaValue=Quoted #ValueCriteria
    | criteria WS+ And WS+ criteria												#AndCriteria
    | criteria WS+ Or WS+ criteria												#OrCriteria
    | LParens WS* criteria WS* RParens														#BracedCriteria
    ;

order
	: orderField=Quoted WS* (WS+ direction=(Asc|Desc))?
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
	
And
	: 'and'
	;
	
Or
	: 'or'
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
