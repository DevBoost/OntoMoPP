package org.emftext.language.swrl.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.owl.BooleanLiteral;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.DataProperty;
import org.emftext.language.owl.Description;
import org.emftext.language.owl.Feature;
import org.emftext.language.owl.IRIIdentified;
import org.emftext.language.owl.Literal;
import org.emftext.language.owl.ObjectProperty;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.swrl.Atom;
import org.emftext.language.swrl.DLiteral;
import org.emftext.language.swrl.DObject;
import org.emftext.language.swrl.DVariable;
import org.emftext.language.swrl.DescriptionAtom;
import org.emftext.language.swrl.DifferentFromAtom;
import org.emftext.language.swrl.IObject;
import org.emftext.language.swrl.IVariable;
import org.emftext.language.swrl.PropertyAtom;
import org.emftext.language.swrl.Rule;
import org.emftext.language.swrl.SWRLDocument;
import org.emftext.language.swrl.SameAsAtom;
import org.emftext.language.swrl.UnknownObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLRule;

/**
 * A class that transforms a SWRLDocument into a SWRL rule that can be used
 * with the OWL API.
 */
public class SWRLRuleBuilder {

	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private OWLDataFactory factory = manager.getOWLDataFactory();

	public List<SWRLRule> getRules(SWRLDocument document) {
		List<Rule> rules = document.getRules();
		List<SWRLRule> result = new ArrayList<SWRLRule>();
		for (Rule rule : rules) {
			result.add(getRule(rule));
		}
		return result;
	}

	private SWRLRule getRule(Rule rule) {
		List<Atom> body = rule.getAntecedent().getBody();
		List<Atom> head = rule.getConsequent().getBody();
		Set<SWRLAtom> bodyAtoms = getSWRLAtoms(body);
		Set<SWRLAtom> headAtoms = getSWRLAtoms(head);
		return factory.getSWRLRule(bodyAtoms, headAtoms);
	}

	private Set<SWRLAtom> getSWRLAtoms(List<Atom> atoms) {
		Set<SWRLAtom> result = new LinkedHashSet<SWRLAtom>();
		for (Atom atom : atoms) {
			result.add(getSWRLAtom(atom));
		}
		return result;
	}

	private SWRLAtom getSWRLAtom(Atom atom) {
		if (atom instanceof DescriptionAtom) {
			DescriptionAtom descriptionAtom = (DescriptionAtom) atom;
			Description description = descriptionAtom.getDescription();
			if (description instanceof ClassAtomic) {
				ClassAtomic classAtomic = (ClassAtomic) description;
				IObject object = descriptionAtom.getObject();
				SWRLIArgument argument = getSWRLArgument(object);
				Class clazz = classAtomic.getClazz();
				OWLClass owlClass = getOWLClass(clazz);
				return factory.getSWRLClassAtom(owlClass, argument);
			}
		} else if (atom instanceof PropertyAtom) {
			return getSWRLObjectPropertyAtom((PropertyAtom) atom);
		} else if (atom instanceof DifferentFromAtom) {
			return getSWRLDifferentFromAtom((DifferentFromAtom) atom);
		} else if (atom instanceof SameAsAtom) {
			return getSWRLSameAsAtom((SameAsAtom) atom);
		}
		throw new RuntimeException("Found unknown atom (" + atom + ") in SWRL rule.");
	}

	private SWRLAtom getSWRLDifferentFromAtom(DifferentFromAtom atom) {
		IObject objectA = atom.getObjectA();
		IObject objectB = atom.getObjectB();
		SWRLIArgument argumentA = getSWRLArgument(objectA);
		SWRLIArgument argumentB = getSWRLArgument(objectB);
		return factory.getSWRLDifferentIndividualsAtom(argumentA, argumentB);
	}

	private SWRLAtom getSWRLSameAsAtom(SameAsAtom atom) {
		IObject objectA = atom.getObjectA();
		IObject objectB = atom.getObjectB();
		SWRLIArgument argumentA = getSWRLArgument(objectA);
		SWRLIArgument argumentB = getSWRLArgument(objectB);
		return factory.getSWRLSameIndividualAtom(argumentA, argumentB);
	}

