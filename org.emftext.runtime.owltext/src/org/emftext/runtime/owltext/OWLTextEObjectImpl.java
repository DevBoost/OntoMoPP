package org.emftext.runtime.owltext;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.emftext.language.owl.AnnotationProperty;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.DataProperty;
import org.emftext.language.owl.Datatype;
import org.emftext.language.owl.DatatypeReference;
import org.emftext.language.owl.Description;
import org.emftext.language.owl.DifferentIndividuals;
import org.emftext.language.owl.FeatureReference;
import org.emftext.language.owl.FeatureRestriction;
import org.emftext.language.owl.Frame;
import org.emftext.language.owl.Individual;
import org.emftext.language.owl.Namespace;
import org.emftext.language.owl.NestedDescription;
import org.emftext.language.owl.ObjectProperty;
import org.emftext.language.owl.ObjectPropertyExactly;
import org.emftext.language.owl.ObjectPropertySome;
import org.emftext.language.owl.ObjectPropertyValue;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.OwlFactory;
import org.emftext.language.owl.resource.owl.mopp.OwlPrinter;

import eu.most.transformation.ecore_owl.OWLTransformationHelper;

public class OWLTextEObjectImpl extends EObjectImpl {

	private Individual owlIndividual;
	private Class superclass;

	private final class OWLTextEListDelegator<T> implements EList<T> {
		private final EList<T> original;
		private OWLTextEObjectImpl thisObject;
		private int featureID;

		private OWLTextEListDelegator(EList<T> result,
				OWLTextEObjectImpl thisObject, int featureID) {
			this.original = result;
			this.thisObject = thisObject;
			this.featureID = featureID;
		}

		public boolean add(T e) {
			// TODO check whether we also have to deal with EAtrributes here
			OwlFactory factory = OwlFactory.eINSTANCE;
			ObjectPropertyValue objectPropertyValue = factory
					.createObjectPropertyValue();

			ObjectProperty objectProperty = factory.createObjectProperty();
			objectProperty.setIri(OWLTransformationHelper
					.getFeatureIdentificationIRI(eDynamicFeature(featureID)));
			FeatureReference featureRef = factory.createFeatureReference();
			featureRef.setFeature(objectProperty);
			objectPropertyValue.setFeatureReference(featureRef);
			Individual individual = ((OWLTextEObjectImpl) e).getIndividual();
			objectPropertyValue.setIndividual(individual);

			NestedDescription nestedDescription = factory
					.createNestedDescription();
			nestedDescription.setDescription(objectPropertyValue);
			owlIndividual.getTypes().add(nestedDescription);

			return original.add(e);
		}

		public boolean addAll(Collection<? extends T> c) {
			for (T t : c) {
				OwlFactory factory = OwlFactory.eINSTANCE;

				ObjectPropertyValue objectPropertyValue = factory
						.createObjectPropertyValue();

				ObjectProperty objectProperty = factory.createObjectProperty();
				objectProperty
						.setIri(OWLTransformationHelper
								.getFeatureIdentificationIRI(eDynamicFeature(featureID)));
				FeatureReference featureRef = factory.createFeatureReference();
				featureRef.setFeature(objectProperty);
				objectPropertyValue.setFeatureReference(featureRef);
				Individual individual = ((OWLTextEObjectImpl) t)
						.getIndividual();
				objectPropertyValue.setIndividual(individual);

				NestedDescription nestedDescription = factory
						.createNestedDescription();
				nestedDescription.setDescription(objectPropertyValue);
				owlIndividual.getTypes().add(nestedDescription);

			}

			return original.addAll(c);
		}

		public void clear() {
			EList<Description> descriptions = owlIndividual.getTypes();
			List<Description> toRemove = new ArrayList<Description>();
			for (Description description : descriptions) {
				if (description instanceof NestedDescription) {
					if (((NestedDescription) description).getDescription() instanceof FeatureRestriction) {
						FeatureRestriction featureRestriction = (FeatureRestriction) ((NestedDescription) description)
								.getDescription();
						if (featureRestriction
								.getFeatureReference()
								.getFeature()
								.getIri()
								.equals(
										OWLTransformationHelper
												.getFeatureIdentificationIRI(eDynamicFeature(this.featureID)))) {
							toRemove.add(description);
						}
					}
				}

			}
			owlIndividual.getTypes().removeAll(toRemove);
			original.clear();
		}

		public boolean contains(Object o) {
			return original.contains(o);
		}

		public boolean containsAll(Collection<?> c) {
			return original.containsAll(c);
		}

		public boolean isEmpty() {
			return original.isEmpty();
		}

