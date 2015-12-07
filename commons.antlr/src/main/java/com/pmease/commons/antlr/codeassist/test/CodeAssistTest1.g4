grammar CodeAssistTest1;

selfReference: 'ab' selfReferenceTail;

selfReferenceTail: 'cd' selfReferenceTail|;

mandatories: 'ab' 'c' | 'ab' 'c' | ('cd' ('ef' 'g')) 'h';

ID: [a-zA-Z]+;

WS: [ \t\r\n]+ -> skip;