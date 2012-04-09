/*******************************************************************************
 * Copyright (c) 2006-2012
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 ******************************************************************************/
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
