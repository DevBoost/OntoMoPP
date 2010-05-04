package eu.most.transformation.ecore_owl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.emftext.language.owl.AbbreviatedXSDStringLiteral;
import org.emftext.language.owl.Characteristic;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.Conjunction;
import org.emftext.language.owl.DataProperty;
import org.emftext.language.owl.DataPropertyFact;
import org.emftext.language.owl.Datatype;
import org.emftext.language.owl.DatatypeReference;
import org.emftext.language.owl.Feature;
import org.emftext.language.owl.FeatureReference;
import org.emftext.language.owl.FeatureRestriction;
import org.emftext.language.owl.Frame;
import org.emftext.language.owl.Individual;
import org.emftext.language.owl.IndividualsAtomic;
import org.emftext.language.owl.Namespace;
import org.emftext.language.owl.ObjectProperty;
import org.emftext.language.owl.ObjectPropertyFact;
import org.emftext.language.owl.ObjectPropertyMax;
import org.emftext.language.owl.ObjectPropertyMin;
import org.emftext.language.owl.ObjectPropertyReference;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.OwlFactory;

public class Ecore2Owl {

	public Ecore2Owl() {
		super();
	}

	private OwlFactory owlFactory = OwlFactory.eINSTANCE;
	private Ontology ontology;
	private HashMap<ENamedElement, Frame> etype2oclass = new HashMap<ENamedElement, Frame>();
	private HashMap<EStructuralFeature, Feature> references2objectProperties = new HashMap<EStructuralFeature, Feature>();

	private void initDatatypes() {
		HashMap<String, String> datatypeMap = new HashMap<String, String>();
		// # owl:real
		// # owl:rational
		// # xsd:decimal
		datatypeMap.put("java.math.BigDecimal", "xsd:decimal");
		datatypeMap.put("EBigDecimal","xsd:decimal");
		// # xsd:integer
		datatypeMap.put("java.math.BigInteger", "xsd:integer");
		datatypeMap.put("java.lang.Integer", "xsd:integer");
		datatypeMap.put("EInt", "xsd:integer");
		datatypeMap.put("EInteger", "xsd:integer");
		datatypeMap.put("EIntegerObject", "xsd:integer");
		datatypeMap.put("EBigInteger", "xsd:integer");
		datatypeMap.put("integer", "xsd:integer");
		// # xsd:nonNegativeInteger
		// # xsd:nonPositiveInteger
		// # xsd:positiveInteger
		// # xsd:negativeInteger
		// # xsd:long
		datatypeMap.put("long", "xsd:long");
		datatypeMap.put("java.lang.Long", "xsd:long");
		datatypeMap.put("ELong", "xsd:long");
		datatypeMap.put("ELongObject", "xsd:long");

		// # xsd:int
		datatypeMap.put("int", "xsd:int");
		// # xsd:short
		datatypeMap.put("short", "xsd:short");
		datatypeMap.put("java.lang.Short", "xsd:short");
		datatypeMap.put("EShort", "xsd:short");
		datatypeMap.put("EShortObject", "xsd:short");
		// # xsd:byte
		datatypeMap.put("byte", "xsd:byte");
		datatypeMap.put("java.lang.Byte", "xsd:byte");
		datatypeMap.put("EByte", "xsd:byte");
		datatypeMap.put("EByteObject", "xsd:byte");
		
		// # xsd:unsignedLong
		// # xsd:unsignedInt
		// # xsd:unsignedShort
		// # xsd:unsignedByte
		//		
		// # xsd:double
		datatypeMap.put("double", "xsd:double");
		datatypeMap.put("java.lang.Double", "xsd:double");
		datatypeMap.put("EDouble", "xsd:double");
		datatypeMap.put("EDoubleObject", "xsd:double");
		

		// # xsd:float
		datatypeMap.put("float", "xsd:float");
		datatypeMap.put("java.lang.Float", "xsd:float");
		datatypeMap.put("EFloat", "xsd:float");
		datatypeMap.put("EFloatObject", "xsd:float");
		//
		// # xsd:boolean
		datatypeMap.put("java.lang.Boolean", "xsd:boolean");
		datatypeMap.put("EBoolean", "xsd:boolean");
		datatypeMap.put("boolean", "xsd:boolean");
		datatypeMap.put("EBooleanObject", "xsd:boolean");
		//		
		// # xsd:string
		datatypeMap.put("java.lang.String", "xsd:string");
		datatypeMap.put("EString", "xsd:string");
		datatypeMap.put("string", "xsd:string");
		// # xsd:normalizedString
		// # xsd:token
		// # xsd:language
		// # xsd:Name
		// # xsd:NCName
		// # xsd:NMTOKEN
		//		
		// # xsd:hexBinary
		// # xsd:base64Binary
		//		
		// # xsd:dateTime
		datatypeMap.put("java.lang.Date", "xsd:dateTime");
		datatypeMap.put("EDate", "xsd:dateTime");

		
		datatypeMap.put("java.lang.Char", "xsd:string");
		datatypeMap.put("char", "xsd:string");
		datatypeMap.put("EChar", "xsd:string");
		datatypeMap.put("ECharacterObject", "xsd:string");
		
		EList<EClassifier> eClassifiers = EcorePackage.eINSTANCE
				.getEClassifiers();
		for (EClassifier eclassifier : eClassifiers) {
			if (eclassifier instanceof EDataType) {
				EDataType primitive = (EDataType) eclassifier;
				Datatype property = owlFactory.createDatatype();
				String typeName = datatypeMap.get(primitive
						.getInstanceClassName());
				if (typeName == null)
					typeName = primitive.getName();
				property.setIri(typeName);
				etype2oclass.put(primitive, property);
				
				// ontology.getFrames().add(property);
			}

		}
	}

