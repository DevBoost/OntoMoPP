package org.emftext.runtime.owltext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.DataProperty;
import org.emftext.language.owl.DataPropertyFact;
import org.emftext.language.owl.Fact;
import org.emftext.language.owl.Individual;
import org.emftext.language.owl.ObjectProperty;
import org.emftext.language.owl.ObjectPropertyFact;
import org.emftext.language.owl.OwlFactory;

public class OWLTextEObjectImpl extends EObjectImpl {

	private Individual owlIndividual;
	private Class superclass;
	
	
	private final class OWLTextEListDelegator<T> implements EList<T> {
		private final EList<T> original;
		private OWLTextEObjectImpl thisObject;
		private int featureID;

		private OWLTextEListDelegator(EList<T> result, OWLTextEObjectImpl thisObject, int featureID) {
			this.original = result;
			this.thisObject = thisObject;
			this.featureID = featureID;
		}
		
		public boolean add(T e) {
			// TODO check whether we also have to deal with EAtrributes here
			OwlFactory factory = OwlFactory.eINSTANCE;
			ObjectPropertyFact objectPropertyFact = factory.createObjectPropertyFact();
			
			ObjectProperty objectProperty = factory.createObjectProperty();
			objectProperty.setIri(OWLTransformationHelper.getIdentificationIRI(eDynamicFeature(featureID)));
			objectPropertyFact.setObjectProperty(objectProperty);
			
			objectPropertyFact.setIndividual(((OWLTextEObjectImpl) e).getOWLIndividual());
			
			owlIndividual.getFacts().add(objectPropertyFact);
			
			return original.add(e);
		}
	
		public boolean addAll(Collection<? extends T> c) {
			for (T t : c) {
				OwlFactory factory = OwlFactory.eINSTANCE;
				ObjectPropertyFact objectPropertyFact = factory.createObjectPropertyFact();
				
				ObjectProperty objectProperty = factory.createObjectProperty();
				objectProperty.setIri(OWLTransformationHelper.getIdentificationIRI(eDynamicFeature(featureID)));
				objectPropertyFact.setObjectProperty(objectProperty);
				
				objectPropertyFact.setIndividual(((OWLTextEObjectImpl) t).getOWLIndividual());
				
				owlIndividual.getFacts().add(objectPropertyFact);
			}

			return original.addAll(c);
		}

		public void clear() {
			EList<Fact> facts = owlIndividual.getFacts();
			List<Fact> toRemove = new ArrayList<Fact>();
			for (Fact fact : facts) {
				if (fact instanceof ObjectPropertyFact) {
					ObjectPropertyFact opf = (ObjectPropertyFact) fact;
					if (opf.getObjectProperty().getIri().equals(OWLTransformationHelper.getIdentificationIRI(eDynamicFeature(this.featureID)))) {
						toRemove.add(opf);
					}
				}
				if (fact instanceof DataPropertyFact) {
					DataPropertyFact dpf = (DataPropertyFact) fact;
					if (dpf.getDataProperty().getIri().equals(OWLTransformationHelper.getIdentificationIRI(eDynamicFeature(this.featureID)))) {
						toRemove.add(dpf);
					}
				}
			}
			owlIndividual.getFacts().removeAll(toRemove);
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
			System.out.println("remove: " + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() + " " + o);
			return original.remove(o);
		}

