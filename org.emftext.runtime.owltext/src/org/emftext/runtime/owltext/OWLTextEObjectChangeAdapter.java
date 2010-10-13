package org.emftext.runtime.owltext;

import java.util.Collection;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.Description;
import org.emftext.language.owl.Feature;
import org.emftext.language.owl.FeatureReference;
import org.emftext.language.owl.FeatureRestriction;
import org.emftext.language.owl.NestedDescription;
import org.emftext.language.owl.ObjectPropertySome;
import org.emftext.language.owl.ObjectPropertyValue;
import org.emftext.language.owl.OwlFactory;

import eu.most.transformation.ecore_owl.OWLTransformationHelper;

public class OWLTextEObjectChangeAdapter implements Adapter {

	private OWLTextEObjectImpl thisObject;
	private OwlFactory factory = OwlFactory.eINSTANCE;

	public OWLTextEObjectChangeAdapter(OWLTextEObjectImpl thisObject) {
		this.thisObject = thisObject;
	}

	public void notifyChanged(Notification notification) {
		switch (notification.getEventType()) {
		case Notification.ADD:
			add(notification);
			break;
		case Notification.ADD_MANY:
			addMany(notification);
			break;
		case Notification.MOVE:
			move(notification);
			break;
		case Notification.REMOVE:
			remove(notification);
			break;
		case Notification.REMOVE_MANY:
			removeMany(notification);
			break;
		case Notification.SET:
			set(notification);
			break;
		case Notification.UNSET:
			unset(notification);
			break;
		default:
			throw new RuntimeException("unhandeled Event type: " + notification);

		}

	}

