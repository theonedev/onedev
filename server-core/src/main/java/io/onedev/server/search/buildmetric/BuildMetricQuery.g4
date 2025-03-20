grammar BuildMetricQuery;

query
    : WS* criteria WS* EOF
    ;

criteria
	: operator=(BuildIsSuccessful|BuildIsFailed) #OperatorCriteria
    | criteriaField=Quoted WS+ operator=(IsEmpty|IsNotEmpty) #FieldOperatorCriteria
    | operator=(Since|Until) WS+ criteriaValue=multipleQuoted #OperatorValueCriteria
    | criteriaField=Quoted WS+ operator=(Is|IsNot) WS+ criteriaValue=multipleQuoted #FieldOperatorValueCriteria
    | criteria WS+ And WS+ criteria	#AndCriteria
    | criteria WS+ Or WS+ criteria #OrCriteria
    | Not WS* LParens WS* criteria WS* RParens #NotCriteria 
    | LParens WS* criteria WS* RParens #ParensCriteria
    ;

multipleQuoted
    : Quoted(WS* Comma WS* Quoted)*
    ;

BuildIsSuccessful
	: 'build' WS+ 'is' WS+ 'successful'
	;
	
BuildIsFailed
	: 'build' WS+ 'is' WS+ 'failed'
	;	

Is
	: 'is'
	;

IsNot
    : 'is' WS+ 'not'
    ;

IsEmpty
	: 'is' WS+ 'empty'
	;	

IsNotEmpty
    : 'is' WS+ 'not' WS+ 'empty'
    ;

Since
	: 'since'
	;

Until
	: 'until'
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

Identifier
	: [a-zA-Z0-9:_/\\+\-;]+
	;    
