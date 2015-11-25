grammar CodeAssistTest;

selfReference:	'ab' | selfReference 'cd';

mandatories: 'ab' 'c' | 'ab' 'c' | ('cd' ('ef' 'g')) 'h';

ID: [a-zA-Z]+;

WS: [ \t\r\n]+ -> skip;