		public Iterator<T> iterator() {
			return original.iterator();
		}

		public boolean remove(Object o) {
			// TODO remove corresponding axioms to ontology
			System.out
					.println("remove: " + thisObject.eClass().getName() + "."
							+ thisObject.eDynamicFeature(featureID).getName()
							+ " " + o);
			return original.remove(o);
		}

		public boolean removeAll(Collection<?> c) {
			// TODO remove corresponding axioms to ontology
			System.out.println("removeAll: " + thisObject.eClass().getName()
					+ "." + thisObject.eDynamicFeature(featureID).getName()
					+ " " + c);
			return original.removeAll(c);
		}

		public boolean retainAll(Collection<?> c) {
			// TODO remove corresponding axioms to ontology
			return original.removeAll(c);
		}

		public int size() {
			return original.size();
		}

		public Object[] toArray() {
			return original.toArray();
		}

		public <O> O[] toArray(O[] a) {
			return original.toArray(a);
		}

		public void move(int newPosition, T object) {
			// TODO add corresponding axioms to ontology
			System.out.println("move" + thisObject.eClass().getName() + "."
					+ thisObject.eDynamicFeature(featureID).getName());
			original.move(newPosition, object);

		}

		public T move(int newPosition, int oldPosition) {
			// TODO add corresponding axioms to ontology
			System.out.println("move" + thisObject.eClass().getName() + "."
					+ thisObject.eDynamicFeature(featureID).getName());
			return original.move(newPosition, oldPosition);
		}

		public void add(int index, T element) {
			// TODO add corresponding axioms to ontology
			System.out.println("add at" + thisObject.eClass().getName() + "."
					+ thisObject.eDynamicFeature(featureID).getName());
			original.add(index, element);

		}

		public boolean addAll(int index, Collection<? extends T> c) {
			// TODO add corresponding axioms to ontology
			System.out.println("addAll at" + thisObject.eClass().getName()
					+ "." + thisObject.eDynamicFeature(featureID).getName());
			return original.addAll(index, c);
		}

		public T get(int index) {
			// TODO infer and read corresponding axioms to ontology
			System.out.println("get at" + thisObject.eClass().getName() + "."
					+ thisObject.eDynamicFeature(featureID).getName());
			return original.get(index);
		}

		public int indexOf(Object o) {
			return original.indexOf(o);
		}

		public int lastIndexOf(Object o) {
			return original.lastIndexOf(o);
		}

		public ListIterator<T> listIterator() {
			return original.listIterator();
		}

		public ListIterator<T> listIterator(int index) {
			return original.listIterator(index);
		}

		public T remove(int index) {
			// TODO remove corresponding axioms to ontology
			System.out.println("remove at" + thisObject.eClass().getName()
					+ "." + thisObject.eDynamicFeature(featureID).getName());
			return original.remove(index);
		}

		public T set(int index, T element) {
			// TODO adapt corresponding axioms to ontology
			return original.set(index, element);
		}

		public List<T> subList(int fromIndex, int toIndex) {
			return original.subList(fromIndex, toIndex);
		}

	}

	public OWLTextEObjectImpl() {
		super();
		OwlFactory factory = OwlFactory.eINSTANCE;
		this.owlIndividual = factory.createIndividual();
		owlIndividual.setIri(OWLTransformationHelper
				.getObjectIdentificationIRI(this));

		this.superclass = factory.createClass();
		superclass.setIri(OWLTransformationHelper
				.getClassIdentificationIRI(this.eClass()));
		ClassAtomic classAtomic = factory.createClassAtomic();
		classAtomic.setClazz(superclass);
		owlIndividual.getTypes().add(classAtomic);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd,
			int featureID, NotificationChain msgs) {
		EStructuralFeature feature = this.eDynamicFeature(featureID);
		EList<Description> descriptions = this.owlIndividual.getTypes();
		ObjectPropertySome toRemove = null;
		for (Description description : descriptions) {
			// only object property facts hold (inverse) references
			if (description instanceof NestedDescription) {
				if (((NestedDescription) description).getDescription() instanceof ObjectPropertySome) {
					ObjectPropertySome ops = (ObjectPropertySome) ((NestedDescription) description)
							.getDescription();
					if (ops.getFeatureReference().getFeature().getIri().equals(
							OWLTransformationHelper
									.getFeatureIdentificationIRI(feature))
							&& ((ClassAtomic) ops.getPrimary())
									.getClazz()
									.getIri()
									.equals(
											OWLTransformationHelper
													.getObjectIdentificationIRI(otherEnd))) {
						toRemove = ops;
						break;
					}
				}
			}

		}
		this.owlIndividual.getTypes().remove(toRemove);

		System.out.println("eInverseRemove: " + this.eClass().getName()
				+ this.hashCode() + "."
				+ this.eDynamicFeature(featureID).getName() + " " + otherEnd);
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		// TODO infer and read corresponding axioms from ontology
		System.out.println("eGet: " + this.eClass().getName() + this.hashCode()
				+ "." + this.eDynamicFeature(featureID).getName());
		Object result = super.eGet(featureID, resolve, coreType);
		if (result instanceof EList<?>) {
			result = encapsulate((EList<?>) result, this, featureID);
		}
		return result;
	}

