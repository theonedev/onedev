grammar CodeAssistTest1;

selfReference: 'ab' | selfReference 'cd';

mandatories: 'ab' 'c' | 'ab' 'c' | ('cd' ('ef' 'g')) 'h';

notRealAmbiguity: NUMBER+ NUMBER;

NUMBER: [1-9]+;

ID: [a-z]+;

WS: [ \t\r\n]+ -> skip;