grammar CodeCommentQuery;

query
    : WS* criteria WS* (WS OrderBy WS+ order (WS* orderOperator=Comma WS* order)* WS*)? EOF
    | WS* OrderBy WS+ order (WS* orderOperator=Comma WS* order)* WS* EOF
    | WS* EOF
    ;

criteria
	: operator=(CreatedByMe|RepliedByMe|MentionedMe|Resolved|Unresolved) #OperatorCriteria
    | operator=(CreatedBy|RepliedBy|Mentioned|OnCommit) WS+ criteriaValue=multipleQuoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsNot|IsUntil|IsSince|IsGreaterThan|IsLessThan|Contains) WS+ criteriaValue=multipleQuoted #FieldOperatorValueCriteria
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

CreatedByMe
	: 'created' WS+ 'by' WS+ 'me'
	;

RepliedByMe
	: 'replied' WS+ 'by' WS+ 'me'
	;

MentionedMe
    : 'mentioned' WS+ 'me'
    ;
	
Resolved
	: 'resolved'
	;
	
Unresolved
	: 'unresolved'
	;
	
CreatedBy
	: 'created' WS+ 'by'
	;

RepliedBy
    : 'replied' WS+ 'by'
    ;

Mentioned
    : 'mentioned'
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

IsNot
    : 'is' WS+ 'not'
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
