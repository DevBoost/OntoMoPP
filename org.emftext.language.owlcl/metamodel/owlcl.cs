SYNTAXDEF owlcl
FOR <http://www.emftext.org/language/owlcl>
START OWLCLSpec

IMPORTS {
	owl : <http://org.emftext/owl.ecore> WITH SYNTAX owl <../../org.emftext.language.owl/metamodel/owl.cs>
}

OPTIONS {
	licenceHeader ="platform:/resource/org.reuseware/licence.txt";
	reloadGeneratorModel = "true";
	usePredefinedTokens = "false";
	generateCodeFromGeneratorModel = "false"; 
	//defaultTokenName = "IDENTIFIER";
	tokenspace = "1";
	overrideBuilder = "false" ;
	overrideManifest = "false" ;
}



TOKENS {
	DEFINE X $'df'$;
}

TOKENSTYLES {
	X 
}


RULES {
	OWLCLSpec ::= "import" constrainedMetamodel['"','"'] ("refinements:" "{" types* "}")? constraints*;
	
	Type ::= "type" name[IRI] "refines" eSuperTypes[IRI] ":" typeDescription;
	
	Constraint ::= constrainedMetaclass[IRI] "message" errorMsg['"','"'] ":" constraintDescription ";";
}