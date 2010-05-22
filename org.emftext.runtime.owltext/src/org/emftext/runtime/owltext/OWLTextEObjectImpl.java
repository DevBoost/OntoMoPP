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

/**
 * A custom implementation of EObject that intercepts all reflective 
 * calls modifying and querying features of the concrete EObject.
 * 
 * @author cwende
 *
 */
public class OWLTextEObjectImpl extends EObjectImpl {

	/**
	 * Instance variable storing the OWL individual associated with the EObject
	 */
	private Individual owlIndividual;
	
	/**
	 * Instance variable storing the superclass of the owl individual associated 
	 * with the EObject
	 */
	private Class superclass;

	
	/** 
	 * An EList delegator that intercepts all query and modification calls 
	 * to ELists that hold feature settings for the EObject
	 * @author cwende
	 *
	 * @param <T>
	 */
	private final class OWLTextEListDelegator<T> implements EList<T> {
		private final EList<T> original;
		private OWLTextEObjectImpl thisObject;
		private int featureID;

		private OWLTextEListDelegator(EList<T> result,
				OWLTextEObjectImpl thisObject, int featureID) {
			// TODO check whether original can be finally 
			// completely removed when all methods are implemented
			this.original = result;
			this.thisObject = thisObject;
			this.featureID = featureID;
		}

		/**
		 * Intercepts the add calls on ELists and introduces the according axioms 
		 * for the associated owl individual
		 */
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

		/**
		 * Intercepts the addAll calls on ELists and introduces the according axioms 
		 * for the associated owl individual
		 */
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

		/**
		 * Intercepts the clear calls on ELists and removes the according axioms 
		 * for the associated owl individual
		 */
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

		/**
		 * Intercepts the contains calls on ELists and returns
		 * if a containment for the queried object can be derived 
		 * from the axioms of the associated owl individial
		 */
		public boolean contains(Object o) {
			// TODO implement ontology lookup
			//Test
			/*
			Individual individual = ((OWLTextEObjectImpl) o).getIndividual();
			
			EList<Description> descriptions = owlIndividual.getTypes();
			for (Description description : descriptions) {
				if (description instanceof NestedDescription) {
					ObjectPropertyValue objectPropertyValue = (ObjectPropertyValue)((NestedDescription)description).getDescription();
					if(objectPropertyValue.getIndividual().equals(individual)) return true;					
				}
			}
			return false;
			*/
			return original.contains(o);
		}

		/**
		 * Intercepts the containsAll calls on ELists and returns
		 * if a containment for the queried collection can be derived 
		 * from the axioms of the associated owl individial
		 */
		public boolean containsAll(Collection<?> c) {
			// TODO implement ontology lookup
			//Test
			/*
			for (Object o : c) {
				if(!contains(o))return false;
			}
			return true;
			*/
			return original.containsAll(c);
		}

		/**
		 * Intercepts the isEmpty calls on ELists and returns
		 * if a emptiness derived 
		 * from the axioms of the associated owl individial
		 */
		public boolean isEmpty() {
			// TODO implement ontology lookup
			//Test
			/*
			EList<Description> descriptions = owlIndividual.getTypes();
			if (descriptions.size() > 0) return false;
			for (Description description : descriptions) {
				if (description instanceof NestedDescription) {
					if(((NestedDescription)description).getDescription() != null) return false;				
				}
			}
			return true;
			*/
			return original.isEmpty();
		}

		/**
		 * Intercepts the iterator calls on ELists and returns
		 * the corresponding iterator
		 */
		public Iterator<T> iterator() {
			// TODO implement ontology lookup
			return original.iterator();
		}

		/**
		 * Intercepts the remove calls on ELists and 
		 * removes the according axioms from the ontology
		 */
		public boolean remove(Object o) {
			// TODO remove corresponding axioms to ontology
			System.out
					.println("remove: " + thisObject.eClass().getName() + "."
							+ thisObject.eDynamicFeature(featureID).getName()
							+ " " + o);
			return original.remove(o);
		}

		/**
		 * Intercepts the removeAll calls on ELists and 
		 * removes the according axioms from the ontology
		 */
		public boolean removeAll(Collection<?> c) {
			// TODO remove corresponding axioms to ontology
			System.out.println("removeAll: " + thisObject.eClass().getName()
					+ "." + thisObject.eDynamicFeature(featureID).getName()
					+ " " + c);
			return original.removeAll(c);
		}

		/**
		 * Intercepts the retainAll calls on ELists and 
		 * removes the not needed axioms from the ontology
		 */
		public boolean retainAll(Collection<?> c) {
			// TODO remove corresponding axioms to ontology
			return original.removeAll(c);
		}

