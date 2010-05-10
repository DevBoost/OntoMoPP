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
import java.util.Iterator;
import java.util.Set;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxParserFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.mindswap.pellet.exceptions.InternalReasonerException;
import org.mindswap.pellet.owlapi.AxiomConverter;
import org.mindswap.pellet.owlapi.Reasoner;
import org.mindswap.pellet.utils.ATermUtils;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.io.OWLParser;
import org.semanticweb.owl.io.OWLParserFactoryRegistry;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyURIMapper;

import aterm.ATermAppl;

import com.clarkparsia.ic.Constraint;
import com.clarkparsia.ic.ConstraintViolation;
import com.clarkparsia.ic.ICTranslator;
import com.clarkparsia.ic.ICValidator;
import com.clarkparsia.ic.impl.ICTranslatorImpl;
import com.clarkparsia.ic.impl.ICValidatorPellet;
import com.hp.hpl.jena.iri.IRI;

public class PelletReasoner implements
		org.emftext.language.owl.reasoning.EMFTextOWLReasoner {

	public Set<OWLIndividual> getInconsistentIndividuals(
			String owlRepresentation) throws ReasoningException {
		Set<OWLIndividual> inconsistentIndividuals = new HashSet<OWLIndividual>();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Reasoner reasoner = loadOntology(owlRepresentation);
		try {
			reasoner.realise();
		} catch (OWLReasonerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ICTranslator translator = new ICTranslatorImpl();
		Set<Constraint> rules = translator.translate(reasoner.getKB());

		ICValidator validator = new ICValidatorPellet();
		Iterator<ConstraintViolation> violations = validator.getViolations(
				reasoner.getKB(), rules);

		AxiomConverter converter = new AxiomConverter(reasoner.getKB(), factory);
		// Display all violations
		while (violations.hasNext()) {
			ConstraintViolation violation = violations.next();
			// Convert the constraint to an OWLAPI axiom
			Constraint constraint = violation.getConstraint();
			OWLAxiom constraintAxiom = converter.convert(constraint.getAxiom());
			System.out.println(constraint.getVariable());
			System.out
					.println("Constraint is violated by each of the resources below:\n "
							+ constraintAxiom + "\n" + constraint);

			for (ATermAppl term : violation.getIndividuals()) {

				// Convert the violating individual to an OWLAPI individual
				OWLIndividual i;
				if (ATermUtils.isBnode(term)) {
					i = factory.getOWLAnonymousIndividual(URI.create(((ATermAppl) term
							.getArgument(0)).getName()));
				} else {
					i = factory.getOWLIndividual(URI.create(term.getName()));
				}

				System.out.println("V: " + term);
			}
		}
		return inconsistentIndividuals;
	}

	public Set<OWLClass> getInconsistentClasses(String owlRepresentation)
			throws ReasoningException {

		Set<OWLClass> inconsistentClasses = new HashSet<OWLClass>();

		try {
			// prepare infrastructure

			Reasoner reasoner = loadOntology(owlRepresentation);
			// derive inconsistent classes
			if (!reasoner.isConsistent()) {
				String message = "The ontologies fact base is inconsistent. ";
				throw new ReasoningException(message);

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
				reasoner.realise();
				reasoner.classify();
				reasoner.getKB().printClassTree();

				// reasoner.realise();
				// reasoner.classify();
				// reasoner.getKB().ensureConsistency();
				inconsistentClasses = reasoner.getInconsistentClasses();
			}
			return inconsistentClasses;

		} catch (InternalReasonerException e) {
			String message = "The ontology could not be checked for consistency: "
					+ e.getMessage();
			throw new ReasoningException(message, e);
		} catch (OWLReasonerException e) {
			String message = "The ontology could not be checked for consistency: "
					+ e.getMessage();
			throw new ReasoningException(message, e);
		}

	}

	private Reasoner loadOntology(String owlRepresentation)
			throws ReasoningException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		ManchesterOWLSyntaxParserFactory f = new ManchesterOWLSyntaxParserFactory();
		OWLParserFactoryRegistry.getInstance().registerParserFactory(f);

		OWLParser parser = f.createParser(manager);

		// load and parse ontology in manchester syntax
		StringInputSource inputSource = new StringInputSource(owlRepresentation) {

		};
		OWLOntology ontology;
		try {
			ontology = manager.createOntology(URI.create("check"));

			manager.addURIMapper(new OWLOntologyURIMapper() {

				public URI getPhysicalURI(URI logicalUri) {
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

						return absoluteURI;
					} else if (localURI.isFile()) {
						URI absoluteURI = URI.create(localURI.toString());
						return absoluteURI;
					}
					return null;
				}
			});
			parser.setOWLOntologyManager(manager);

			parser.parse(inputSource, ontology);
		} catch (OWLOntologyCreationException e) {
			String message = "The ontology could not be checked for consistency: "
					+ e.getMessage();
			throw new ReasoningException(message, e);
		}

		// load ontology in pellet

		Reasoner reasoner = new Reasoner(manager);
		Set<OWLOntology> importsClosure = manager.getImportsClosure(ontology);
		reasoner.loadOntologies(importsClosure);
		return reasoner;
	}

}
