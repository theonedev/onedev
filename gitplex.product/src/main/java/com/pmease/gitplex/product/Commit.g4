grammar Commit;

query
	:	(revision)*
	;
revision
	: 	branch|tag
	;
branch
	:	'branch' '(' ID ')'
	;
tag
	:	'tag' '(' ID ')'
	;

ID
	:	[a-z]+
	;
WS: [ \t\r\n]+ -> skip;