	private SWRLAtom getSWRLObjectPropertyAtom(PropertyAtom propertyAtom) {
		Feature property = propertyAtom.getProperty();
		if (property instanceof ObjectProperty) {
			ObjectProperty objectProperty = (ObjectProperty) property;
			OWLObjectProperty owlObjectProperty = getOWLObjectProperty(objectProperty);
			IObject iObject = propertyAtom.getSource();
			UnknownObject dObject = propertyAtom.getTarget();
			SWRLIArgument argument1 = getSWRLArgument(iObject);
			SWRLIArgument argument2 = getSWRLArgument(dObject);
			return factory.getSWRLObjectPropertyAtom(owlObjectProperty, argument1, argument2);
		} else if (property instanceof DataProperty) {
			DataProperty dataProperty = (DataProperty) property;
			OWLDataProperty owlDataProperty = getOWLDataProperty(dataProperty);
			IObject iObject = propertyAtom.getSource();
			UnknownObject dObject = propertyAtom.getTarget();
			if (dObject instanceof DObject) {
				SWRLIArgument argument1 = getSWRLArgument(iObject);
				SWRLDArgument argument2 = getSWRLArgument((DObject) dObject);
				return factory.getSWRLDataPropertyAtom(owlDataProperty, argument1, argument2);
			}
		}
		throw new RuntimeException("Found unknown property atom (" + property + ") in SWRL rule.");
	}

	private OWLClass getOWLClass(Class clazz) {
		String fullIRI = getFullIRI(clazz);
		OWLClass owlClass = factory.getOWLClass(IRI.create(fullIRI));
		return owlClass;
	}

	private String getFullIRI(IRIIdentified object) {
		String iri = object.getIri();
		OntologyDocument rootContainer = (OntologyDocument) EcoreUtil.getRootContainer(object);
		String uri = rootContainer.getOntology().getUri();
		String fullIRI = uri + "#" + iri;
		return fullIRI;
	}

	private OWLObjectProperty getOWLObjectProperty(ObjectProperty property) {
		String fullIRI = getFullIRI(property);
		OWLObjectProperty owlProperty = factory.getOWLObjectProperty(IRI.create(fullIRI));
		return owlProperty;
	}

	private OWLDataProperty getOWLDataProperty(DataProperty property) {
		String fullIRI = getFullIRI(property);
		OWLDataProperty owlProperty = factory.getOWLDataProperty(IRI.create(fullIRI));
		return owlProperty;
	}

	private SWRLIArgument getSWRLArgument(UnknownObject object) {
		if (object instanceof IVariable) {
			IVariable variable = (IVariable) object;
			IRI varIRI = IRI.create(variable.getIri());
			return factory.getSWRLVariable(varIRI);
		}
		throw new RuntimeException("Found unknown IObject in SWRL rule.");
	}

	private SWRLDArgument getSWRLArgument(DObject object) {
		if (object instanceof DVariable) {
			DVariable variable = (DVariable) object;
			IRI varIRI = IRI.create(variable.getIri());
			return factory.getSWRLVariable(varIRI);
		} else if (object instanceof DLiteral) {
			DLiteral dLiteral = (DLiteral) object;
			Literal literal = dLiteral.getLiteral();
			OWLLiteral owlLiteral = getOWLLiteral(literal);
			return factory.getSWRLLiteralArgument(owlLiteral);
		}
		throw new RuntimeException("Found unknown DObject (" + object + ") in SWRL rule.");
	}

	private OWLLiteral getOWLLiteral(Literal literal) {
		if (literal instanceof BooleanLiteral) {
			BooleanLiteral bLiteral = (BooleanLiteral) literal;
			return factory.getOWLTypedLiteral(bLiteral.isValue());
		}
		throw new RuntimeException("Found unknown Literal (" + literal + ") in SWRL rule.");
	}
}
