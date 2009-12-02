package eu.most.transformation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.emftext.language.owl.AbbreviatedXSDStringLiteral;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.Conjunction;
import org.emftext.language.owl.DataProperty;
import org.emftext.language.owl.DataPropertyFact;
import org.emftext.language.owl.Datatype;
import org.emftext.language.owl.DatatypeReference;
import org.emftext.language.owl.Feature;
import org.emftext.language.owl.Frame;
import org.emftext.language.owl.Individual;
import org.emftext.language.owl.ObjectProperty;
import org.emftext.language.owl.ObjectPropertyFact;
import org.emftext.language.owl.ObjectPropertyMax;
import org.emftext.language.owl.ObjectPropertyMin;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.OwlFactory;

public class Ecore2Owl {

	public Ecore2Owl() {
		super();
	}
	
	private OwlFactory owlFactory = OwlFactory.eINSTANCE;
	private Ontology ontology;
	private HashMap<EClassifier, Frame> etype2oclass = new HashMap<EClassifier, Frame>();
	private HashMap<EStructuralFeature, Feature> references2objectProperties = new HashMap<EStructuralFeature, Feature>();
	
	private void initDatatypes() {
		HashMap<String, String> nameMap = new HashMap<String, String>();
		nameMap.put("EString", "xsd:string");
		nameMap.put("EInt", "xsd:integer");
		nameMap.put("EFloat", "xsd:float");
		
		EDataType[] primitiveTypes = new EDataType[]{
				EcorePackage.eINSTANCE.getEString(),
				EcorePackage.eINSTANCE.getEInt(),
				EcorePackage.eINSTANCE.getEFloat()};
		for (EDataType primitive : primitiveTypes) {
			Datatype property = owlFactory.createDatatype();
			property.setIri(nameMap.get(primitive.getName()));
			etype2oclass.put(primitive, property);
			//ontology.getFrames().add(property);
		}
	}
	
	public OntologyDocument transform(Collection<EObject> eObjects) {
		OntologyDocument d = owlFactory.createOntologyDocument();
		ontology = owlFactory.createOntology();
		initDatatypes();
		d.setOntology(ontology);
		
		for (EObject eObject : eObjects) {
			// TODO propagate each metamodel only once
			EPackage metamodel = eObject.eClass().getEPackage();
			while (metamodel.getESuperPackage() != null) {
				metamodel = metamodel.getESuperPackage();
			}
			propagateMetamodel(metamodel);
			propagateInstances(eObject);
		}
		return d;
	}

	private void propagateInstances(EObject eo) {
		HashMap<EObject, Individual> eobject2individual = new HashMap<EObject, Individual>();
		TreeIterator<EObject> allContents = eo.eAllContents();
		while(allContents.hasNext()) {
			EObject instance = allContents.next();
			Individual individual = owlFactory.createIndividual();
			individual.setIri("eid_" + instance.hashCode());
			eobject2individual.put(instance, individual);
			
			Class metaclass = (Class) etype2oclass.get(instance.eClass());
			ClassAtomic metaclassAtomic = owlFactory.createClassAtomic();
			metaclassAtomic.setClazz(metaclass);
			individual.getTypes().add(metaclassAtomic);
			ontology.getFrames().add(individual);
			
		}
		allContents = eo.eAllContents();
		while(allContents.hasNext()) {
			EObject instance = allContents.next();
			Individual contextIndividual = eobject2individual.get(instance);
			EList<EStructuralFeature> allStructuralFeatures = instance.eClass().getEAllStructuralFeatures();
			for (EStructuralFeature structuralFeature : allStructuralFeatures) {
				Object get = instance.eGet(structuralFeature);
				if (get instanceof EObject) {
					Individual i = eobject2individual.get(get);
					ObjectPropertyFact fact = owlFactory.createObjectPropertyFact();
					fact.setObjectProperty((ObjectProperty) references2objectProperties.get(structuralFeature));
					fact.setIndividual(i);
					contextIndividual.getFacts().add(fact);
				}
				else if (get instanceof String) {
					DataPropertyFact fact = owlFactory.createDataPropertyFact();
					fact.setDataProperty((DataProperty) references2objectProperties.get(structuralFeature));
					AbbreviatedXSDStringLiteral l = owlFactory.createAbbreviatedXSDStringLiteral();
					l.setValue((String) get);
					fact.setLiteral(l);
					contextIndividual.getFacts().add(fact);
				}
			}
		}
	}

