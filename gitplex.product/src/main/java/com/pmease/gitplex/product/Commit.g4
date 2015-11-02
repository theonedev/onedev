grammar Commit;
/*
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
*/
query
	: (ab|cd|ef) gh
	;
	
ab
	: 'ab'
	;
	
cd
	: 'cd'
	;
	
ef
	: 'ef'
	;
	
gh
	: 'gh'
	;
	
WS: [ \t\r\n]+ -> skip;