grammar CodeCommentQuery;

query
    : WS* (criteria|All) WS* (WS OrderBy WS+ order (WS+ And WS+ order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
	: CreatedByMe #OperatorCriteria
    | CreatedBy WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsBefore|IsAfter|Contains) WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
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

CreatedByMe
	: 'created by me'
	;
	
CreatedBy
	: 'created by'
	;
	
OrderBy
    : 'order by'
    ;

Is
	: 'is'
	;

Contains
	: 'contains'
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
	: [a-zA-Z0-9:_/\\+-;]+
	;    

fragment
ESCAPE
    : '\\'["\\]
    ;