	public OntologyDocument transformMetamodel(EPackage metamodel) {
		OntologyDocument d = owlFactory.createOntologyDocument();
		ontology = owlFactory.createOntology();
		initDatatypes();
		initStandardImports(d, metamodel);
		ontology.setUri(metamodel.getNsURI());
		propagateMetamodel(metamodel);
		d.setOntology(ontology);
		return d;
	}

	private void initStandardImports(OntologyDocument d, EPackage metamodel) {

		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		namespaces.put("owl", "http://www.w3.org/2002/07/owl#");
		namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		namespaces.put("xsd", "http://www.w3.org/2001/XMLSchema#");
		//namespaces.put("owl2xml", "http://www.w3.org/2006/12/owl2-xml#");

		namespaces.put(metamodel.getNsPrefix(), metamodel.getNsURI());
		addSubpackages(metamodel.getNsPrefix(), metamodel.getNsURI(),
				namespaces, metamodel.getESubpackages());

		namespaces.put(" ", metamodel.getNsURI());
		for (String prefix : namespaces.keySet()) {
			Namespace namespace = owlFactory.createNamespace();
			d.getNamespace().add(namespace);
			namespace.setPrefix(prefix);
			Ontology o = owlFactory.createOntology();
			o.setUri(namespaces.get(prefix));
			namespace.setImportedOntology(o);
		}

	}

