package org.emftext.runtime.owltext;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.emftext.language.owl.AnnotationProperty;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.DataProperty;
import org.emftext.language.owl.Datatype;
import org.emftext.language.owl.DatatypeReference;
import org.emftext.language.owl.Description;
import org.emftext.language.owl.DisjointClasses;
import org.emftext.language.owl.FeatureReference;
import org.emftext.language.owl.FeatureRestriction;
import org.emftext.language.owl.Frame;
import org.emftext.language.owl.Individual;
import org.emftext.language.owl.Namespace;
import org.emftext.language.owl.NestedDescription;
import org.emftext.language.owl.ObjectProperty;
import org.emftext.language.owl.ObjectPropertyExactly;
import org.emftext.language.owl.ObjectPropertyMin;
import org.emftext.language.owl.ObjectPropertySome;
import org.emftext.language.owl.ObjectPropertyValue;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.OwlFactory;
import org.emftext.runtime.owltext.transformation.Ecore2Owl;
import org.emftext.runtime.owltext.transformation.OWLTransformationHelper;

/**
 * A custom implementation of EObject that intercepts all reflective calls
 * modifying and querying features of the concrete EObject.
 * 
 * @author cwende, mrange
 * 
 */
public class OWLTextEObjectImpl extends EObjectImpl {

	public static final Class OWL_THING = OwlFactory.eINSTANCE.createClass();
	static {
		OWL_THING.setIri("owl:Thing");
	}

	/**
	 * Instance variable storing the OWL individual associated with the EObject
	 */
	private Class owlIndividualClass;

	/**
	 * Instance variable storing the superclass of the owl individual associated
	 * with the EObject
	 */
	private Class superclass;

	private List<String> createdNamings;

	/**
	 * An EList delegator that intercepts all query and modification calls to
	 * ELists that hold feature settings for the EObject
	 * 
	 * @author cwende
	 * 
	 * @param <T>
	 */
	private final class OWLTextEListDelegator<T> implements EList<T> {
		private final EList<T> original;

		private int featureID;

		private OWLTextEListDelegator(EList<T> result,
				OWLTextEObjectImpl thisObject, int featureID) {
			this.original = result;
			this.featureID = featureID;
		}

		/**
		 * Intercepts the contains calls on ELists and returns if a containment
		 * for the queried object can be derived from the axioms of the
		 * associated owl individial
		 */
		public boolean contains(Object o) {

			EStructuralFeature feature = eDynamicFeature(featureID);

			if (feature instanceof EReference) {
				if (o instanceof OWLTextEObjectImpl) {
					Class individual = ((OWLTextEObjectImpl) o)
							.getOwlIndividualClass();

					Description description = findDescriptionForReference(
							feature, individual);
					return (description != null);
				} else {
					System.out
							.println("contain: false input typ. Expect OWLTextEObjectImpl");
				}
			} else { // EAtrributes

				if (o instanceof OWLTextEObjectImpl) {
					System.out
							.println("contain: false input typ. Expect EAtrributes");
				} else {
					Description description = findDescriptionForAttribute(
							feature, o);
					return (description != null);
				}
			}
			return false;
		}

		/**
		 * Intercepts the containsAll calls on ELists and returns if a
		 * containment for the queried collection can be derived from the axioms
		 * of the associated owl individial
		 */
		public boolean containsAll(Collection<?> c) {
			for (Object o : c) {
				if (!contains(o))
					return false;
			}
			return true;
		}

		/**
		 * Intercepts the isEmpty calls on ELists and returns if a emptiness
		 * derived from the axioms of the associated owl individial
		 */
		public boolean isEmpty() {
			EList<Description> descriptions = getOwlIndividualClass()
					.getSuperClassesDescriptions();

			for (Description description : descriptions) {
				if (description instanceof NestedDescription) {
					if (isSameFeatureIRI(eDynamicFeature(featureID),
							(NestedDescription) description)) {
						return false;
					}
				}

			}
			return true;
		}

