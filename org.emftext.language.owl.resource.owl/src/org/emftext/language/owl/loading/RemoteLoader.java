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
package org.emftext.language.owl.loading;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emftext.language.owl.AnnotationProperty;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.DataProperty;
import org.emftext.language.owl.Datatype;
import org.emftext.language.owl.Frame;
import org.emftext.language.owl.IRIIdentified;
import org.emftext.language.owl.Individual;
import org.emftext.language.owl.ObjectProperty;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.OwlFactory;
import org.emftext.language.owl.resource.owl.mopp.OwlResourceFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class RemoteLoader {

	// private Ontology ontology;
	private static OwlFactory factory = OwlFactory.eINSTANCE;
	private static Map<String, Ontology> url2remoteontologies = new HashMap<String, Ontology>();
	private Map<String, Map<String, Frame>> remoteurl2irimaps = new HashMap<String, Map<String, Frame>>();

	static {
		String uri = "http://www.w3.org/2001/XMLSchema#";
		Resource resource = new OwlResourceFactory()
				.createResource(org.eclipse.emf.common.util.URI.createURI(uri));
		Ontology o = factory.createOntology();
		resource.getContents().add(o);
		o.setUri(uri);
		String[] types = new String[] { "ENTITIES", "ENTITY", "ID", "IDREF",
				"IDREFS", "NCName", "NMToken", "NOTATION", "Name", "QName",
				"anySimpleType", "anyType", "anyURI", "base64Binary",
				"boolean", "byte", "date", "dateTime", "dateTimeStamp",
				"decimal", "double", "duration", "float", "gDay", "gMonth",
				"gYear", "gMonthYear", "gYear", "gYearMonth", "hexBinary",
				"int", "integer", "language", "long", "negativeInteger",
				"nonNegativeInteger", "nonPositiveInteger", "normalizedString",
				"positiveInteger", "short", "string", "time", "token",
				"unsignedByte", "unsignedInt", "unsignedLong", "unsignedShort" };
		for (String type : types) {
			Datatype datatype = factory.createDatatype();
			datatype.setIri(type);
			o.getFrames().add(datatype);
		}
		url2remoteontologies.put(uri, o);

	}

	public RemoteLoader() {

	}

	public Ontology loadOntology(String uri, EObject container)
			throws OntologyLoadExeption {
		Ontology ontology = url2remoteontologies.get(uri);
		if (ontology == null) {
			ontology = initialise(uri, container);
		}
		return ontology;
	}

	public void addRemoteUriMapping(String uri, Ontology ontology) {
		url2remoteontologies.put(uri, ontology);
	}

	// public List<IRIIdentified> getOntologyElement(String identifier,
	// boolean resolveFuzzy) {
	// return findEntity(identifier, ontology, resolveFuzzy);
	// }

	public List<IRIIdentified> getOntologyElement(String identifier,
			Ontology ontology, boolean resolveFuzzy) {
		return findEntity(identifier, ontology, resolveFuzzy);
	}

	private org.eclipse.emf.common.util.URI getLocationHintURI(
			String locationHint, EObject container) {
		org.eclipse.emf.common.util.URI hintURI = null;

		if (locationHint.contains(":")) {
			// locationHint is an absolute path - we can use it as it is
			hintURI = org.eclipse.emf.common.util.URI.createURI(locationHint);
		} else {
			// locationHint is an relative path - we must resolve it
			org.eclipse.emf.common.util.URI containerURI = container
					.eResource().getURI();
			if (containerURI.isRelative()) {
				URI f = new File(".").getAbsoluteFile().toURI();
				org.eclipse.emf.common.util.URI baseURI = org.eclipse.emf.common.util.URI
						.createURI(f.toString());
				containerURI = containerURI.resolve(baseURI);
			}
			hintURI = org.eclipse.emf.common.util.URI.createURI(locationHint)
					.resolve(containerURI);

		}

		return hintURI;
	}

	private Ontology initialise(String uri, EObject container)
			throws OntologyLoadExeption {
		if (isRemoteUri(uri)) {
			return initialiseRemoteUri(uri);
		} else {
			return initialiseLocalUri(uri, container);
		}
	}

	private boolean isRemoteUri(String uri) {
		return uri.startsWith("http");
	}

	private Ontology initialiseLocalUri(String uri, EObject container) {
		ResourceSet rs = container.eResource().getResourceSet();

		if (uri == null) {
			return null;
		}
		org.eclipse.emf.common.util.URI loadUri = getLocationHintURI(uri,
				container);
		if ("owl".equals(loadUri.fileExtension())) {
			Resource ontoResource = null;

			try {
				ontoResource = rs.getResource(loadUri, true);
			} catch (Exception e) {
				e.printStackTrace();
			}

			EList<EObject> contents = null;
			if (ontoResource != null) {
				contents = ontoResource.getContents();
			}
			if (contents != null && contents.size() > 0) {
				if (contents.get(0) instanceof OntologyDocument) {
					OntologyDocument d = (OntologyDocument) contents.get(0);
					Ontology ontology = d.getOntology();
					return ontology;
				}
			}
		}
		return null;
	}

	private Ontology initialiseRemoteUri(String uri)
			throws OntologyLoadExeption {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		Ontology ontology;
		// Load the ontology
		try {
			OWLOntology owlOnto = manager.loadOntology(IRI.create(uri));
			ontology = propagate(owlOnto);
			ontology.setUri(uri);
			intialiseIriMap(ontology);
		} catch (OWLOntologyCreationException e) {
			// ontology = propagate(manager.
			// createOntology(Collections.EMPTY_SET));
			throw new OntologyLoadExeption(
					"Ontology could not be loaded for given URI (" + uri + ").",
					e);

			// e.printStackTrace();
		}
		if (ontology != null) {
			addRemoteUriMapping(uri, ontology);
		}
		return ontology;
	}

	private Ontology propagate(OWLOntology owlOntology) {
		String uri = owlOntology.getOntologyID().getOntologyIRI().toString();

		Resource resource = new OwlResourceFactory()
				.createResource(org.eclipse.emf.common.util.URI.createURI(uri));
		Ontology o = factory.createOntology();
		resource.getContents().add(o);

		o.setUri(uri);
		Set<OWLClass> classes = owlOntology.getClassesInSignature();
		for (OWLClass clazz : classes) {
			Class newClass = factory.createClass();
			newClass.setIri(clazz.getIRI().getFragment());
			o.getFrames().add(newClass);
		}
		Set<OWLObjectProperty> objectProperties = owlOntology
				.getObjectPropertiesInSignature();
		for (OWLObjectProperty objectProperty : objectProperties) {
			ObjectProperty newOP = factory.createObjectProperty();
			newOP.setIri(objectProperty.getIRI().getFragment());
			o.getFrames().add(newOP);
		}
		Set<OWLDataProperty> dataPropertiesInSignature = owlOntology
				.getDataPropertiesInSignature();
		for (OWLDataProperty dataProperty : dataPropertiesInSignature) {
			DataProperty newDP = factory.createDataProperty();
			newDP.setIri(dataProperty.getIRI().getFragment());
			o.getFrames().add(newDP);
		}
		Set<OWLNamedIndividual> individualsInSignature = owlOntology
				.getIndividualsInSignature();
		for (OWLNamedIndividual individual : individualsInSignature) {
			Individual newIndividual = factory.createIndividual();
			newIndividual.setIri(individual.getIRI().getFragment());
			o.getFrames().add(newIndividual);
		}
		Set<OWLAnnotationProperty> annotationProperties = owlOntology
				.getAnnotationPropertiesInSignature();
		for (OWLAnnotationProperty annotation : annotationProperties) {
			AnnotationProperty newAnnotationProperty = factory
					.createAnnotationProperty();
			newAnnotationProperty.setIri(annotation.getIRI().getFragment());
			o.getFrames().add(newAnnotationProperty);
		}
		return o;
	}

	private List<IRIIdentified> findEntity(String identifier, Ontology onto,
			boolean resolveFuzzy) {
		List<IRIIdentified> results = new ArrayList<IRIIdentified>();
		if (onto == null)
			return results;
		Map<String, Frame> iriMap = remoteurl2irimaps.get(onto.getUri());
		if (iriMap != null) {
			if (!resolveFuzzy) {
				Frame frame = iriMap.get(identifier);
				if (frame != null) {
					results.add(frame);
				}
				return results;
			} else {
				results.addAll(iriMap.values());
			}
		} else {
			EList<Frame> frames = onto.getFrames();
			if (!resolveFuzzy) {
				for (Frame frame : frames) {
					if (frame.getIri().equals(identifier)) {
						results.add(frame);
						return results;
					}
				}
			} else {
				results.addAll(frames);
			}

		}
		return results;
	}

	private Map<String, Frame> intialiseIriMap(Ontology onto) {
		Map<String, Frame> iriMap = new HashMap<String, Frame>();
		if (onto == null)
			return iriMap;

		EList<Frame> frames = onto.getFrames();
		for (Frame frame : frames) {
			if (frame.getIri() != null) {
				iriMap.put(frame.getIri(), frame);
			}
		}
		remoteurl2irimaps.put(onto.getUri(), iriMap);
		return iriMap;
	}

}
