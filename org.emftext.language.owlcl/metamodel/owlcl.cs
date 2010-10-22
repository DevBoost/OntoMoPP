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
}


RULES {
	OWLCLSpec ::= "import" constrainedMetamodel['"','"'] constraints*;
	Constraint ::= constrainedMetaclass[IRI] "message" errorMsg['"','"'] ":" constraintDescription ";";
}