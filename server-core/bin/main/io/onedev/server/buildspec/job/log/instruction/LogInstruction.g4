grammar LogInstruction;

instruction
    : PREFIX '[' Identifier param+ ']' EOF
    ;

param
	: (Identifier '=')? Value+
	;    

PREFIX
    : '##onedev'
    ;

Identifier
    : [a-zA-Z_][a-zA-Z0-9_]*
    ;

WS
    : ' ' -> skip
    ;

Value
	: '\'' ('\\'.|~['\\])*? '\''
	;