	private void propagateMetamodel(EPackage metamodel) {
		TreeIterator<EObject> allContents = metamodel.eAllContents();
		while (allContents.hasNext()) {
			EObject elem = allContents.next();
			if (elem instanceof EClass) transformSingle((EClass) elem);
			else if (elem instanceof EDataType) transformSingle((EDataType) elem);
			// TODO needs to be completed
			//else System.out.println("not transformed: " + elem);
		}
		allContents = metamodel.eAllContents();
		while (allContents.hasNext()) {
			EObject elem = allContents.next();
			if (elem instanceof EReference) transformSingle((EReference) elem);
			if (elem instanceof EAttribute) transformSingle((EAttribute) elem);
			// TODO needs to be completed
			//else System.out.println("not transformed: " + elem);
		}
		
		Set<EClassifier> keySet = etype2oclass.keySet();
		for (EClassifier classifier : keySet) {
			if (classifier instanceof EClass) {
				EClass eclass = (EClass) classifier;
				Class c = (Class) etype2oclass.get(eclass);
				EList<EClass> superTypes = eclass.getESuperTypes();	
				Conjunction supertypes = owlFactory.createConjunction();
				c.getSuperClassesDescriptions().add(supertypes);
				for (EClass superclass : superTypes) {
					Class superframe = (Class) etype2oclass.get(superclass);
					ClassAtomic superClassAtomic = owlFactory.createClassAtomic();
					superClassAtomic.setClazz(superframe);
					supertypes.getPrimaries().add(superClassAtomic);		
				}
				EList<EAttribute> attributes = eclass.getEAttributes();
				for (EAttribute attribute : attributes) {
					DataProperty dataProperty = (DataProperty) references2objectProperties.get(attribute);
					
					if (attribute.getLowerBound() != 0) {
						ObjectPropertyMin minRestriction = owlFactory.createObjectPropertyMin();
						minRestriction.setFeature(dataProperty);
						minRestriction.setValue(attribute.getLowerBound());
						supertypes.getPrimaries().add(minRestriction);
					}
					
					if (attribute.getUpperBound() != -1) {
						ObjectPropertyMax maxRestriction = owlFactory.createObjectPropertyMax();
						maxRestriction.setFeature(dataProperty);
						maxRestriction.setValue(attribute.getUpperBound());
						supertypes.getPrimaries().add(maxRestriction);
					}
				}
				EList<EReference> references = eclass.getEReferences();
				for (EReference reference : references) {
					ObjectProperty objectProperty = (ObjectProperty) references2objectProperties.get(reference);
					
					if (reference.getLowerBound() != 0) {
						ObjectPropertyMin minRestriction = owlFactory.createObjectPropertyMin();
						minRestriction.setFeature(objectProperty);
						minRestriction.setValue(reference.getLowerBound());
						supertypes.getPrimaries().add(minRestriction);
					}
					
					if (reference.getUpperBound() != -1) {
						ObjectPropertyMax maxRestriction = owlFactory.createObjectPropertyMax();
						maxRestriction.setFeature(objectProperty);
						maxRestriction.setValue(reference.getUpperBound());
						supertypes.getPrimaries().add(maxRestriction);
					}
					
				}
			}
			
		}
		
	}

	private void transformSingle(EAttribute elem) {
		DataProperty d = owlFactory.createDataProperty();
		ontology.getFrames().add(d);
		d.setIri(elem.getName());
		Class domainClass = (Class) etype2oclass.get(elem.getEContainingClass());
		ClassAtomic domainClassAtomic = owlFactory.createClassAtomic();
		domainClassAtomic.setClazz(domainClass);
		d.getDomain().add(domainClassAtomic);
	
		elem.getEAttributeType();
		DatatypeReference dtr = owlFactory.createDatatypeReference();
		Datatype dataType = (Datatype) etype2oclass.get(elem.getEAttributeType());
		dtr.setTheDatatype(dataType);
		d.getRange().add(dtr);
		
		references2objectProperties.put(elem, d);

	}

	private void transformSingle(EReference elem) {
		ObjectProperty o = owlFactory.createObjectProperty();
		o.setIri(elem.getName());
		ontology.getFrames().add(o);
		
		Class rangeClass = (Class) etype2oclass.get(elem.getEType());
		ClassAtomic rangeClassAtomic = owlFactory.createClassAtomic();
		rangeClassAtomic.setClazz(rangeClass);
		o.getPropertyRange().add(rangeClassAtomic);
		
		Class domainClass = (Class) etype2oclass.get(elem.getEContainingClass());
		ClassAtomic domainClassAtomic = owlFactory.createClassAtomic();
		domainClassAtomic.setClazz(domainClass);
		o.getPropertyDomain().add(domainClassAtomic);
		
		references2objectProperties.put(elem, o);
	}

	private void transformSingle(EDataType elem) {
		Datatype d = owlFactory.createDatatype();
		ontology.getFrames().add(d);
		d.setIri(elem.getName());
		etype2oclass.put(elem, d);
		
	}

	private void transformSingle(EClass elem) {
		Class c = owlFactory.createClass();
		ontology.getFrames().add(c);
		c.setIri(elem.getName());
		etype2oclass.put(elem, c);
	}
}
