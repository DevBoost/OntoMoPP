package org.emftext.runtime.owltext;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.emftext.language.owl.AnnotationProperty;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.DataProperty;
import org.emftext.language.owl.Datatype;
import org.emftext.language.owl.Frame;
import org.emftext.language.owl.Individual;
import org.emftext.language.owl.Namespace;
import org.emftext.language.owl.ObjectProperty;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.OwlFactory;
import org.emftext.language.owl.loading.OntologyLoadExeption;
import org.emftext.language.owl.resource.owl.analysis.custom.CrossResourceIRIResolver;
import org.emftext.language.owl.resource.owl.mopp.OwlPrinter;

public class OWLTransformationHelper {


	private static Map<EObject, Integer> uniqueIdMap = new HashMap<EObject, Integer>();
	private static int counter = 0;
	private static Map<EPackage, Ontology> packageOntologyMap = new HashMap<EPackage, Ontology>();
	
	private static String getNamespacePrefix(EPackage ePackage) {
		return ePackage.getName();
	}

	
	public static String createIri(EClass eClass) {
		String iri = eClass.getEPackage().getName();
		iri += ":";
		iri += eClass.getName();
		return iri;
	}

	public static String getIdentificationIRI(OWLTextEObjectImpl eObject) {
		return "individual_" + getUniqueId(eObject);
	}

	private static String getUniqueId(OWLTextEObjectImpl eObject) {
		Integer id = uniqueIdMap.get(eObject);
		if (id == null ) {
			id = counter++;
			uniqueIdMap.put(eObject, id);
		}
		return id.toString();
	}

	public static String getIdentificationIRI(EStructuralFeature feature) {
		String iri = createIri(feature.getEContainingClass());
		iri += "_" + feature.getName();
		return iri;
	}
	
	
	public static String getOWLRepresentation(OWLTextEObjectImpl eobject) {
		OwlFactory factory = OwlFactory.eINSTANCE;
		OntologyDocument ontologyDocument = factory.createOntologyDocument();
		Ontology ontology = factory.createOntology();
		ontology.setUri("http://transformed/" + eobject.eResource().getURI().lastSegment());
		ontologyDocument.setOntology(ontology);
		OWLTextEObjectImpl root = eobject;
		while(root.eContainer() != null) {
			root = (OWLTextEObjectImpl) root.eContainer();
		}
		
		addMetamodelImportAxioms(factory, ontologyDocument, ontology, root);
		
		
		ontology.getFrames().add(root.getOWLIndividual());
		TreeIterator<EObject> eAllContents = root.eAllContents();
		while (eAllContents.hasNext()) {
			OWLTextEObjectImpl child = (OWLTextEObjectImpl) eAllContents.next();
			ontology.getFrames().add(child.getOWLIndividual());
		}
		ByteArrayOutputStream outStream=new ByteArrayOutputStream();  
	    OwlPrinter printer = new OwlPrinter(outStream, null);
		printer.print(ontologyDocument);
	    String string = outStream.toString();
	    return string;
	}

	private static void addMetamodelImportAxioms(OwlFactory factory,
			OntologyDocument ontologyDocument, Ontology ontology,
			OWLTextEObjectImpl root) {
		EPackage ePackage = root.eClass().getEPackage();
		
		Ontology importedMetamodelOntology = getOntology(ePackage, root);
		ontology.getImports().add(importedMetamodelOntology);
		
		
		Namespace namespace = factory.createNamespace();
		String metaModelNamespacePrefix = getNamespacePrefix(ePackage);
		namespace.setPrefix(metaModelNamespacePrefix);
		namespace.setImportedOntology(importedMetamodelOntology);
		ontologyDocument.getNamespace().add(namespace);
		
		EList<Frame> frames = importedMetamodelOntology.getFrames();
		for (Frame frame : frames) {
			if (frame.getIri() != null && !frame.getIri().isEmpty()) {
				Frame declarationFrame = null;
				if (frame instanceof Class) {
					declarationFrame = factory.createClass();
				} else if (frame instanceof ObjectProperty) {
					declarationFrame = factory.createObjectProperty();
				} 
				else if (frame instanceof DataProperty) {
					declarationFrame = factory.createDataProperty();
				} else if (frame instanceof Datatype) {
					declarationFrame = factory.createDatatype();
				} else if (frame instanceof AnnotationProperty) {
					declarationFrame = factory.createAnnotationProperty();
				} else if (frame instanceof Individual) {
					declarationFrame = factory.createIndividual();
				}
				declarationFrame.setIri(metaModelNamespacePrefix + ":" + frame.getIri());
				ontology.getFrames().add(declarationFrame);
			
			}
		}
	}


	private static Ontology getOntology(EPackage ePackage, OWLTextEObjectImpl root) {
		Ontology ontology = packageOntologyMap.get(ePackage);
		if (ontology == null ) {
			String identifier = "platform:/resource/org.emftext.runtime.owltext.test/metamodel/" + ePackage.getName() + ".owl";
			try {
				ontology = CrossResourceIRIResolver.theInstance().getRemoteLoader().loadOntology(identifier, root);
				packageOntologyMap.put(ePackage, ontology);
				
			} catch (OntologyLoadExeption e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ontology;
	}
}
