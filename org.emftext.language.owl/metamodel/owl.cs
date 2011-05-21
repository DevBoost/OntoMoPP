//*******************************************************************************
// Copyright (c) 2006-2010 
// Software Technology Group, Dresden University of Technology
// 
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0 
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
// 
// Contributors:
//   Software Technology Group - TU Dresden, Germany 
//      - initial API and implementation
// ******************************************************************************/

SYNTAXDEF owl
FOR <http://org.emftext/owl.ecore> <owl.genmodel>
START OntologyDocument

OPTIONS {	
	licenceHeader ="../../org.dropsbox/licence.txt";
	//generateCodeFromGeneratorModel = "true";
	overrideManifest = "false";
	overrideBuildProperties = "false";
	overrideClasspath = "false";
	overrideProjectFile = "false";
	overrideDefaultResolverDelegate = "false";
	tokenspace = "1";
	//memoize = "true";
	usePredefinedTokens = "false";
	// avoid overriding of plug-in xml. otherwise extension point
	// for reasoning builder would be invalidated
	overridePluginXML = "false";
	//additionalExports = "com.clarkparsia.explanation,com.clarkparsia.explanation.io,com.clarkparsia.explanation.util,com.clarkparsia.modularity.locality,de.uulm.ecs.ai.owl.krssparser,de.uulm.ecs.ai.owl.krssrenderer,edu.unika.aifb.rdf.api.syntax,edu.unika.aifb.rdf.api.util,org.coode.manchesterowlsyntax,org.coode.obo.parser,org.coode.obo.renderer,org.coode.owl.functionalparser,org.coode.owl.functionalrenderer,org.coode.owl.krssparser,org.coode.owl.latex,org.coode.owl.owlxmlparser,org.coode.owl.rdf.model,org.coode.owl.rdf.rdfxml,org.coode.owl.rdf.renderer,org.coode.owl.rdf.turtle,org.coode.owl.rdfxml.parser,org.coode.owlapi.owlxml.renderer,org.coode.string,org.coode.xml,org.emftext.language.owl.loading,org.emftext.language.owl.resource.owl,org.emftext.language.owl.resource.owl.analysis,org.emftext.language.owl.resource.owl.analysis.custom,org.emftext.language.owl.resource.owl.mopp,org.emftext.language.owl.resource.owl.ui,org.emftext.language.owl.resource.owl.util,org.semanticweb.owl,org.semanticweb.owl.apibinding,org.semanticweb.owl.debugging,org.semanticweb.owl.expression,org.semanticweb.owl.inference,org.semanticweb.owl.io,org.semanticweb.owl.metrics,org.semanticweb.owl.model,org.semanticweb.owl.modularity,org.semanticweb.owl.normalform,org.semanticweb.owl.profiles,org.semanticweb.owl.util,org.semanticweb.owl.vocab,org.semanticweb.reasonerfactory,org.semanticweb.reasonerfactory.factpp,org.semanticweb.reasonerfactory.hermit,org.semanticweb.reasonerfactory.pellet,uk.ac.manchester.cs.bhig.util,uk.ac.manchester.cs.owl,uk.ac.manchester.cs.owl.dlsyntax,uk.ac.manchester.cs.owl.dlsyntax.parser,uk.ac.manchester.cs.owl.explanation.ordering,uk.ac.manchester.cs.owl.inference.dig11,uk.ac.manchester.cs.owl.mansyntaxrenderer,uk.ac.manchester.cs.owl.modularity,uk.ac.manchester.cs.owl.turtle.parser";
	disableLaunchSupport = "true";
	disableDebugSupport = "true";
}

