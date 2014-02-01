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
package org.emftext.runtime.owltext;

import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.emftext.access.EMFTextAccessProxy;
import org.emftext.access.resource.IResource;
import org.emftext.language.owl.AbbreviatedXSDStringLiteral;
import org.emftext.language.owl.Annotation;
import org.emftext.language.owl.AnnotationProperty;
import org.emftext.language.owl.AnnotationValue;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.Frame;
import org.emftext.language.owl.LiteralTarget;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.Target;
import org.emftext.language.owl.reasoning.EMFTextPelletReasoner;
import org.emftext.language.owl.reasoning.ReasoningException;
import org.semanticweb.owlapi.model.IRI;

public class OWLTextValidationMarker {

	private HashMap<String, String> errorMap;

	public void annotateValidationResults(Resource resource) {
		if (resource.getContents().size() < 1) return;
		if (resource.getContents().get(0) == null
				|| !(resource.getContents().get(0) instanceof OWLTextEObjectImpl)) {
			throw new RuntimeException("Given Resource is not OWLTextResource.");
		}

		IResource resourceProxy = (IResource) EMFTextAccessProxy.get(resource,
				IResource.class);

		OWLTextEObjectImpl root = (OWLTextEObjectImpl) resource.getContents()
				.get(0);
		OntologyDocument ontologyDocument = root.getOWLRepresentation();
		initialiseErrorMap(ontologyDocument);
		String owlRepresentation = OWLTextEObjectPrinter
				.getOWLRepresentation(ontologyDocument);
		String ontologyUri = root.getOntologyUri();

		EMFTextPelletReasoner reasoner = new EMFTextPelletReasoner();
		TreeIterator<EObject> allContents = resource.getAllContents();

		while (allContents.hasNext()) {
			EObject eObject = (EObject) allContents.next();
			if (eObject instanceof OWLTextEObjectImpl) {
				OWLTextEObjectImpl oteo = (OWLTextEObjectImpl) eObject;
				String iri = oteo.getOwlIndividualClass().getIri();

				try {
					List<String> allSuperFrames = reasoner.getAllSuperframes(
							owlRepresentation, ontologyUri, iri);
					for (String superframe : allSuperFrames) {
						IRI superframeIri = IRI.create(superframe);
						if (superframeIri.getFragment().startsWith(
								EMFTextPelletReasoner.CONSTRAINT_CLASS_PREFIX)) {
							String errorMsg = errorMap.get(superframeIri
									.getFragment());
							resourceProxy.addError(errorMsg, eObject);
						}

					}
				} catch (ReasoningException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	private void initialiseErrorMap(OntologyDocument ontologyDocument) {
		this.errorMap = new HashMap<String, String>();
		EList<Ontology> importedOntologies = ontologyDocument.getOntology()
				.getImports();
		for (Ontology ontology : importedOntologies) {
			EList<Frame> frames = ontology.getFrames();
			for (Frame frame : frames) {
				if (frame instanceof Class) {
					Class c = (Class) frame;
					if (c.getIri().startsWith(
							EMFTextPelletReasoner.CONSTRAINT_CLASS_PREFIX)) {
						EList<Annotation> annotations = c.getAnnotations();
						for (Annotation annotation : annotations) {
							EList<AnnotationValue> annotationValues = annotation.getAnnotationValues();
							for (AnnotationValue annotationValue : annotationValues) {
								AnnotationProperty annotationProperty = annotationValue.getAnnotationProperty();
								if ("comment".equals(annotationProperty
										.getIri()) || "rdfs:comment".equals(annotationProperty.getIri())) {
									if (annotationValue.getTarget() instanceof LiteralTarget) {
										LiteralTarget lt = (LiteralTarget) annotationValue.getTarget();
										if (lt.getLiteral() instanceof AbbreviatedXSDStringLiteral) {
											AbbreviatedXSDStringLiteral stringLiteral = (AbbreviatedXSDStringLiteral) lt
													.getLiteral();
											String errorMsg = stringLiteral
													.getValue();
											this.errorMap.put(c.getIri(), errorMsg);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