		/**
		 * Intercepts the iterator calls on ELists and returns the corresponding
		 * iterator
		 */
		public Iterator<T> iterator() {
			return (Iterator<T>) listIterator();
		}

		/**
		 * Intercepts the size calls on ELists and derives the according axioms
		 * from the ontology
		 */
		public int size() {
			EList<Description> descriptions = getOwlIndividualClass()
					.getSuperClassesDescriptions();
			int count = 0;

			for (Description description : descriptions) {
				if (description instanceof NestedDescription) {
					if (isSameFeatureIRI(eDynamicFeature(featureID),
							(NestedDescription) description)) {
						count++;
					}
				}
			}

			return count;
		}

		public Object[] toArray() {

			EList<Description> descriptions = getOwlIndividualClass()
					.getSuperClassesDescriptions();
			Object[] array = new Object[size()];
			int count = -1;

			for (Description description : descriptions) {
				if (description instanceof NestedDescription) {
					if (isSameFeatureIRI(eDynamicFeature(featureID),
							(NestedDescription) description)) {
						count++;
						array[count] = getObjectFromDescription(
								eDynamicFeature(featureID),
								(NestedDescription) description);
					}
				}
			}
			return array;
		}

		@SuppressWarnings("unchecked")
		public <AT> AT[] toArray(AT[] a) {
			int size = size();
			// look for the collection fits in the specified array
			if (a.length < size)
				a = (AT[]) Array.newInstance(a.getClass().getComponentType(),
						size);

			EList<Description> descriptions = getOwlIndividualClass()
					.getSuperClassesDescriptions();
			int count = -1;

			for (Description description : descriptions) {
				if (description instanceof NestedDescription) {
					if (isSameFeatureIRI(eDynamicFeature(featureID),
							(NestedDescription) description)) {
						count++;

						T t = getObjectFromDescription(
								eDynamicFeature(featureID),
								(NestedDescription) description);
						a[count] = (AT) t;
					}
				}
			}
			// set unused elements to null
			for (; count < a.length - 1;) {
				count++;
				a[count] = null;
			}

			return a;
		}

		/**
		 * Intercepts the get calls on ELists and retrieves the according axioms
		 * from the ontology
		 */

		public T get(int index) {
			// System.out.println("get at " + thisObject.eClass().getName() +
			// "."
			// + thisObject.eDynamicFeature(featureID).getName());

			EList<Description> descriptions = getOwlIndividualClass()
					.getSuperClassesDescriptions();

			int count = -1;
			for (Description description : descriptions) {
				if (description instanceof NestedDescription) {
					if (isSameFeatureIRI(eDynamicFeature(featureID),
							(NestedDescription) description)) {
						count++;// inkrementiere bei jeder gefundenen FeatureIRI

						if (count == index) {// Pruefe ob es sich um aktuelles
												// Object handelt
							return getObjectFromDescription(
									eDynamicFeature(featureID),
									(NestedDescription) description);
						}
					}
				}
			}
			return null; // wenn nicht enthalten
		}

		/**
		 * Intercepts the indexOf calls on ELists and lock the according axioms
		 * up in the ontology
		 */
		public int indexOf(Object o) {
			EList<Description> descriptions = getOwlIndividualClass()
					.getSuperClassesDescriptions();
			EStructuralFeature feature = eDynamicFeature(featureID);

			int count = -1;
			for (Description description : descriptions) {
				if (description instanceof NestedDescription) {
					if (isSameFeatureIRI(eDynamicFeature(featureID),
							(NestedDescription) description)) {
						count++;// inkrementiere bei jeder gefundenen FeatureIRI

						// Pruefe ob es sich um aktuelles Object handelt
						if (feature instanceof EReference) {
							if (o instanceof OWLTextEObjectImpl) {
								Class individual = ((OWLTextEObjectImpl) o)
										.getOwlIndividualClass();
								if (((NestedDescription) description)
										.getDescription() instanceof ObjectPropertySome) {

									ObjectPropertySome property = (ObjectPropertySome) ((NestedDescription) description)
											.getDescription();
									if (property.getPrimary() != null
											&& property.getPrimary() instanceof ClassAtomic) {
										ClassAtomic ca = (ClassAtomic) property
												.getPrimary();
										if (ca.getClazz().equals(individual)) {
											return count;
										}
									}
								}
							} else {
								System.out
										.println("indexOf: false input typ. Expect OWLTextEObjectImpl");
								return -1;
							}

						} else { // EAtrributes

							if (o instanceof OWLTextEObjectImpl) {
								System.out
										.println("indexOf: false input typ. Expect EAtrributes");
								return -1;
							} else {
								if (((NestedDescription) description)
										.getDescription() instanceof ObjectPropertyValue) {

									ObjectPropertyValue property = (ObjectPropertyValue) ((NestedDescription) description)
											.getDescription();

									// TODO: Literal vergleich bisher nur ueber
									// Zeichenketten
									if (property.getLiteral() != null
											&& property.getLiteral().toString()
													.contains(o.toString())) {
										return count;
									}
								}
							}
						}
					}
				}
			}
			return -1; // wenn nicht enthalten
		}