		public boolean removeAll(Collection<?> c) {
			// TODO remove corresponding axioms to ontology
			System.out.println("removeAll: " + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() + " " + c);
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

		public <O> O[] toArray(O[] a)  {
			return original.toArray(a);
		}

		public void move(int newPosition, T object) {
			// TODO add corresponding axioms to ontology
			System.out.println("move" + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
			original.move(newPosition, object);
			
		}

		public T move(int newPosition, int oldPosition) {
			// TODO add corresponding axioms to ontology
			System.out.println("move" + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
			return original.move(newPosition, oldPosition);
		}

		public void add(int index, T element) {
			// TODO add corresponding axioms to ontology
			System.out.println("add at" + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
			original.add(index, element);
			
		}

		public boolean addAll(int index, Collection<? extends T> c) {
			// TODO add corresponding axioms to ontology
			System.out.println("addAll at" + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
			return original.addAll(index, c);
		}

		public T get(int index) {
			// TODO infer and read corresponding axioms to ontology
			System.out.println("get at" + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
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
			System.out.println("remove at" + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
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
		this.owlIndividual =  factory.createIndividual();
		owlIndividual.setIri(OWLTransformationHelper.getIdentificationIRI(this));
	
		this.superclass = factory.createClass();
		superclass.setIri(OWLTransformationHelper.getIdentificationIRI(this.eClass()));
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
		EList<Fact> facts = this.owlIndividual.getFacts();
		Fact toRemove = null;
		for (Fact fact : facts) {
			// only object property facts hold (inverse) references
			if (fact instanceof ObjectPropertyFact) {
				ObjectPropertyFact opf = (ObjectPropertyFact) fact;
				if (opf.getObjectProperty().getIri().equals(OWLTransformationHelper.getIdentificationIRI(feature))
						&& opf.getIndividual().getIri().equals(OWLTransformationHelper.getIdentificationIRI(otherEnd)) ) {
					toRemove = opf;
					break;
				}
			}
		}
		this.owlIndividual.getFacts().remove(toRemove);
		
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
	  public Object eGet(int featureID, boolean resolve, boolean coreType)
	  {
		// TODO infer and read corresponding axioms from ontology
		System.out.println("eGet: " + this.eClass().getName() + this.hashCode() + "." + this.eDynamicFeature(featureID).getName());
		Object result = super.eGet(featureID, resolve, coreType);
		if (result instanceof EList<?>) {
			result = encapsulate((EList<?>) result, this, featureID);
		}
		return result;
	}

	private <T> EList<T> encapsulate(final EList<T> original, OWLTextEObjectImpl thisObject, int featureID) {
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
		
		if (feature instanceof EReference) {
			ObjectPropertyFact objectPropertyFact = factory.createObjectPropertyFact();
			
			ObjectProperty objectProperty = factory.createObjectProperty();
			objectProperty.setIri(OWLTransformationHelper.getIdentificationIRI(feature));
			objectPropertyFact.setObjectProperty(objectProperty);
			
			objectPropertyFact.setIndividual(((OWLTextEObjectImpl) newValue).getOWLIndividual());
			
			this.owlIndividual.getFacts().add(objectPropertyFact);
		} else {
			DataPropertyFact dataPropertyFact = factory.createDataPropertyFact();
			
			DataProperty dataProperty = factory.createDataProperty();
			dataProperty.setIri(OWLTransformationHelper.getIdentificationIRI(feature));
			dataPropertyFact.setDataProperty(dataProperty);
		
			dataPropertyFact.setLiteral(new LiteralConverter().convert(newValue));
			
			this.owlIndividual.getFacts().add(dataPropertyFact);
		}
		super.eSet(featureID, newValue);
	}

	protected Individual getOWLIndividual() {
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
		EList<Fact> facts = this.owlIndividual.getFacts();
		Fact toRemove = null;
		for (Fact fact : facts) {
			if (fact instanceof ObjectPropertyFact) {
				ObjectPropertyFact opf = (ObjectPropertyFact) fact;
				if (opf.getObjectProperty().getIri().equals(OWLTransformationHelper.getIdentificationIRI(feature))) {
					toRemove = opf;
					break;
				}
			} else if (fact instanceof DataPropertyFact) {
				DataPropertyFact dpf = (DataPropertyFact) fact;
				if (dpf.getDataProperty().getIri().equals(OWLTransformationHelper.getIdentificationIRI(feature))) {
					toRemove = dpf;
					break;
				}
			}
		}
		this.owlIndividual.getFacts().remove(toRemove);
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

	
	
	
	

}
