package eu.most.transformation.ecore_owl;

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
	
	public static String getNamespacePrefix(EPackage ePackage) {
		return ePackage.getName();
	}

	
	public static String getIdentificationIRI(EClass eClass) {
		String iri = eClass.getEPackage().getName();
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
}
