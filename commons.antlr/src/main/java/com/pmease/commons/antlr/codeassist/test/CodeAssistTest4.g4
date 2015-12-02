grammar CodeAssistTest4;

stat: expr ';' | ID '=' expr ';' | ';';
expr: expr ('*'|'/') expr | expr ('+'|'-') expr | INT | ID | '(' expr ')';
ID: [a-zA-Z]+;
INT: [0-9]+;
WS: [ \t\n]+ -> skip;
