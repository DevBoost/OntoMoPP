package org.emftext.language.swrl.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.Description;
import org.emftext.language.swrl.Atom;
import org.emftext.language.swrl.DescriptionAtom;
import org.emftext.language.swrl.IObject;
import org.emftext.language.swrl.IVariable;
import org.emftext.language.swrl.Rule;
import org.emftext.language.swrl.SWRLDocument;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
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
		List<Atom> head = rule.getAntecedent().getBody();
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
				if (object instanceof IVariable) {
					IVariable variable = (IVariable) object;
					IRI varIRI = IRI.create(variable.getIri());
					SWRLIArgument argument = factory.getSWRLVariable(varIRI);
					
					Class clazz = classAtomic.getClazz();
					String clazzIRI = clazz.getIri();
					OWLClass owlClass = factory.getOWLClass(IRI.create(clazzIRI));
					return factory.getSWRLClassAtom(owlClass, argument);
				}
			}
		}
		return null;
	}
}
