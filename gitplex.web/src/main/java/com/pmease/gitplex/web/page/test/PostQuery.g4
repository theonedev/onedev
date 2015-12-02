grammar PostQuery; // rename to distinguish from Expr.g4

query:   stat (';' stat)* ;

stat:   ID '=' expr (';'|',')?              # assign
    |   expr (';')?                         # printExpr
    ;

expr:   op=('-'|'+') expr                    # signed
    |   expr op=('*'|'/') expr               # MulDiv
    |   expr op=('+'|'-') expr               # AddSub
    |   ID                                   # id                 
    |   DOUBLE                               # Double
    |   '(' expr ')'                         # parens
    ;



MUL :   '*' ; // assigns token name to '*' used above in grammar
DIV :   '/' ;
ADD :   '+' ;
SUB :   '-' ;
ID  :   [a-zA-Z]+ [0-9]* ;      // match identifiers
DOUBLE :   [0-9]+ ('.' [0-9]+)? ;
WS : [ \t\r\n]+ -> skip ;