grammar CommitQuery;

query: criteria+ EOF;

criteria: revisionRange | date | committer | author | path | message;

revisionRange: revision | EXCLUDE revision | revision RANGE revision;

revision: branch | tag | id;

branch: BRANCH VALUE;

tag: TAG VALUE;

id: ID VALUE;

date: before | after;

before: BEFORE VALUE;

after: AFTER VALUE;

committer: COMMITTER VALUE;

author: AUTHOR VALUE;

path: PATH VALUE;

message: MESSAGE VALUE;

BRANCH: 'branch';

TAG: 'tag';

ID: 'id';

BEFORE: 'before';

AFTER: 'after';

MESSAGE: 'message';

COMMITTER: 'committer';

AUTHOR: 'author';

PATH: 'path';

EXCLUDE: '^';

LPAREN: '(';

RPAREN: ')';

RANGE: '..'| '...';

VALUE: LPAREN (ESCAPE|~[()\\])+? RPAREN;

fragment
ESCAPE: '\\'[()\\];

WS: [ \t\r\n]+ -> skip;