	private <T> EList<T> encapsulate(final EList<T> original,
			OWLTextEObjectImpl thisObject, int featureID) {
		return new OWLTextEListDelegator<T>(original, thisObject, featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */

	@Override
	public void eSet(int featureID, Object newValue) {
		EStructuralFeature feature = this.eDynamicFeature(featureID);
		OwlFactory factory = OwlFactory.eINSTANCE;
		if (feature.getUpperBound() == 1) {
			eUnset(feature);
		}
		if (newValue == null)
			return;

		if (feature instanceof EReference) {
			ObjectPropertyValue objectPropertyValue = factory
					.createObjectPropertyValue();

			ObjectProperty objectProperty = factory.createObjectProperty();
			objectProperty.setIri(OWLTransformationHelper
					.getFeatureIdentificationIRI(feature));
			FeatureReference featureRef = factory.createFeatureReference();
			featureRef.setFeature(objectProperty);
			objectPropertyValue.setFeatureReference(featureRef);
			Individual individual = ((OWLTextEObjectImpl) newValue)
					.getIndividual();
			objectPropertyValue.setIndividual(individual);

			NestedDescription nestedDescription = factory
					.createNestedDescription();
			nestedDescription.setDescription(objectPropertyValue);
			this.owlIndividual.getTypes().add(nestedDescription);
		} else {
			ObjectPropertyValue objectPropertyValue = factory
					.createObjectPropertyValue();

			DataProperty dataProperty = factory.createDataProperty();
			dataProperty.setIri(OWLTransformationHelper
					.getFeatureIdentificationIRI(feature));
			FeatureReference featureRef = factory.createFeatureReference();
			featureRef.setFeature(dataProperty);
			objectPropertyValue.setFeatureReference(featureRef);

			objectPropertyValue.setLiteral(new LiteralConverter()
					.convert(newValue));

			NestedDescription nestedDescription = factory
					.createNestedDescription();
			nestedDescription.setDescription(objectPropertyValue);
			this.owlIndividual.getTypes().add(nestedDescription);
		}
		super.eSet(featureID, newValue);
	}

	protected Individual getIndividual() {
		return this.owlIndividual;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		EStructuralFeature feature = this.eDynamicFeature(featureID);
		EList<Description> descriptions = this.owlIndividual.getTypes();
		Description toRemove = null;
		for (Description description : descriptions) {
			if (description instanceof NestedDescription) {
				if (((NestedDescription) description).getDescription() instanceof FeatureRestriction) {
					FeatureRestriction featureRestriction = (FeatureRestriction) ((NestedDescription) description)
							.getDescription();
					if (featureRestriction
							.getFeatureReference()
							.getFeature()
							.getIri()
							.equals(
									OWLTransformationHelper
											.getFeatureIdentificationIRI(feature))) {
						toRemove = description;
						break;
					}
				}
			}
		}
		this.owlIndividual.getTypes().remove(toRemove);
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		// TODO infer and read corresponding axioms to ontology
		System.out.println("eIsSet: " + this.eClass().getName()
				+ this.hashCode() + "."
				+ this.eDynamicFeature(featureID).getName());
		return super.eIsSet(featureID);
	}

	public String getOWLRepresentation(OWLTextEObjectImpl eobject) {
		OwlFactory factory = OwlFactory.eINSTANCE;
		OntologyDocument ontologyDocument = factory.createOntologyDocument();
		Ontology ontology = factory.createOntology();
		ontology.setUri("http://transformed/"
				+ eobject.eResource().getURI().lastSegment());
		ontologyDocument.setOntology(ontology);
		OWLTextEObjectImpl root = eobject;
		while (root.eContainer() != null) {
			root = (OWLTextEObjectImpl) root.eContainer();
		}

		addMetamodelImportAxioms(factory, ontologyDocument, ontology, root);

		ontology.getFrames().add(root.getIndividual());
		TreeIterator<EObject> eAllContents = root.eAllContents();
		List<Individual> individuals = new LinkedList<Individual>();
		Individual selfIndividual = this.getIndividual();
		clean(selfIndividual);
		fixCardinality(factory, this, selfIndividual);

		individuals.add(selfIndividual);
		// collect individuals, fix cardinalities
		while (eAllContents.hasNext()) {
			OWLTextEObjectImpl child = (OWLTextEObjectImpl) eAllContents.next();

			Individual individual = child.getIndividual();
			ontology.getFrames().add(individual);
			individuals.add(individual);

			// fix cardinality for all features
			clean(individual);
			fixCardinality(factory, child, individual);
		}

		// uniqueness
		if (individuals.size() > 1) {
			DifferentIndividuals differentIndividuals = factory
					.createDifferentIndividuals();
			ontology.getFrames().add(differentIndividuals);
			for (Individual individual : individuals) {
				differentIndividuals.getIndividuals().add(individual);
			}
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		OwlPrinter printer = new OwlPrinter(outStream, null);
		printer.print(ontologyDocument);
		String string = outStream.toString();
		return string;
	}

	private void fixCardinality(OwlFactory factory, OWLTextEObjectImpl child,
			Individual individual) {
		EList<EStructuralFeature> eStructuralFeatures = child.eClass()
				.getEAllStructuralFeatures();
		for (EStructuralFeature eStructuralFeature : eStructuralFeatures) {

			Object value = child.eGet(eStructuralFeature);
			int size = 0;
			if (value instanceof Collection<?>) {
				Collection<?> c = (Collection<?>) value;
				size = c.size();
			} else if (value == null) {
				size = 0;
			} else {
				size = 1;
			}

			ObjectPropertyExactly ope = factory.createObjectPropertyExactly();
			ObjectProperty objectProperty = factory.createObjectProperty();
			objectProperty.setIri(OWLTransformationHelper
					.getFeatureIdentificationIRI(eStructuralFeature));
			FeatureReference featureRef = factory.createFeatureReference();
			featureRef.setFeature(objectProperty);
			ope.setFeatureReference(featureRef);
			if (eStructuralFeature instanceof EAttribute) {
				DatatypeReference primary = factory.createDatatypeReference();
				Datatype datatype = factory.createDatatype();
				datatype.setIri(OWLTransformationHelper.getDatatypeMap().get(
						eStructuralFeature.getEType().getInstanceTypeName()));
				primary.setTheDatatype(datatype);
				ope.setDataPrimary(primary);
			} else {
				ClassAtomic primary = factory.createClassAtomic();
				Class clazz = factory.createClass();
				clazz.setIri(OWLTransformationHelper
						.getClassIdentificationIRI(eStructuralFeature
								.getEType()));
				primary.setClazz(clazz);
				ope.setPrimary(primary);
			}

			ope.setValue(size);
			NestedDescription nestedDescription = factory
					.createNestedDescription();
			nestedDescription.setDescription(ope);
			individual.getTypes().add(nestedDescription);

		}
	}

	private void clean(Individual individual) {
		EList<Description> types = individual.getTypes();
		List<Description> toRemove = new LinkedList<Description>();

		for (Description description : types) {
			if (description instanceof NestedDescription) {
				if (((NestedDescription) description).getDescription() instanceof ObjectPropertyExactly) {
					toRemove.add(description);
				}
			}
		}
		individual.getTypes().removeAll(toRemove);
	}

	private void addMetamodelImportAxioms(OwlFactory factory,
			OntologyDocument ontologyDocument, Ontology ontology, EObject root) {
		EPackage ePackage = root.eClass().getEPackage();

		Ontology importedMetamodelOntology = OWLTransformationHelper
				.getOntology(ePackage, root);
		ontology.getImports().add(importedMetamodelOntology);

		Namespace local = factory.createNamespace();
		local.setPrefix(":");
		local.setImportedOntology(ontology);
		ontologyDocument.getNamespace().add(local);

		Namespace namespace = factory.createNamespace();
		String metaModelNamespacePrefix = OWLTransformationHelper
				.getNamespacePrefix(ePackage);
		namespace.setPrefix(metaModelNamespacePrefix + ":");
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
				} else if (frame instanceof DataProperty) {
					declarationFrame = factory.createDataProperty();
				} else if (frame instanceof Datatype) {
					declarationFrame = factory.createDatatype();
				} else if (frame instanceof AnnotationProperty) {
					declarationFrame = factory.createAnnotationProperty();
				} else if (frame instanceof Individual) {
					declarationFrame = factory.createIndividual();
				}
				declarationFrame.setIri(metaModelNamespacePrefix + ":"
						+ frame.getIri());
				ontology.getFrames().add(declarationFrame);

			}
		}
	}

}