	private void addSubpackages(String nsPrefix, String nsURI,
			Map<String, String> namespaces, EList<EPackage> eSubpackages) {
		for (EPackage ePackage : eSubpackages) {
			namespaces.put(nsPrefix + "_" + ePackage.getNsPrefix(), nsURI);
			addSubpackages(nsPrefix + "_" + ePackage.getNsPrefix(), nsURI,
					namespaces, ePackage.getESubpackages());
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
		while (allContents.hasNext()) {
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
		while (allContents.hasNext()) {
			EObject instance = allContents.next();
			Individual contextIndividual = eobject2individual.get(instance);
			EList<EStructuralFeature> allStructuralFeatures = instance.eClass()
					.getEAllStructuralFeatures();
			for (EStructuralFeature structuralFeature : allStructuralFeatures) {
				Object get = instance.eGet(structuralFeature);
				if (get instanceof EObject) {
					Individual i = eobject2individual.get(get);
					ObjectPropertyFact fact = owlFactory
							.createObjectPropertyFact();
					fact
							.setObjectProperty((ObjectProperty) references2objectProperties
									.get(structuralFeature));
					fact.setIndividual(i);
					contextIndividual.getFacts().add(fact);
				} else if (get instanceof String) {
					DataPropertyFact fact = owlFactory.createDataPropertyFact();
					fact
							.setDataProperty((DataProperty) references2objectProperties
									.get(structuralFeature));
					AbbreviatedXSDStringLiteral l = owlFactory
							.createAbbreviatedXSDStringLiteral();
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
			if (elem instanceof EClass)
				transformEClass((EClass) elem);
			else if (elem instanceof EEnum)
				transformEEnum((EEnum) elem);
			else if (elem instanceof EDataType)
				transformEDatatype((EDataType) elem);

			// TODO needs to be completed
//			else
//				System.out.println("not transformed: " + elem);
		}
		allContents = metamodel.eAllContents();
		while (allContents.hasNext()) {
			EObject elem = allContents.next();
			if (elem instanceof EReference)
				transformEReference((EReference) elem);
			if (elem instanceof EAttribute)
				transformEAttribute((EAttribute) elem);
			// TODO needs to be completed
//			else
//				System.out.println("not transformed: " + elem);
		}

		Set<ENamedElement> keySet = etype2oclass.keySet();
		for (ENamedElement classifier : keySet) {
			if (classifier instanceof EClass) {
				EClass eclass = (EClass) classifier;
				Class c = (Class) etype2oclass.get(eclass);
				EList<EClass> superTypes = eclass.getESuperTypes();
				Conjunction supertypes = owlFactory.createConjunction();
				c.getSuperClassesDescriptions().add(supertypes);
				for (EClass superclass : superTypes) {
					Class superframe = (Class) etype2oclass.get(superclass);
					ClassAtomic superClassAtomic = owlFactory
							.createClassAtomic();
					superClassAtomic.setClazz(superframe);
					supertypes.getPrimaries().add(superClassAtomic);
				}
				EList<EAttribute> attributes = eclass.getEAttributes();
				for (EAttribute attribute : attributes) {
					DataProperty dataProperty = (DataProperty) references2objectProperties
							.get(attribute);

					if (attribute.getLowerBound() != 0) {
						ObjectPropertyMin minRestriction = owlFactory
								.createObjectPropertyMin();
						setFeature(minRestriction, dataProperty);
						minRestriction.setValue(attribute.getLowerBound());
						supertypes.getPrimaries().add(minRestriction);
					}

					if (attribute.getUpperBound() != -1) {
						ObjectPropertyMax maxRestriction = owlFactory
								.createObjectPropertyMax();
						setFeature(maxRestriction, dataProperty);
						maxRestriction.setValue(attribute.getUpperBound());
						supertypes.getPrimaries().add(maxRestriction);
					}
				}
				EList<EReference> references = eclass.getEReferences();
				for (EReference reference : references) {
					ObjectProperty objectProperty = (ObjectProperty) references2objectProperties
							.get(reference);

					if (reference.getLowerBound() != 0) {
						ObjectPropertyMin minRestriction = owlFactory
								.createObjectPropertyMin();
						setFeature(minRestriction, objectProperty);
						minRestriction.setValue(reference.getLowerBound());
						supertypes.getPrimaries().add(minRestriction);
					}

					if (reference.getUpperBound() != -1) {
						ObjectPropertyMax maxRestriction = owlFactory
								.createObjectPropertyMax();
						setFeature(maxRestriction, objectProperty);
						maxRestriction.setValue(reference.getUpperBound());
						supertypes.getPrimaries().add(maxRestriction);
					}
					if (reference.getEOpposite() != null) {
						EReference eOpposite = reference.getEOpposite();
						ObjectProperty oppositeProperty = (ObjectProperty) references2objectProperties
								.get(eOpposite);
						ObjectPropertyReference ref = owlFactory
								.createObjectPropertyReference();
						ref.setObjectProperty(oppositeProperty);
						objectProperty.getInverseProperties().add(ref);

						ObjectPropertyReference oppRef = owlFactory
								.createObjectPropertyReference();
						oppRef.setObjectProperty(objectProperty);
						oppositeProperty.getInverseProperties().add(oppRef);

					}

				}
				if (supertypes.getPrimaries().size() == 0) {
					Class owlThing = (Class) owlFactory.createClass();
					owlThing.setIri("owl:Thing");
					ClassAtomic superClassAtomic = owlFactory
							.createClassAtomic();
					superClassAtomic.setClazz(owlThing);
					supertypes.getPrimaries().add(superClassAtomic);
				}

			}

		}

	}

	private void setFeature(FeatureRestriction restriction, Feature feature) {
		FeatureReference reference = OwlFactory.eINSTANCE
				.createFeatureReference();
		reference.setFeature(feature);
		restriction.setFeatureReference(reference);
	}

	private void transformEAttribute(EAttribute elem) {
		DataProperty d = owlFactory.createDataProperty();
		ontology.getFrames().add(d);
		d.setIri(OWLTransformationHelper.getIdentificationIRI(elem));
		Class domainClass = (Class) etype2oclass
				.get(elem.getEContainingClass());
		ClassAtomic domainClassAtomic = owlFactory.createClassAtomic();
		domainClassAtomic.setClazz(domainClass);
		d.getDomain().add(domainClassAtomic);

		elem.getEAttributeType();
		DatatypeReference dtr = owlFactory.createDatatypeReference();
		Datatype dataType = (Datatype) etype2oclass.get(elem
				.getEAttributeType());
		dtr.setTheDatatype(dataType);
		d.getRange().add(dtr);

		if (elem.getUpperBound() == 1)
			d.setCharacteristic(Characteristic.FUNCTIONAL);
		references2objectProperties.put(elem, d);

	}

	private void transformEReference(EReference elem) {
		ObjectProperty o = owlFactory.createObjectProperty();
		o.setIri(OWLTransformationHelper.getIdentificationIRI(elem));
		ontology.getFrames().add(o);

		Class rangeClass = (Class) etype2oclass.get(elem.getEType());
		ClassAtomic rangeClassAtomic = owlFactory.createClassAtomic();
		rangeClassAtomic.setClazz(rangeClass);
		o.getPropertyRange().add(rangeClassAtomic);

		Class domainClass = (Class) etype2oclass
				.get(elem.getEContainingClass());
		ClassAtomic domainClassAtomic = owlFactory.createClassAtomic();
		domainClassAtomic.setClazz(domainClass);
		o.getPropertyDomain().add(domainClassAtomic);

		if (elem.getUpperBound() == 1)
			o.getCharacteristics().add(Characteristic.FUNCTIONAL);

		references2objectProperties.put(elem, o);
	}

	private void transformEEnum(EEnum elem) {
		Class d = owlFactory.createClass();
		ontology.getFrames().add(d);
		d.setIri(OWLTransformationHelper.getIdentificationIRI(elem));
		etype2oclass.put(elem, d);

		IndividualsAtomic description = owlFactory.createIndividualsAtomic();
		d.getEquivalentClassesDescriptions().add(description);
		EList<EEnumLiteral> literals = elem.getELiterals();
		for (EEnumLiteral eEnumLiteral : literals) {
			transformEEnumLiteral(eEnumLiteral);
			Individual individual = (Individual) etype2oclass.get(eEnumLiteral);
			description.getIndividuals().add(individual);
		}

	}

	private void transformEEnumLiteral(EEnumLiteral eEnumLiteral) {
		Individual individual = owlFactory.createIndividual();
		individual.setIri(eEnumLiteral.toString());
		etype2oclass.put(eEnumLiteral, individual);
	}

	private void transformEDatatype(EDataType elem) {
		Datatype d = owlFactory.createDatatype();
		ontology.getFrames().add(d);
		d.setIri(OWLTransformationHelper.getIdentificationIRI(elem));
		etype2oclass.put(elem, d);

	}

	private void transformEClass(EClass elem) {
		Class c = owlFactory.createClass();
		ontology.getFrames().add(c);
		c.setIri(OWLTransformationHelper.getIdentificationIRI(elem));
		etype2oclass.put(elem, c);
	}

}
