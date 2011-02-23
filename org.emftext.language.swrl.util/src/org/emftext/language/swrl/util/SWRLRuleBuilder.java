package org.emftext.language.swrl.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.Description;
import org.emftext.language.owl.IRIIdentified;
import org.emftext.language.owl.ObjectProperty;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.swrl.Atom;
import org.emftext.language.swrl.DescriptionAtom;
import org.emftext.language.swrl.IObject;
import org.emftext.language.swrl.IVariable;
import org.emftext.language.swrl.IndividualPropertyAtom;
import org.emftext.language.swrl.Rule;
import org.emftext.language.swrl.SWRLDocument;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLAtom;
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
		} else if (atom instanceof IndividualPropertyAtom) {
			return getSWRLObjectPropertyAtom((IndividualPropertyAtom) atom);
		}
		throw new RuntimeException("Found unknown element in SWRL rule.");
	}

	private SWRLAtom getSWRLObjectPropertyAtom(IndividualPropertyAtom propertyAtom) {
		ObjectProperty property = propertyAtom.getProperty();
		OWLObjectProperty owlObjectProperty = getOWLObjectProperty(property);
		IObject iObject = propertyAtom.getSource();
		IObject dObject = propertyAtom.getTarget();
		SWRLIArgument argument1 = getSWRLArgument(iObject);
		SWRLIArgument argument2 = getSWRLArgument(dObject);
		return factory.getSWRLObjectPropertyAtom(owlObjectProperty, argument1, argument2);
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

	private SWRLIArgument getSWRLArgument(IObject object) {
		if (object instanceof IVariable) {
			IVariable variable = (IVariable) object;
			IRI varIRI = IRI.create(variable.getIri());
			return factory.getSWRLVariable(varIRI);
		}
		throw new RuntimeException("Found unknown IObject in SWRL rule.");
	}

	/*
	private SWRLIArgument getSWRLArgument(DObject object) {
		if (object instanceof DVariable) {
			DVariable variable = (DVariable) object;
			IRI varIRI = IRI.create(variable.getIri());
			return factory.getSWRLVariable(varIRI);
		}
		throw new RuntimeException("Found unknown DObject in SWRL rule.");
	}
	*/
}
