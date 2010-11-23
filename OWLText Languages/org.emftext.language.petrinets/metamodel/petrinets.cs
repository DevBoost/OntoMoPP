SYNTAXDEF petrinets
FOR <http://www.emftext.org/language/petrinets>
START PetriNet

OPTIONS {
	overrideBuilder = "false";	
	reloadGeneratorModel = "true";
	usePredefinedTokens = "false";
}

TOKENS {
 	DEFINE SL_COMMENT $'//'(~('\n'|'\r'|'\uffff'))* $;
	DEFINE ML_COMMENT $'/*'.*'*/'$;
	
	DEFINE WHITESPACE $(' '|'\t'|'\f')$;
	DEFINE LINEBREAKS $('\r\n'|'\r'|'\n')$;
	
	DEFINE IDENTIFIER $('A'..'Z' | 'a'..'z' | '0'..'9' | '_' | '-' | '::')+$;
}

TOKENSTYLES {
	"ML_COMMENT" COLOR #008000, ITALIC;
	"SL_COMMENT" COLOR #000080, ITALIC;
}
 
RULES {
	PetriNet ::= "petrinet" name['"','"']?
				("import" imports['<','>'] ";")*
				"{" (components | arcs)* "}";
	
	Arc ::= in['"','"'] "->" out['"','"'] 
		("{" arcStatements*
		"}")*;
		
	Variable ::= name[IDENTIFIER] "=" initialisation;
	
	FunctionCall ::= function[IDENTIFIER]"("(parameters ("," parameters)*)?")";
	VariableCall ::= variable[IDENTIFIER];
		
	ConsumingArc ::= in['"','"'] "-consume->" out['"','"'];
 	ProducingArc ::= in['"','"'] "-produce->" out['"','"'];	
	
	Place ::= "place" name['"','"']? ":" type[IDENTIFIER];
	Transition ::= "transition" name['"','"']?; 

}