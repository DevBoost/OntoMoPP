SYNTAXDEF fea
FOR <http://org.owltext/feature>
START MandatoryFeature

OPTIONS {
	resourcePluginID="org.emftext.runtime.owltext.test";
	resourceUIPluginID="org.emftext.runtime.owltext.test";
	overrideManifest="false";
	overrideUIManifest="false";
}
TOKENS{
	DEFINE COMMENT$'//'(~('\n'|'\r'|'\uffff'))*$;
	DEFINE OPERATOR$'+'|'-'|'*'|'/'$;
	
	//DEFINE INTEGER$('-')?('1'..'9')('0'..'9')*|'0'$;
	//DEFINE FLOAT$('-')?(('1'..'9') ('0'..'9')* | '0') '.' ('0'..'9')+ $;
	
}

TOKENSTYLES{
	"+Feature" COLOR #7F0055, BOLD;
	"-Feature" COLOR #7F0055, BOLD;
}

RULES{
	
	MandatoryFeature ::= "+Feature"   name['"','"'] annotation? 
		comments['<','>']* anyLiterals* ("{" children* "}")?;
	
	OptionalFeature ::= "-Feature"   name['"','"'] 
		("{" children* "}")? 
		//("("  () operator[OPERATOR] )* ")")?
		;
	
	Annotation ::=	value['[',']'];
	
	AnyInt ::= literal['$','$'];
	AnyFloat ::= literal['%','%'];
	AnyBoolean ::= literal['&','&'];
	AnyBigDecimal ::= literal['d','d'];
	AnyChar ::= literal['c','c'];
	
}