		public int lastIndexOf(Object o) {
			EList<Description> descriptions = getOwlIndividualClass()
					.getSuperClassesDescriptions();
			EStructuralFeature feature = eDynamicFeature(featureID);

			int foundAt = -1;
			int count = -1;
			for (Description description : descriptions) {
				if (description instanceof NestedDescription) {
					if (isSameFeatureIRI(eDynamicFeature(featureID),
							(NestedDescription) description)) {
						count++;// inkrementiere bei jeder gefundenen FeatureIRI

						// Pruefe ob es sich um aktuelles Object handelt
						if (feature instanceof EReference) {
							if (o instanceof OWLTextEObjectImpl) {
								Class individual = ((OWLTextEObjectImpl) o)
										.getOwlIndividualClass();
								if (((NestedDescription) description)
										.getDescription() instanceof ObjectPropertySome) {

									ObjectPropertySome property = (ObjectPropertySome) ((NestedDescription) description)
											.getDescription();
									if (property.getPrimary() != null
											&& property.getPrimary() instanceof ClassAtomic) {
										ClassAtomic ca = (ClassAtomic) property
												.getPrimary();
										if (ca.getClazz().equals(individual)) {
											foundAt = count;
										}
									}
								}
							} else {
								System.out
										.println("indexOf: false input typ. Expect OWLTextEObjectImpl");
								return -1;
							}

						} else { // EAtrributes

							if (o instanceof OWLTextEObjectImpl) {
								System.out
										.println("indexOf: false input typ. Expect EAtrributes");
								return -1;
							} else {
								if (((NestedDescription) description)
										.getDescription() instanceof ObjectPropertyValue) {

									ObjectPropertyValue property = (ObjectPropertyValue) ((NestedDescription) description)
											.getDescription();

									// TODO: Literal vergleich bisher nur ueber
									// Zeichenketten
									if (property.getLiteral() != null
											&& property.getLiteral().toString()
													.contains(o.toString())) {
										foundAt = count;
									}
								}
							}
						}
					}
				}
			}
			return foundAt;
		}

		public ListIterator<T> listIterator() {
			final OWLTextEListDelegator<T> elist = this;

			ListIterator<T> iter = new ListIterator<T>() {

				private int index = -1;
				private int size = elist.size();

				public void add(T e) {
					if (index == -1)
						index = 0;
					elist.add(index, e);
					size++;
				}

				public boolean hasNext() {
					return (index + 1 < size);
				}

				public boolean hasPrevious() {
					return (index - 1 > -1);
				}

				public T next() {
					index++;
					T t = elist.get(index);
					return t;
				}

				public int nextIndex() {
					int i = index + 1;
					return i;
				}

				public T previous() {
					index--;
					T t = elist.get(index);
					return t;
				}

				public int previousIndex() {
					int i = index - 1;
					if (i == -1)
						i = 0;
					return i;
				}

				public void remove() {
					if (index == -1)
						index = 0;
					elist.remove(index);
					size--;
				}

				public void set(T e) {
					if (index == -1)
						index = 0;
					elist.set(index, e);
				}
			};
			return iter;
		}