TOKENS {
	DEFINE STRING_LITERAL $'"'('\\'('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')|('\\''u'('0'..'9'|'a'..'f'|'A'..'F')('0'..'9'|'a'..'f'|'A'..'F')('0'..'9'|'a'..'f'|'A'..'F')('0'..'9'|'a'..'f'|'A'..'F'))|'\\'('0'..'7')|~('\\'|'"'))*'"'$;
	

	DEFINE WHITESPACE $(' '|'\t'|'\f')$;
	DEFINE LINEBREAKS $('\r\n'|'\r'|'\n')$;
	DEFINE NOT $'not'$;
	//DEFINE INVERSE $'inverse'|'inv'$;
	DEFINE INT $('+'|'-')?('0'..'9')+$;
	DEFINE DECIMAL $('+'|'-')?('0'..'9')+ '.' ('0'..'9')+$;
	DEFINE FLOAT $('+'|'-')?( ('0'..'9')+ '.' ('0'..'9')* (('e'|'E'|'p'|'P') ('+'|'-')? ('0'..'9')+)? ('f'|'F') 
				| ('.' ('0'..'9')+ (('e'|'E'|'p'|'P') ('+'|'-')? ('0'..'9')+)?) ('f'|'F') 
				| (('0'..'9')+ (('e'|'E'|'p'|'P') ('+'|'-')? ('0'..'9')+) ('f'|'F') 
				| ('0'..'9')+ ('f'|'F')) )$;
	
	DEFINE FACETKINDS $'length'|'minLength'|'maxLength'
						|'pattern'|'langPattern'|'<='|'<'
						|'>'|'>='$;
	DEFINE CHARACTERISTICS $'Functional'|'InverseFunctional'|'Reflexive'
						|'Irreflexive'|'Symmetric'|'Asymmetric'|'Transitive'$; 
	DEFINE IRI $(('<')(~('>')|('\\''>'))*('>'))|(('A'..'Z' | ':' | 'a'..'z' | '0'..'9' | '_' | '-' )+)$;
}


