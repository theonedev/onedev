grammar PackQuery;

query
    : WS* criteria WS* (WS OrderBy WS+ order (WS* orderOperator=Comma WS* order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS* orderOperator=Comma WS* order)* WS* EOF
    | WS* EOF
    ;

criteria
	: operator=PublishedByMe #OperatorCriteria
	| operator=(PublishedByUser|PublishedByBuild|PublishedByProject) WS+ criteriaValue=multipleQuoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsNot|IsUntil|IsSince) WS+ criteriaValue=multipleQuoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria	#AndCriteria
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

PublishedByMe
    : 'published' WS+ 'by' WS+ 'me'
    ;

PublishedByUser
    : 'published' WS+ 'by' WS+ 'user'
    ;

PublishedByBuild
    : 'published' WS+ 'by' WS+ 'build'
    ;

PublishedByProject
    : 'published' WS+ 'by' WS+ 'project'
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
