grammar ProjectQuery;

query
    : WS* criteria WS* (WS OrderBy WS+ order (WS* orderOperator=Comma WS* order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS* orderOperator=Comma WS* order)* WS* EOF
    | WS* EOF
    ;

criteria
	: operator=(Roots|Leafs|ForkRoots|OwnedByMe|OwnedByNone|HasOutdatedReplicas|WithoutEnoughReplicas|MissingStorage) #OperatorCriteria
	| operator=(OwnedBy|ForksOf|ChildrenOf) WS+ criteriaValue=multipleQuoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsNot|Contains|IsUntil|IsSince) WS+ criteriaValue=multipleQuoted #FieldOperatorValueCriteria
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

OrderBy
    : 'order' WS+ 'by'
    ;

Is
	: 'is'
	;

IsNot
    : 'is' WS+ 'not'
    ;

OwnedBy
	: 'owned' WS+ 'by'
	;
	
OwnedByMe
	: 'owned' WS+ 'by' WS+ 'me'
	;

OwnedByNone
	: 'owned' WS+ 'by' WS+ 'none'
	;

IsSince
	: 'is' WS+ 'since'
	;

IsUntil
	: 'is' WS+ 'until'
	;
	
Contains
	: 'contains'
	;

ForksOf
	: 'forks' WS+ 'of'
	;
	
Roots
	: 'roots'
	;
	
Leafs
	: 'leafs'
	;
	
ForkRoots
	: 'fork' WS+ 'roots'
	;

WithoutEnoughReplicas
    : 'without' WS+ 'enough' WS+ 'replicas'
    ;

HasOutdatedReplicas
    : 'has' WS+ 'outdated' WS+ 'replicas'
    ;

MissingStorage
    : 'missing' WS+ 'storage'
    ;

ChildrenOf
	: 'children' WS+ 'of'
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
