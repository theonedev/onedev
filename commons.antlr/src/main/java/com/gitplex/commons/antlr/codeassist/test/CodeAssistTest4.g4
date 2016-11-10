grammar CodeAssistTest4;

stat: expr ';' | ID '=' expr ';' | ';';
expr: INT | ID | expr ('+'|'-'|'*'|'/') expr | '(' expr ')';
ID: [a-zA-Z]+;
INT: [0-9]+;
WS: [ \t\n]+ -> skip;