		public ListIterator<T> listIterator(int index) {
			if (index < 0 || index > size())
				throw new IndexOutOfBoundsException();

			ListIterator<T> iter = listIterator();
			for (int i = 0; i < index; i++)
				iter.next();

			return iter;
		}

		/**
		 * Intercepts the subList calls on ELists and adapts the according
		 * axioms from the ontology. Only for non-structural changes of Objects,
		 * not for literals
		 */
		public List<T> subList(final int fromIndex, final int toIndex) {
			return original.subList(fromIndex, toIndex);
			// return encapsulate((EList<T>) original.subList(fromIndex,
			// toIndex), this.thisObject, this.featureID);
		}

		public boolean add(T e) {
			return original.add(e);
		}

		public boolean remove(Object o) {
			return original.remove(o);
		}

		public boolean addAll(Collection<? extends T> c) {
			return original.addAll(c);
		}

		public boolean addAll(int index, Collection<? extends T> c) {
			return original.addAll(index, c);
		}

		public boolean removeAll(Collection<?> c) {
			return original.removeAll(c);
		}

		public boolean retainAll(Collection<?> c) {
			return original.retainAll(c);
		}

		public void clear() {
			original.clear();

		}

		public T set(int index, T element) {
			return original.set(index, element);
		}

		public void add(int index, T element) {
			original.add(index, element);
		}

		public T remove(int index) {
			return original.remove(index);
		}

		public void move(int newPosition, T object) {
			original.move(newPosition, object);

		}

		public T move(int newPosition, int oldPosition) {
			return original.move(newPosition, oldPosition);
		}
	}

	/**
	 * The public constructur to create a new owlifyable EObject
	 */
	public OWLTextEObjectImpl() {
		super();
		Adapter e = new OWLTextEObjectChangeAdapter(this);
		this.eAdapters().add(e);
		OwlFactory factory = OwlFactory.eINSTANCE;
		this.owlIndividualClass = factory.createClass();
		owlIndividualClass.setIri(OWLTransformationHelper
				.getObjectIdentificationIRI(this));

		this.superclass = factory.createClass();
		superclass.setIri(OWLTransformationHelper
				.getClassIdentificationIRI(this.eClass()));
		ClassAtomic classAtomic = factory.createClassAtomic();
		classAtomic.setClazz(superclass);
		owlIndividualClass.getSuperClassesDescriptions().add(classAtomic);
	}

	/**
	 * Intercepts all eGet calls and retrieves the according axioms in the
	 * ontology
	 * 
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		// System.out.println("eGet: " + this.eClass().getName() +
		// this.hashCode()
		// + "." + this.eDynamicFeature(featureID).getName());
		Object result = super.eGet(featureID, resolve, coreType);

		if (result instanceof EList<?>) {
			return encapsulate((EList<?>) result, this, featureID);
		} else {
			for (Description description : this.getOwlIndividualClass()
					.getSuperClassesDescriptions()) {
				if (description instanceof NestedDescription) {
					if (isSameFeatureIRI(eDynamicFeature(featureID),
							(NestedDescription) description)) {
						return getObjectFromDescription(
								eDynamicFeature(featureID),
								(NestedDescription) description);
					}
				}
			}

		}
		return null;
	}

	/**
	 * Method used to encapsulate simple EList to intercepting EList adapters
	 * 
	 */
	private <T> EList<T> encapsulate(final EList<T> original,
			OWLTextEObjectImpl thisObject, int featureID) {
		return new OWLTextEListDelegator<T>(original, thisObject, featureID);
	}

