grammar IssueQuery;

query
    : WS* (criteria|All) WS+ OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* (criteria|All) WS* EOF
    | WS* OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
    : operator=(Mine|Open|Closed|SubmittedByMe)	#OperatorCriteria
    | SubmittedBy WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(IsMe|IsEmpty) #FieldOperatorCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsGreaterThan|IsLessThan|IsBefore|IsAfter|Contains) WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria #AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria 
    | LParens WS* criteria WS* RParens #ParensCriteria
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
	
Open
	: 'open'
	;
	
Closed
	: 'closed'
	;	
	
OrderBy
    : 'order' WS+ 'by'
    ;

SubmittedBy
	: 'submitted' WS+ 'by'
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

IsAfter
	: 'is' WS+ 'after'
	;

IsBefore
	: 'is' WS+ 'before'
	;

IsEmpty
	: 'is' WS+ 'empty'
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
