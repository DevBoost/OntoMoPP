package org.emftext.ontomopp.modelsync.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

public class OWLTestHelper {

	public OWLOntology loadOntology(OWLOntologyManager manager, URI uri) {
		String fileString = uri.toFileString();
		IRI iri = IRI.create(new File(fileString));
		try {
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(iri);
			return ontology;
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return null;
		}
	}

	public PelletReasoner createReasoner(OWLOntology ontology) {
		PelletExplanation.setup();
		PelletOptions.USE_UNIQUE_NAME_ASSUMPTION = true;
		PelletReasoner reasoner = com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory.getInstance().createReasoner(ontology);
		try {
			reasoner.getKB().realize();
		} catch (InconsistentOntologyException e) {
			PelletExplanation explanation = new PelletExplanation(ontology);
			Set<OWLAxiom> axioms = explanation.getInconsistencyExplanation();
			for (OWLAxiom owlAxiom : axioms) {
				System.out.println("Axiom involved in inconsistency: " + owlAxiom);
			}
			fail(e.getMessage());
		}
		return reasoner;
	}
}