	/**
	 * Returns an ontology derived for this eobject and its children
	 * 
	 */
	public OntologyDocument getOWLRepresentation() {
		getOwlIndividualClass().setIri(
				OWLTransformationHelper.getObjectIdentificationIRI(this));

		OwlFactory factory = OwlFactory.eINSTANCE;
		OntologyDocument ontologyDocument = factory.createOntologyDocument();
		Ontology ontology = factory.createOntology();
		ontology.setUri(getOntologyUri());
		ontologyDocument.setOntology(ontology);
		OWLTextEObjectImpl root = this;
		while (root.eContainer() != null) {
			root = (OWLTextEObjectImpl) root.eContainer();
		}
		
		addMetamodelImportAxioms(factory, ontologyDocument, ontology, root);

		ontology.getFrames().add(root.getOwlIndividualClass());
		List<Class> individuals = new LinkedList<Class>();
		Class selfIndividual = this.getOwlIndividualClass();
		clean(selfIndividual);
		fixCardinality(this, selfIndividual);

		individuals.add(selfIndividual);
		createdNamings = new ArrayList<String>();
		createNamedEqivalent(this, selfIndividual, ontology);
		// collect individuals, fix cardinalities
		Set<EObject> allContainedAndReferencedEObjects = new HashSet<EObject>();
		allContainedAndReferencedEObjects.addAll(root.eCrossReferences());
		allContainedAndReferencedEObjects.addAll(root.eContents());
		
		addObjectsTransitively(ontology, individuals, allContainedAndReferencedEObjects);

		// uniqueness
		if (individuals.size() > 1) {
			DisjointClasses disjointClasses = factory.createDisjointClasses();
			ontology.getFrames().add(disjointClasses);
			for (Class individual : individuals) {
				ClassAtomic ca = factory.createClassAtomic();
				ca.setClazz(individual);
				disjointClasses.getDescriptions().add(ca);
			}
		}

		new Ecore2Owl().cleanTransitiveImports(ontology);

		return ontologyDocument;
	}

	private void addObjectsTransitively(Ontology ontology, List<Class> individuals,
			Set<EObject> objectsToAdd) {
		Set<EObject> containedAndReferenced = new HashSet<EObject>();
		for (EObject eObject : objectsToAdd) {
			if (!(eObject instanceof OWLTextEObjectImpl))
				continue;
			containedAndReferenced.addAll(eObject.eCrossReferences());
			containedAndReferenced.addAll(eObject.eContents());
			OWLTextEObjectImpl child = (OWLTextEObjectImpl) eObject;

			Class individual = child.getOwlIndividualClass();
			createNamedEqivalent(child, individual, ontology);

			ontology.getFrames().add(individual);
			individuals.add(individual);
			// fix cardinality for all features
			clean(individual);
			fixCardinality(child, individual);
		}
		containedAndReferenced.removeAll(objectsToAdd);
		if (containedAndReferenced.size() > 0) {
			addObjectsTransitively(ontology, individuals, containedAndReferenced);
		}
	}

	private void createNamedEqivalent(OWLTextEObjectImpl child,
			Class individual, Ontology ontology) {
		EAttribute eidAttribute = child.eClass().getEIDAttribute();
		if (eidAttribute != null) {
			OwlFactory factory = OwlFactory.eINSTANCE;
			Object id = child.eGet(eidAttribute);
			if (id != null && !"".equals(id.toString())
					&& !(createdNamings.contains(id.toString()))) {
				Class eq = factory.createClass();
				eq.setIri(OWLTransformationHelper.createValidIri(id.toString()));
				ClassAtomic ca = factory.createClassAtomic();
				ca.setClazz(individual);
				eq.getEquivalentClassesDescriptions().add(ca);
				ontology.getFrames().add(eq);
				createdNamings.add(id.toString());
			}
		}

	}

	public String getOntologyUri() {
		return "http://transformed/" + this.eResource().getURI().lastSegment();
	}

