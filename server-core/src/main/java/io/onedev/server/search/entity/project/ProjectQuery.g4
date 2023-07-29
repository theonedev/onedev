grammar ProjectQuery;

query
    : WS* criteria WS* (WS OrderBy WS+ order (WS+ And WS+ order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS+ And WS+ order)* WS* EOF
    | WS* EOF
    ;

criteria
	: operator=(Roots|Leafs|ForkRoots|OwnedByMe|OwnedByNone|HasOutdatedReplicas|WithoutEnoughReplicas|MissingStorage) #OperatorCriteria
	| operator=(OwnedBy|ForksOf|ChildrenOf) WS+ criteriaValue=Quoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|Contains|IsUntil|IsSince) WS+ criteriaValue=Quoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria #AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria
    | LParens WS* criteria WS* RParens #ParensCriteria
    | Fuzzy #FuzzyCriteria
    ;

order
	: orderField=Quoted WS* (WS+ direction=(Asc|Desc))?
	;

OrderBy
    : 'order' WS+ 'by'
    ;

Is
	: 'is'
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

WS
    : ' '
    ;

Fuzzy
    : '~' ('\\'.|~[~])+? '~'
    ;

Identifier
	: [a-zA-Z0-9:_/\\+\-;]+
	;
