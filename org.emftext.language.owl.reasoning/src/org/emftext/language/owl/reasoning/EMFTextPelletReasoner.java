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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.mindswap.pellet.exceptions.InternalReasonerException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class EMFTextPelletReasoner implements
		org.emftext.language.owl.reasoning.EMFTextOWLReasoner {

	public static final String CONSTRAINT_CLASS_PREFIX = "__c__";
	public static final String CONSTRAINT_PROPERTY_NAME = "rdfs:comment";
	private PelletReasoner reasoner;
	private String ontologyString;

	public Map<String, String> getInconsistentFrames(String owlRepresentation)
			throws ReasoningException {
		HashMap<String, String> inconsistentObjects = new HashMap<String, String>();

		try {
			// prepare infrastructure
			PelletReasoner reasoner = loadOntology(owlRepresentation);
			

			// // derive inconsistent individuals

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
				// reasoner.realise();
				Node<OWLClass> unsatisfiableClasses = reasoner
						.getUnsatisfiableClasses();
				for (OWLClass owlClass : unsatisfiableClasses.getEntities()) {
					inconsistentObjects.put(owlClass.getIRI().getFragment()
							.toString(), "Class is unsatisfiable.");
				}

				OWLOntology rootOntology = reasoner.getRootOntology();
				HashMap<String, String> class2errorMsg = new HashMap<String, String>();
				for (OWLOntology ontology : rootOntology.getImports()) {
					Set<OWLAnnotationAssertionAxiom> annotations = ontology
							.getAxioms(AxiomType.ANNOTATION_ASSERTION);
					for (OWLAnnotationAssertionAxiom owlAnnotation : annotations) {
						String iri = owlAnnotation.getSubject().toString();
						OWLAnnotationValue error = owlAnnotation.getValue();
						String propertyIri = owlAnnotation.getProperty()
								.toString();
						if (propertyIri.equals(CONSTRAINT_PROPERTY_NAME)
								&& error instanceof OWLLiteral) {
							class2errorMsg.put(iri,
									((OWLLiteral) error).getLiteral());

						}
					}
				}
				Set<OWLClass> classesInSignature = rootOntology
						.getClassesInSignature();

				for (OWLClass clazz : classesInSignature) {
					if (clazz.getIRI().getFragment()
							.startsWith(CONSTRAINT_CLASS_PREFIX)) {
						String error = class2errorMsg.get(clazz.getIRI()
								.toString());

						NodeSet<OWLNamedIndividual> instances = reasoner
								.getInstances(clazz, true);
						for (OWLNamedIndividual individual : instances
								.getFlattened()) {
							String iri = individual.getIRI().getFragment();
							inconsistentObjects.put(iri, error);

						}
					}
				}
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
		if (owlRepresentation.equals(this.ontologyString)) {
			return reasoner;
		}
		
		this.ontologyString = owlRepresentation;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		StringDocumentSource inputSource = new StringDocumentSource(
				owlRepresentation);
		// OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//
		// ManchesterOWLSyntaxParserFactory f = new
		// ManchesterOWLSyntaxParserFactory();
		// OWLParserFactoryRegistry.getInstance().registerParserFactory(f);
		//
		// OWLParser parser = f.createParser(manager);

		// load and parse ontology in manchester syntax

		OWLOntology ontology;
		try {

		
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
					
						return IRI.create(absoluteURI);
					} else if (localURI.isFile()) {
						URI absoluteURI = URI.create(localURI.toString());
						return IRI.create(absoluteURI);
					}
					return null;
				}
			});
			ontology = manager.loadOntologyFromOntologyDocument(inputSource);
		
			reasoner = PelletReasonerFactory.getInstance().createReasoner(
					ontology);
			reasoner.prepareReasoner();
		} catch (OWLOntologyCreationException e) {
			String message = "The ontology could not be checked for consistency: "
					+ e.getMessage();
			System.err.println(this.ontologyString);
			throw new ReasoningException(message, e);
		}

		return reasoner;
	}

	public List<String> getAllSuperframes(String owlRepresentation,
			String ontologyUri,
			String givenIri) throws ReasoningException {
		List<String> superFrames = new ArrayList<String>();
		PelletReasoner reasoner;
		reasoner = loadOntology(owlRepresentation);
		if (reasoner == null) return superFrames;
		if (!reasoner.isConsistent()) {

			String message = "The ontologies fact base is inconsistent. ";
			throw new ReasoningException(message);

		} else {
			IRI iri = IRI.create(ontologyUri + "#" + givenIri);
			OWLClass c = reasoner.getManager().getOWLDataFactory()
					.getOWLClass(iri);
			NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(c, false);
			Set<OWLClass> set = superClasses.getFlattened();
			for (OWLClass owlClass : set) {
				superFrames.add(owlClass.getIRI().toString());
			}
		}
		return superFrames;
	}
	
//	public List<String> getAllSubframes(String owlRepresentation,
//			String ontologyUri,
//			String givenIri) throws ReasoningException {
//		List<String> subframes = new ArrayList<String>();
//		PelletReasoner reasoner;
//		reasoner = loadOntology(owlRepresentation);
//		if (reasoner == null) return subframes;
//		if (!reasoner.isConsistent()) {
//
//			String message = "The ontologies fact base is inconsistent. ";
//			throw new ReasoningException(message);
//
//		} else {
//
//			IRI iri = IRI.create(ontologyUri + "#" + givenIri);
//			OWLClass c = reasoner.getManager().getOWLDataFactory()
//					.getOWLClass(iri);
//			NodeSet<OWLClass> subclasses = reasoner.getSubClasses(c, false);
//			Set<OWLClass> set = subclasses.getFlattened();
//			for (OWLClass owlClass : set) {
//				subframes.add(owlClass.getIRI().toString());
//			}
//		}
//
//		return subframes;
//	}
	

}