	private void fixCardinality(OWLTextEObjectImpl child, Class individual) {
		OwlFactory factory = OwlFactory.eINSTANCE;
		EList<EStructuralFeature> eStructuralFeatures = child.eClass()
				.getEAllStructuralFeatures();
		for (EStructuralFeature eStructuralFeature : eStructuralFeatures) {
			if (eStructuralFeature instanceof EAttribute
					&& ((EAttribute) eStructuralFeature).getEType() instanceof EEnum) {
				continue; // TODO finalise support for Enums and EnumLiterals in
							// OWLizing
			}
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
			if (size > 1) {
				size = 2; // all upper bound greater than 1 are handled as
							// potentially unbounded, otherwise reasoning is not
							// performant
				ObjectPropertyMin opm = factory.createObjectPropertyMin();
				ObjectProperty objectProperty = factory.createObjectProperty();
				objectProperty.setIri(OWLTransformationHelper
						.getFeatureIdentificationIRI(eStructuralFeature));
				FeatureReference featureRef = factory.createFeatureReference();
				featureRef.setFeature(objectProperty);
				opm.setFeatureReference(featureRef);
				if (eStructuralFeature instanceof EAttribute) {
					DatatypeReference primary = factory
							.createDatatypeReference();
					Datatype datatype = factory.createDatatype();
					datatype.setIri(OWLTransformationHelper.getDatatypeMap()
							.get(eStructuralFeature.getEType()
									.getInstanceTypeName()));
					primary.setTheDatatype(datatype);
					opm.setDataPrimary(primary);
				} else {
					ClassAtomic primary = factory.createClassAtomic();
					Class clazz = factory.createClass();
					clazz.setIri(OWLTransformationHelper
							.getClassIdentificationIRI(eStructuralFeature
									.getEType()));
					primary.setClazz(clazz);
					opm.setPrimary(primary);
				}

				opm.setValue(size);
				NestedDescription nestedDescription = factory
						.createNestedDescription();
				nestedDescription.setDescription(opm);
				individual.getSuperClassesDescriptions().add(nestedDescription);

			} else {
				ObjectPropertyExactly ope = factory
						.createObjectPropertyExactly();
				ObjectProperty objectProperty = factory.createObjectProperty();
				objectProperty.setIri(OWLTransformationHelper
						.getFeatureIdentificationIRI(eStructuralFeature));
				FeatureReference featureRef = factory.createFeatureReference();
				featureRef.setFeature(objectProperty);
				ope.setFeatureReference(featureRef);
				if (eStructuralFeature instanceof EAttribute) {
					DatatypeReference primary = factory
							.createDatatypeReference();
					Datatype datatype = factory.createDatatype();
					datatype.setIri(OWLTransformationHelper.getDatatypeMap()
							.get(eStructuralFeature.getEType()
									.getInstanceTypeName()));
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
				individual.getSuperClassesDescriptions().add(nestedDescription);
			}
		}
	}

	/**
	 * Cleans the corresponding OWL individual from all manually fixed
	 * cardinalities
	 * 
	 */
	private void clean(Class selfIndividual) {
		EList<Description> types = selfIndividual.getSuperClassesDescriptions();
		List<Description> toRemove = new LinkedList<Description>();

		for (Description description : types) {
			if (description instanceof NestedDescription) {
				if (((NestedDescription) description).getDescription() instanceof ObjectPropertyExactly) {
					toRemove.add(description);
				}
			}
		}
		selfIndividual.getSuperClassesDescriptions().removeAll(toRemove);
	}

	/**
	 * Declares all imported OWL Classes representing metamodel concepts
	 */
	private void addMetamodelImportAxioms(OwlFactory factory,
			OntologyDocument ontologyDocument, Ontology ontology, EObject root) {
		EPackage ePackage = root.eClass().getEPackage();

		Ontology importedMetamodelOntology = OWLTransformationHelper
				.getOntology(ePackage, root);
		// System.out.println(importedMetamodelOntology);
		ontology.getImports().add(importedMetamodelOntology);

		OntologyDocument od = (OntologyDocument) importedMetamodelOntology
				.eContainer();
		for (Namespace namespace : od.getNamespace()) {
			if ((":").equals(namespace.getPrefix())) {
				continue;
			} else {
				Namespace n = factory.createNamespace();
				n.setImportedOntology(namespace.getImportedOntology());
				n.setPrefix(namespace.getPrefix());
				ontologyDocument.getNamespace().add(n);
				addImportedFrames(factory, ontology,
						namespace.getImportedOntology(), namespace.getPrefix());
			}

		}
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
		addImportedFrames(factory, ontology, importedMetamodelOntology,
				metaModelNamespacePrefix + ":");
	}

	private void addImportedFrames(OwlFactory factory, Ontology ontology,
			Ontology importedMetamodelOntology, String metaModelNamespacePrefix) {
		EList<Frame> frames = importedMetamodelOntology.getFrames();
		for (Frame frame : frames) {
			if (frame.getIri() != null && frame.getIri().length() > 0) {
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
				declarationFrame.setIri(metaModelNamespacePrefix
						+ frame.getIri());
				ontology.getFrames().add(declarationFrame);

			}
		}
	}

	protected Description findDescriptionForAttribute(
			EStructuralFeature feature, Object o) {
		EList<Description> descriptions = getOwlIndividualClass()
				.getSuperClassesDescriptions();
		for (Description description : descriptions) {
			if (description instanceof NestedDescription) {
				if (((NestedDescription) description).getDescription() instanceof ObjectPropertyValue) {
					if (!isSameFeatureIRI(feature,
							(NestedDescription) description)) {
						continue;
					}
					ObjectPropertyValue property = (ObjectPropertyValue) ((NestedDescription) description)
							.getDescription();

					// TODO: Literal vergleich bisher nur ueber
					// Zeichenketten
					if (property.getLiteral() != null
							&& property.getLiteral().toString()
									.contains(o.toString())) {
						return description;
					}
				}
			}
		}
		return null;
	}

	protected Description findDescriptionForReference(
			EStructuralFeature feature, Class individual) {
		EList<Description> descriptions = getOwlIndividualClass()
				.getSuperClassesDescriptions();

		for (Description description : descriptions) {
			if (description instanceof NestedDescription) {
				if (((NestedDescription) description).getDescription() instanceof ObjectPropertySome) {
					if (!isSameFeatureIRI(feature,
							(NestedDescription) description)) {
						continue;
					}
					ObjectPropertySome property = (ObjectPropertySome) ((NestedDescription) description)
							.getDescription();
					if (property.getPrimary() != null
							&& property.getPrimary() instanceof ClassAtomic) {
						ClassAtomic ca = (ClassAtomic) property.getPrimary();
						if (ca.getClazz().equals(individual)) {
							return description;
						}
					}
				}
			}
		}
		return null;
	}

	protected boolean isSameFeatureIRI(EStructuralFeature feature,
			NestedDescription description) {
		if (description.getDescription() instanceof FeatureRestriction
				&& (description.getDescription() instanceof ObjectPropertyValue || description
						.getDescription() instanceof ObjectPropertySome)) {
			FeatureRestriction featureRestriction = (FeatureRestriction) description
					.getDescription();
			return (featureRestriction.getFeatureReference().getFeature()
					.getIri().equals(OWLTransformationHelper
					.getFeatureIdentificationIRI(feature)));
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private <T> T getObjectFromDescription(EStructuralFeature feature,
			NestedDescription description) {
		if (feature instanceof EReference) {
			if (description.getDescription() instanceof ObjectPropertySome) {

				ObjectPropertySome property = (ObjectPropertySome) ((NestedDescription) description)
						.getDescription();

				if (property.getPrimary() != null
						&& property.getPrimary() instanceof ClassAtomic) {
					ClassAtomic ca = (ClassAtomic) property.getPrimary();
					return (T) OWLTransformationHelper.getEObjectFromIRI(ca
							.getClazz().getIri());
				}
			}
		} else { // EAtrributes
			if (description.getDescription() instanceof ObjectPropertyValue) {
				EAttribute attribute = (EAttribute) feature;
				ObjectPropertyValue property = (ObjectPropertyValue) ((NestedDescription) description)
						.getDescription();
				if (property.getLiteral() != null)
					return (T) new LiteralConverter().reconvert(attribute
							.getEAttributeType().getInstanceClass(), property
							.getLiteral());
			}
		}
		return null;
	}

	protected Class getOwlIndividualClass() {

		return this.owlIndividualClass;
	}

}
