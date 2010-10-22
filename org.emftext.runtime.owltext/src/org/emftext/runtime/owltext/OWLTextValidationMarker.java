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
							EList<AnnotationProperty> annotationProperties = annotation
									.getAnnotationProperty();
							boolean constraintProperty = false;
							for (AnnotationProperty annotationProperty : annotationProperties) {
								if ("comment".equals(annotationProperty
										.getIri()) || "rdfs:comment".equals(annotationProperty.getIri())) {
									constraintProperty = true;
								}
							}

							if (!constraintProperty) {
								continue;
							}
							EList<Target> targets = annotation.getTarget();
							for (Target target : targets) {
								if (target instanceof LiteralTarget) {
									LiteralTarget lt = (LiteralTarget) target;
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
