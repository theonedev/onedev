grammar WorkspaceQuery;

query
    : WS* criteria WS* (WS OrderBy WS+ order (WS* orderOperator=Comma WS* order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS* orderOperator=Comma WS* order)* WS* EOF
    | WS* EOF
    ;

criteria
	: operator=(Pending|Active|Error|CreatedByMe) #OperatorCriteria
	| operator=CreatedBy WS+ criteriaValue=multipleQuoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsNot|IsGreaterThan|IsLessThan) WS+ criteriaValue=multipleQuoted #FieldOperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(IsSince|IsUntil) WS+ criteriaValue=multipleQuoted #FieldOperatorValueCriteria
    | Reference #ReferenceCriteria
    | criteria WS+ And WS+ criteria #AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria
    | LParens WS* criteria WS* RParens #ParensCriteria
    | Fuzzy #FuzzyCriteria
    ;

order
	: orderField=Quoted WS* (WS+ direction=(Asc|Desc))?
	;

multipleQuoted
    : Quoted(WS* Comma WS* Quoted)*
    ;

Pending
	: 'pending'
	;

Active
	: 'active'
	;

Error
	: 'error'
	;

CreatedByMe
	: 'created' WS+ 'by' WS+ 'me'
	;

CreatedBy
	: 'created' WS+ 'by'
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

Reference
    : ([a-zA-Z0-9_]([a-zA-Z0-9_\-/.]*[a-zA-Z0-9_])?)? '#' [0-9]+ | [A-Z][A-Z]+ '-' [0-9]+
    ;

Comma
	: ','
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
