grammar CodeCommentQuery;

query
    : WS* criteria WS* (WS OrderBy WS+ order (WS+ And WS+ order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
	: operator=CreatedByMe #OperatorCriteria
    | operator=(CreatedBy|OnCommit) WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsUntil|IsSince|IsGreaterThan|IsLessThan|Contains) WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria	#AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria 
    | LParens WS* criteria WS* RParens #ParensCriteria
    ;

order
	: orderField=Quoted WS* (WS+ direction=(Asc|Desc))?
	;

CreatedByMe
	: 'created' WS+ 'by' WS+ 'me'
	;
	
CreatedBy
	: 'created' WS+ 'by'
	;
	
OnCommit
	: 'on' WS+ 'commit'
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

IsSince
	: 'is' WS+ 'since'
	;

IsUntil
	: 'is' WS+ 'until'
	;
	
IsGreaterThan
	: 'is' WS+ 'greater' WS+ 'than'
	;
	
IsLessThan
	: 'is' WS+ 'less' WS+ 'than'
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
