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
	DEFINE INTEGER$('-')?('1'..'9')('0'..'9')*|'0'$;
	DEFINE FLOAT$('-')?(('1'..'9') ('0'..'9')* | '0') '.' ('0'..'9')+ $;
}

TOKENSTYLES{
	"+Feature" COLOR #7F0055, BOLD;
	"-Feature" COLOR #7F0055, BOLD;
}

RULES{
	
	MandatoryFeature ::= "+Feature"   name['"','"'] annotation? comments['<','>']* ("{" children* "}")?;
	
	OptionalFeature ::= "-Feature"   name['"','"'] ("{" children* "}")?;
	
	Annotation ::=	value['[',']'];
}