RULES{
	OntologyDocument ::= namespace*  ontology;

	Annotation ::= "Annotations:" 
					( annotations? annotationValues)? 
					(!1 "," annotations? annotationValues)*;
	
	AnnotationValue ::= annotationProperty[IRI] target;
	
	IRITarget ::= target[IRI];
	
	LiteralTarget ::= literal;
	
	Namespace ::= "Prefix:" prefix[IRI]? importedOntology[IRI] !0;
	
	Ontology ::= "Ontology:" (uri[IRI] versionIRI[IRI] ? !1)? 
					("Import:" imports[IRI] !1)* 
					(annotations !1)* 
					(!1 frames !1)*;
	
	// ONTOLOGY Class definitions and Axioms			
	Class ::= "Class:" iri[IRI] !1 (
				(annotations !1) 
				| ("SubClassOf:" superClassesDescriptions ("," superClassesDescriptions )* !1)
				| ("EquivalentTo:" equivalentClassesDescriptions ("," equivalentClassesDescriptions)* !1)
				| ("DisjointWith:" disjointWithClassesDescriptions ("," disjointWithClassesDescriptions )* !1)
				| ("DisjointUnionOf:" disjointUnionWithClassesDescriptions ("," disjointUnionWithClassesDescriptions)* !1)
			)*;
	
	// ONTOLOGY Property definitions and Axioms			
	ObjectProperty ::= "ObjectProperty:" iri[IRI] !1 ((annotations !1)
		| ( "Domain:" domain ("," domain)* !1)
	 	| ( "Range:" propertyRange ("," propertyRange)* !1) 
	 	| ( "Characteristics:" characteristics[CHARACTERISTICS] ("," characteristics[CHARACTERISTICS])* !1) 
	 	| ( "SubPropertyOf:" superProperties ("," superProperties)* !1) 
	 	| ( "EquivalentTo:" equivalentProperties ("," equivalentProperties)* !1) 
	 	| ( "DisjointWith:" disjointProperties ("," disjointProperties)* !1) 
	 	| ( "InverseOf:" inverseProperties ("," inverseProperties)* !1) 
	 	| ( "SubPropertyChain:" subPropertyChains ("o" subPropertyChains)+ !1)
	 )*;
	
	// DataProperty definitions	
	DataProperty ::= "DataProperty:" iri[IRI] !1 (
		(annotations !1) 
		| ("Domain:" (domain ("," domain)*)!1)
		| ("Range:" (range ("," range)*)!1)
		| ("Characteristics:" characteristic[CHARACTERISTICS] !1)
		| ("SubPropertyOf:" (superProperties[IRI] ("," superProperties[IRI])*) !1)
		| ("EquivalentTo:" (equivalentProperties[IRI] ("," equivalentProperties[IRI])*) !1)
		| ("DisjointWith:" (disjointProperties[IRI] ("," disjointProperties[IRI])*) !1)
	)*;
	
	// Annotation Property definition
	AnnotationProperty ::= "AnnotationProperty:" iri[IRI] !1 (
		(annotations)
		| ("Domain:" (domains[IRI] ("," domains[IRI])*) !1)
		| ("Range:" (ranges[IRI] ("," ranges[IRI])*) !1)
		| ("SubPropertyOf:" (superAnnotationProperties[IRI]("," superAnnotationProperties[IRI])*) !1)
	)*;

	// Individual Property definition
	Individual ::= "Individual:" iri[IRI] !1 (
		(annotations)
		| ("Types:" (types ("," types)*) !1)
		| ("SameAs:" (sameAs[IRI] ("," sameAs[IRI])*) !1)
		| ("DifferentFrom:" (differentFrom[IRI] ("," differentFrom[IRI])*) !1)
		| ("Facts:" (facts ("," facts)*) !1)
	)*;

	// Datatype definition
	Datatype ::= "Datatype:" iri[IRI] !1 
		(annotations !1)? ("EquivalentTo:" dataRange !1)? (annotations !1)?
	;

	ObjectPropertyFact ::= not[NOT]? objectProperty[IRI] individual[IRI];
	 
	DataPropertyFact ::= not[NOT]? dataProperty[IRI] literal;
	
	// MISC Frame 
	EquivalentClasses ::= "EquivalentClasses:" annotations? descriptions ("," descriptions)+;
	DisjointClasses ::= "DisjointClasses:" annotations? descriptions ("," descriptions)+;
	EquivalentProperties ::= "EquivalentProperties:" annotations? objectProperties[IRI] ("," objectProperties[IRI])+;
	DisjointProperties ::= "DisjointProperties:" annotations? objectProperties[IRI] ("," objectProperties[IRI])+;  
	SameIndividuals ::= "SameIndividual:" annotations? individuals[IRI] ("," individuals[IRI])+;
	DifferentIndividuals ::= "DifferentIndividuals:" annotations? individuals[IRI] ("," individuals[IRI])+;
	
	HasKey ::=  "HasKey:" annotations? (  featureReferences )+;
	
	// Descriptions
	
	Disjunction ::= (annotations ("," annotations)*)? conjunctions:Conjunction ("or" conjunctions:Conjunction)*;
	Conjunction ::= (clazz[IRI] "that")? primaries:Primary ("and" primaries:Primary)*;								
		
	ObjectPropertySome ::= not[NOT]?  featureReference "some" (primary|dataPrimary);
	ObjectPropertyOnly ::= not[NOT]?  featureReference "only" (primary|dataPrimary);
	ObjectPropertySelf ::= not[NOT]? objectPropertyReference "self";
	ObjectPropertyValue ::= not[NOT]?  featureReference "value" (individual[IRI]|literal);

 	ObjectPropertyMin ::= not[NOT]?  featureReference "min" value[INT] (primary|dataPrimary)?;
	ObjectPropertyMax ::= not[NOT]?  featureReference "max" value[INT] (primary|dataPrimary)?;
	ObjectPropertyExactly ::= not[NOT]?  featureReference "exactly" value[INT] (primary|dataPrimary)?;


	ClassAtomic ::= not[NOT]? clazz[IRI];
	IndividualsAtomic ::= not[NOT]? "{" individuals[IRI] ("," individuals[IRI])* "}";
	NestedDescription ::= not[NOT]? "(" description:Disjunction ")";
	
	ObjectPropertyReference ::= objectProperty[IRI];
	InverseObjectPropertyReference ::= "inverse" objectProperty[IRI];
	OwlApiInverseObjectPropertyReference ::= "inv" "("objectProperty[IRI] ")";
	 
	FeatureReference ::= feature[IRI];
	InverseFeatureReference ::= "inverse" feature[IRI];
	OwlApiInverseFeatureReference ::= "inv" "("feature[IRI] ")";
	
	// DataRanges
	DataRange ::= dataConjunctions:DataConjunction ("or" dataConjunctions:DataConjunction)*;
	DataConjunction ::= dataPrimaries:DataPrimary ("and" dataPrimaries:DataPrimary)*; 
	DatatypeReference ::= not[NOT]? theDatatype[IRI] ("[" facets ("," facets)* "]")?;
	Facet ::= facetType[FACETKINDS] literal;
	NestedDataRange ::= not[NOT]? "(" dataRange ")";
	DataPrimaryLiterals ::= not[NOT]? "{" literals ("," literals)* "}";
		
	
	// Literals
	
	TypedLiteral ::= lexicalValue[STRING_LITERAL] "^^" theDatatype[IRI];
	AbbreviatedRDFTextLiteral ::= value[STRING_LITERAL] "@" languageTag[IRI];
	AbbreviatedXSDStringLiteral ::= value[STRING_LITERAL];
	DecimalLiteral ::=value[DECIMAL];
	FloatingPointLiteral ::= literal[FLOAT];
	IntegerLiteral ::= value[INT];
	BooleanLiteral ::= value["true":"false"];
}