	private void unset(Notification notification) {
		EStructuralFeature feature = (EStructuralFeature) notification
				.getFeature();
		EList<Description> descriptions = thisObject.getOwlIndividualClass()
				.getSuperClassesDescriptions();
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
							.equals(OWLTransformationHelper
									.getFeatureIdentificationIRI(feature))) {
						toRemove = description;
						break;
					}
				}
			}
		}
		thisObject.getOwlIndividualClass().getSuperClassesDescriptions()
				.remove(toRemove);

	}

	private void set(Notification notification) {
		if (notification.getNewValue() == null) {
			unset(notification);
			return;
		}
		if (notification.getPosition() != Notification.NO_INDEX) {
			set(notification, notification.getPosition());
			return;
		}
		if (notification.getOldValue() != null) {
			remove(notification, notification.getOldValue());
		}
		Description description;
		EStructuralFeature feature = (EStructuralFeature) notification
				.getFeature();
		Object newValue = notification.getNewValue();

		if (feature instanceof EReference) {
			description = createDescriptionForReference((EObject) newValue,
					feature);

		} else { // EAtrributes
			description = createDescriptionForAttribute(newValue, feature);
		}

		NestedDescription nestedDescription = factory.createNestedDescription();
		nestedDescription.setDescription(description);
		thisObject.getOwlIndividualClass().getSuperClassesDescriptions()
				.add(nestedDescription);

	}

	private void set(Notification notification, int position) {
		remove(notification, notification.getOldValue());
		addAtIndex(notification, notification.getNewValue());
		
	}

	private void removeMany(Notification notification) {
		Collection<Object> oldValues = (Collection<Object>) notification
				.getOldValue();
		for (Object object : oldValues) {
			remove(notification, object);
		}
	}

	private void remove(Notification notification) {
		remove(notification, notification.getOldValue());
	}

	private void addMany(Notification notification) {
		Collection<Object> objects = (Collection<Object>) notification
				.getNewValue();
		for (Object object : objects) {
			add(notification, object);
		}
	}

	private void move(Notification notification) {
		remove(notification, notification.getNewValue());
		add(notification);
	}

	private void add(Notification notification) {
		add(notification, notification.getNewValue());
	}

	private void addAtIndex(Notification notification, Object element) {

		EStructuralFeature feature = (EStructuralFeature) notification
				.getFeature();
		int index = notification.getPosition();
		Description d;

		if (feature instanceof EReference) {
			d = createDescriptionForReference((EObject) element, feature);
		} else { // EAtrributes
			d = createDescriptionForAttribute(element, feature);
		}

		NestedDescription nestedDescription = factory.createNestedDescription();
		nestedDescription.setDescription(d);

		EList<Description> descriptions = thisObject.getOwlIndividualClass()
				.getSuperClassesDescriptions();

		int count = -1;

		for (Description description : descriptions) {
			if (description instanceof NestedDescription) {
				if (thisObject.isSameFeatureIRI(feature,
						(NestedDescription) description)) {
					count++;// inkrementiere bei jeder gefundenen FeatureIRI

					if (count == index) {
						thisObject
								.getOwlIndividualClass()
								.getSuperClassesDescriptions()
								.add(descriptions.indexOf(description),
										nestedDescription);
						break;
					}
				}
			}
		}
		if ((count < 0) || (count != index)) { // falls noch kein Element
			// vorhanden oder angebener
			// index zu gross war
			thisObject.getOwlIndividualClass().getSuperClassesDescriptions()
					.add(nestedDescription);
		}

	}

	private void remove(Notification notification, Object object) {

		Description toRemove = null;

		EStructuralFeature feature = (EStructuralFeature) notification
				.getFeature();

		if (feature instanceof EReference) {
			EReference reference = (EReference) feature;
			if (object instanceof OWLTextEObjectImpl) {
				Class individual = ((OWLTextEObjectImpl) object)
						.getIndividual();
				toRemove = thisObject.findDescriptionForReference(feature,
						individual);
			} else {
				System.out
						.println("remove: false input typ. Expect OWLTextEObjectImpl");

			}

		} else { // EAtrributes

			if (object instanceof OWLTextEObjectImpl) {
				System.out
						.println("remove: false input typ. Expect EAtrributes");
			} else {
				toRemove = thisObject.findDescriptionForAttribute(feature,
						object);
			}
		}

		thisObject.getOwlIndividualClass().getSuperClassesDescriptions()
				.remove(toRemove);

	}

	private void add(Notification notification, Object object) {
		if (notification.getPosition() == Notification.NO_INDEX) {
			addAtEnd(notification, object);
		} else {
			addAtIndex(notification, object);
		}

	}

	private void addAtEnd(Notification notification, Object element) {
		EStructuralFeature feature = (EStructuralFeature) notification
				.getFeature();
		Description description;
		if (feature instanceof EReference) {
			description = createDescriptionForReference((EObject) element,
					feature);
		} else { // EAtrributes
			description = createDescriptionForAttribute(element, feature);
		}

		NestedDescription nestedDescription = factory.createNestedDescription();

		nestedDescription.setDescription(description);
		this.thisObject.getOwlIndividualClass().getSuperClassesDescriptions()
				.add(nestedDescription);

	}

	public Notifier getTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTarget(Notifier newTarget) {
		// TODO Auto-generated method stub

	}

	public boolean isAdapterForType(Object type) {
		// TODO Auto-generated method stub
		return false;
	}

	private ObjectPropertySome createDescriptionForReference(EObject e,
			EStructuralFeature feature) {
		ObjectPropertySome objectPropertySome = factory
				.createObjectPropertySome();

		Feature property;
		property = factory.createObjectProperty();

		property.setIri(OWLTransformationHelper
				.getFeatureIdentificationIRI(feature));
		FeatureReference featureRef = factory.createFeatureReference();
		featureRef.setFeature(property);
		objectPropertySome.setFeatureReference(featureRef);

		Class individual = ((OWLTextEObjectImpl) e).getIndividual();
		ClassAtomic classAtomic = factory.createClassAtomic();
		classAtomic.setClazz(individual);
		objectPropertySome.setPrimary(classAtomic);

		return objectPropertySome;
	}

	private ObjectPropertyValue createDescriptionForAttribute(Object e,
			EStructuralFeature feature) {
		ObjectPropertyValue objectPropertyValue = factory
				.createObjectPropertyValue();

		Feature property;
		property = factory.createObjectProperty();

		property.setIri(OWLTransformationHelper
				.getFeatureIdentificationIRI(feature));
		FeatureReference featureRef = factory.createFeatureReference();
		featureRef.setFeature(property);
		objectPropertyValue.setFeatureReference(featureRef);

		property = factory.createDataProperty();

		objectPropertyValue.setLiteral(new LiteralConverter().convert(e));

		return objectPropertyValue;
	}
}
