SYNTAXDEF petrinets
FOR <http://www.emftext.org/language/petrinets>
START PetriNet

OPTIONS {
	overrideBuilder = "false";	
}

TOKENS {
 	DEFINE SL_COMMENT $'//'(~('\n'|'\r'|'\uffff'))* $;
	DEFINE ML_COMMENT $'/*'.*'*/'$;
}

TOKENSTYLES {
	"ML_COMMENT" COLOR #008000, ITALIC;
	"SL_COMMENT" COLOR #000080, ITALIC;
}
 
RULES {
	PetriNet ::= "petrinet" name['"','"']? "{" (components | arcs)* "}";
	
	Arc ::= in['"','"'] "->" out['"','"'];
	ConsumingArc ::= in['"','"'] "-consume->" out['"','"'];
 	ProducingArc ::= in['"','"'] "-produce->" out['"','"'];	
	
	Place ::= "place" name['"','"']?;  
	Transition ::= "transition" name['"','"']?; 

}