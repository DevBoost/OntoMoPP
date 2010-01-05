package org.emftext.runtime.owltext;

import java.awt.PageAttributes.OriginType;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;

public class OWLTextEObjectImpl extends EObjectImpl {

	private final class OWLTextEListDelegator implements EList {
		private final EList original;
		private OWLTextEObjectImpl thisObject;
		private int featureID;

		private OWLTextEListDelegator(EList result, OWLTextEObjectImpl thisObject, int featureID) {
			this.original = result;
			this.thisObject = thisObject;
			this.featureID = featureID;
		}

		public boolean add(Object e) {
			System.out.println("add: " + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() + " " + e);
			
			return original.add(e);
		}

		public boolean addAll(Collection c) {
			System.out.println("addAll: " + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() + " " + c);
			
			return original.addAll(c);
		}

		public void clear() {
			System.out.println("clear: " + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
			
			
			original.clear();
		}

		public boolean contains(Object o) {
			return original.contains(o);
		}

		public boolean containsAll(Collection c) {
			return original.containsAll(c);
		}

		public boolean isEmpty() {
			return original.isEmpty();
		}

		public Iterator iterator() {
			return original.iterator();
		}

		public boolean remove(Object o) {
			System.out.println("remove: " + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() + " " + o);
			
			return original.remove(o);
		}

		public boolean removeAll(Collection c) {
			System.out.println("removeAll: " + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() + " " + c);
			
			return original.removeAll(c);
		}

		public boolean retainAll(Collection c) {
			return original.removeAll(c);
		}

		public int size() {
			return original.size();
		}

		public Object[] toArray() {
			return original.toArray();
		}

		public Object[] toArray(Object[] a) {
			return original.toArray(a);
		}

		public void move(int newPosition, Object object) {
			System.out.println("move" + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
			
			
			original.move(newPosition, object);
			
		}

		public Object move(int newPosition, int oldPosition) {
			System.out.println("move" + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
			
			return original.move(newPosition, oldPosition);
		}

		public void add(int index, Object element) {
			System.out.println("add at" + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
			
			original.add(index, element);
			
		}

		public boolean addAll(int index, Collection c) {
			System.out.println("addAll at" + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
			
			return original.addAll(index, c);
		}

		public Object get(int index) {
			System.out.println("get at" + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
			
			return original.get(index);
		}

		public int indexOf(Object o) {
			return original.indexOf(o);
		}

		public int lastIndexOf(Object o) {
			return original.lastIndexOf(o);
		}

		public ListIterator listIterator() {
			return original.listIterator();
		}

		public ListIterator listIterator(int index) {
			return original.listIterator(index);
		}

		public Object remove(int index) {
			System.out.println("remove at" + thisObject.eClass().getName() +"." + thisObject.eDynamicFeature(featureID).getName() );
			
			return original.remove(index);
		}

		public Object set(int index, Object element) {
			return original.set(index, element);
		}

		public List subList(int fromIndex, int toIndex) {
			return original.subList(fromIndex, toIndex);
		}

		
	}

	public OWLTextEObjectImpl() {
		super();
		System.out.println("> construct " + this.eClass().getName()
				+ this.hashCode());

	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd,
			int featureID, NotificationChain msgs) {
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
		System.out.println("eGet: " + this.eClass().getName() + this.hashCode() + "." + this.eDynamicFeature(featureID).getName());
		Object result = super.eGet(featureID, resolve, coreType);
		if (result instanceof EList) {
			result = encapsulate((EList) result, this, featureID);
		}
		return result;
	}

	private EList encapsulate(final EList original, OWLTextEObjectImpl thisObject, int featureID) {
		return new OWLTextEListDelegator(original, thisObject, featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		System.out.println("eSet: " + this.eClass().getName() + this.hashCode()
				+ "." + this.eDynamicFeature(featureID).getName() + " "
				+ newValue);
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		System.out.println("eUnset: " + this.eClass().getName()
				+ this.hashCode() + "."
				+ this.eDynamicFeature(featureID).getName());
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		System.out.println("eIsSet: " + this.eClass().getName()
				+ this.hashCode() + "."
				+ this.eDynamicFeature(featureID).getName());
		return super.eIsSet(featureID);
	}

}