		/**
		 * Intercepts the size calls on ELists and 
		 * derives the according axioms from the ontology
		 */
		public int size() {
			return original.size();
		}

		public Object[] toArray() {
			return original.toArray();
		}

		public <O> O[] toArray(O[] a) {
			return original.toArray(a);
		}

		/**
		 * Intercepts the move calls on ELists and 
		 * adapts the according axioms from the ontology
		 */
		public void move(int newPosition, T object) {
			// TODO add corresponding axioms to ontology
			System.out.println("move" + thisObject.eClass().getName() + "."
					+ thisObject.eDynamicFeature(featureID).getName());
			original.move(newPosition, object);

		}

		/**
		 * Intercepts the move calls on ELists and 
		 * adapts the according axioms from the ontology
		 */
		public T move(int newPosition, int oldPosition) {
			// TODO add corresponding axioms to ontology
			System.out.println("move" + thisObject.eClass().getName() + "."
					+ thisObject.eDynamicFeature(featureID).getName());
			return original.move(newPosition, oldPosition);
		}

		/**
		 * Intercepts the add calls on ELists and 
		 * adds the according axioms from the ontology
		 */
		public void add(int index, T element) {
			// TODO add corresponding axioms to ontology
			System.out.println("add at" + thisObject.eClass().getName() + "."
					+ thisObject.eDynamicFeature(featureID).getName());
			original.add(index, element);

		}
		
		/**
		 * Intercepts the addAll calls on ELists and 
		 * adds the according axioms from the ontology
		 */
		public boolean addAll(int index, Collection<? extends T> c) {
			// TODO add corresponding axioms to ontology
			System.out.println("addAll at" + thisObject.eClass().getName()
					+ "." + thisObject.eDynamicFeature(featureID).getName());
			return original.addAll(index, c);
		}

		/**
		 * Intercepts the get calls on ELists and 
		 * retrieves the according axioms from the ontology
		 */
		public T get(int index) {
			// TODO infer and read corresponding axioms to ontology
			System.out.println("get at" + thisObject.eClass().getName() + "."
					+ thisObject.eDynamicFeature(featureID).getName());
			return original.get(index);
		}

		/**
		 * Intercepts the indexOf calls on ELists and 
		 * lock the according axioms up in the ontology
		 */
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

		/**
		 * Intercepts the remove calls on ELists and 
		 * removes the according axioms from the ontology
		 */
		public T remove(int index) {
			// TODO remove corresponding axioms to ontology
			System.out.println("remove at" + thisObject.eClass().getName()
					+ "." + thisObject.eDynamicFeature(featureID).getName());
			return original.remove(index);
		}

		/**
		 * Intercepts the set calls on ELists and 
		 * adapts the according axioms from the ontology
		 */
		public T set(int index, T element) {
			// TODO adapt corresponding axioms to ontology
			return original.set(index, element);
		}

		public List<T> subList(int fromIndex, int toIndex) {
			return original.subList(fromIndex, toIndex);
		}

	}

	/**
	 * The public constructur to create a new owlifyable EObject
	 */
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
	 * Intercepts all eInverseRemoveCalls and adapts the according axioms in the
	 * ontology
	 * 
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
	 * Intercepts all eGet calls and retrieves the according axioms in the
	 * ontology
	 * 
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

	/**
	 * Method used to encapsulate simple EList to intercepting
	 * EList adapters
	 * 
	 */
	private <T> EList<T> encapsulate(final EList<T> original,
			OWLTextEObjectImpl thisObject, int featureID) {
		return new OWLTextEListDelegator<T>(original, thisObject, featureID);
	}

	/**
	 * Intercepts all eSet and adapts the according axioms in the
	 * ontology
	 * 
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

	/**
	 * Returns the OWL individual corresponding to this EObject
	 * 
	 */
	protected Individual getIndividual() {
		return this.owlIndividual;
	}

	/**
	 * Intercepts all eUnset calls and adapts the according axioms in the
	 * ontology
	 * 
	 */@Override
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
	 * Intercepts all eIsSet calls and adapts the checks axioms in the
	 * ontology
	 * 
	 */
	@Override
	public boolean eIsSet(int featureID) {
		// TODO infer and read corresponding axioms to ontology
		System.out.println("eIsSet: " + this.eClass().getName()
				+ this.hashCode() + "."
				+ this.eDynamicFeature(featureID).getName());
		return super.eIsSet(featureID);
	}

	/**
	 * Return a string representing the ontology derived for this eobject 
	 * and its children
	 * 
	 */
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

	/**
	 * Cleans the corresponding OWL individual from all manually fixed
	 * cardinalities
	 * 
	 */
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

	/**
	 *	Declares all imported OWL Classes representing metamodel concepts
	 */
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
