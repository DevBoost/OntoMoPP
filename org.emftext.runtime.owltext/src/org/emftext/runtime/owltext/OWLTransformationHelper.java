package org.emftext.runtime.owltext;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.loading.OntologyLoadExeption;
import org.emftext.language.owl.resource.owl.analysis.custom.CrossResourceIRIResolver;

public class OWLTransformationHelper {


	private static Map<EObject, Integer> uniqueIdMap = new HashMap<EObject, Integer>();
	private static int counter = 0;
	private static Map<EPackage, Ontology> packageOntologyMap = new HashMap<EPackage, Ontology>();
	
	public static String getNamespacePrefix(EPackage ePackage) {
		return ePackage.getName();
	}

	
	public static String getIdentificationIRI(EClass eClass) {
		EPackage ePackage = eClass.getEPackage();
		String iri = ePackage.getName();
		while (ePackage.getESuperPackage() != null) {
			ePackage = ePackage.getESuperPackage();
			iri = ePackage.getName() + "_" + iri;
		}
		iri += ":";
		iri += eClass.getName();
		return iri;
	}

	public static String getIdentificationIRI(EObject eObject) {
		return "individual_" + getUniqueId(eObject);
	}

	private static String getUniqueId(EObject eObject) {
		Integer id = uniqueIdMap.get(eObject);
		if (id == null ) {
			id = counter++;
			uniqueIdMap.put(eObject, id);
		}
		return id.toString();
	}

	public static String getIdentificationIRI(EStructuralFeature feature) {
		String iri = getIdentificationIRI(feature.getEContainingClass());
		iri += "_" + feature.getName();
		return iri;
	}
	
	
	

	public static Ontology getOntology(EPackage ePackage, EObject root) {
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


	public static String getOWLRepresentation(
			OWLTextEObjectImpl rootOWLTextObjectImpl) {
		// TODO Auto-generated method stub
		return null;
	}
}
