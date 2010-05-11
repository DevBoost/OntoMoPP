/*******************************************************************************
 * Copyright (c) 2006-2010 
 * Software Technology Group, Dresden University of Technology
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *      - initial API and implementation
 ******************************************************************************/
package org.emftext.language.owl.reasoning;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.mindswap.pellet.exceptions.InternalReasonerException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class EMFTextPelletReasoner implements
		org.emftext.language.owl.reasoning.EMFTextOWLReasoner {

	public Set<OWLNamedObject> getInconsistentFrames(String owlRepresentation)
			throws ReasoningException {

		Set<OWLNamedObject> inconsistentObjects = new HashSet<OWLNamedObject>();

		try {
			// prepare infrastructure

			PelletReasoner reasoner = loadOntology(owlRepresentation);
			reasoner.prepareReasoner();

			PelletExplanation expGen = new PelletExplanation(reasoner);

			// // derive inconsistent classes
			if (!reasoner.isConsistent()) {
				Set<OWLAxiom> inconsistencyExplanation = expGen
						.getInconsistencyExplanation();
				for (OWLAxiom owlAxiom : inconsistencyExplanation) {
					inconsistentObjects.addAll(owlAxiom
							.getIndividualsInSignature());
				}
				// String message =
				// "The ontologies fact base is inconsistent. ";
				// throw new ReasoningException(message);

				//
				// PelletExplanation pe = new PelletExplanation(reasoner);
				// Set<Set<OWLAxiom>> inconsistencyExplanations =
				// pe.getInconsistencyExplanations();
				//				
				// for (Set<OWLAxiom> set : inconsistencyExplanations) {
				//					
				// }
				//			
			} else {
				// reasoner.realise();
				Node<OWLClass> unsatisfiableClasses = reasoner
						.getUnsatisfiableClasses();
				inconsistentObjects.addAll(unsatisfiableClasses.getEntities());
				reasoner.getKB().printClassTree();

				// reasoner.realise();
				// reasoner.classify();
				// reasoner.getKB().ensureConsistency();
				// inconsistentClasses = reasoner.getInconsistentClasses();
			}
			return inconsistentObjects;

		} catch (InternalReasonerException e) {
			String message = "The ontology could not be checked for consistency: "
					+ e.getMessage();
			throw new ReasoningException(message, e);
		}

	}

	private PelletReasoner loadOntology(String owlRepresentation)
			throws ReasoningException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		StringDocumentSource inputSource = new StringDocumentSource(
				owlRepresentation);
		OWLDataFactory factory = manager.getOWLDataFactory();
		// OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//
		// ManchesterOWLSyntaxParserFactory f = new
		// ManchesterOWLSyntaxParserFactory();
		// OWLParserFactoryRegistry.getInstance().registerParserFactory(f);
		//
		// OWLParser parser = f.createParser(manager);

		// load and parse ontology in manchester syntax

		OWLOntology ontology;
		PelletReasoner reasoner;
		try {

			ontology = manager.loadOntologyFromOntologyDocument(inputSource);

			manager.addIRIMapper(new OWLOntologyIRIMapper() {

				public IRI getDocumentIRI(IRI logicalUri) {
					if (logicalUri.toString().startsWith("http"))
						return null;
					org.eclipse.emf.common.util.URI localURI = org.eclipse.emf.common.util.URI
							.createURI(logicalUri.toString());
					if (localURI.isPlatform()) {

						String platformResourcePath = localURI
								.toPlatformString(true);

						IFile file = ResourcesPlugin.getWorkspace().getRoot()
								.getFile(new Path(platformResourcePath));
						String absoluteLocation = file.getLocation().toFile()
								.toURI().toString();
						URI absoluteURI = URI.create(absoluteLocation);
						System.out.println("Requested: " + logicalUri + " - "
								+ absoluteURI);

						return IRI.create(absoluteURI);
					} else if (localURI.isFile()) {
						URI absoluteURI = URI.create(localURI.toString());
						return IRI.create(absoluteURI);
					}
					return null;
				}
			});
			reasoner = PelletReasonerFactory.getInstance().createReasoner(
					ontology);
			// reasoner.loadOntology(ontology);
			// parser.setOWLOntologyManager(manager);

			// parser.parse(inputSource, ontology);
		} catch (OWLOntologyCreationException e) {
			String message = "The ontology could not be checked for consistency: "
					+ e.getMessage();
			throw new ReasoningException(message, e);
		}

		// load ontology in pellet

		Set<OWLOntology> importsClosure = manager.getImportsClosure(ontology);
		// reasoner.loadOntologies(importsClosure);
		return reasoner;
	}

}
