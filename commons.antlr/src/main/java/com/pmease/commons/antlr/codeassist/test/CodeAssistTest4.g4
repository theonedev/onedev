grammar CodeAssistTest4;

stat: expr ';' | ID '=' expr ';' | ';';
expr: INT exprTail | ID exprTail | '(' expr ')' exprTail;
exprTail: ('*'|'/'|'+'|'-') expr exprTail | ; 
ID: [a-zA-Z]+;
INT: [0-9]+;
WS: [ \t\n]+ -> skip;
