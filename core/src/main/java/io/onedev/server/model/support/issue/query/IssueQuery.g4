grammar IssueQuery;

query
    : criteria* 'order by' FieldName (',' FieldName)* EOF
    ;

criteria
    : 'c'
    ;

FieldName
    : ~[~`!@#$%^&()=+;',/\\:*?\"|(){}<> \[\]]
    ;

