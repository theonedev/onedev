grammar ProjectQuery;

query
    : WS* (criteria|All) WS* (WS OrderBy WS+ order (WS+ And WS+ order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
    : operator=ForksOf WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=IsMe #FieldOperatorCriteria
    | criteriaField=Quoted WS+ operator=(Is|Contains) WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria #AndCriteria
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

OrderBy
    : 'order' WS+ 'by'
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

ForksOf
	: 'forks' WS+ 'of'
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
