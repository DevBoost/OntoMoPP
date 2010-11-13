/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.owl.resource.owl.analysis;


public class OwlDefaultResolverDelegate<ContainerType extends org.eclipse.emf.ecore.EObject, ReferenceType extends org.eclipse.emf.ecore.EObject> {
	
	public final static String NAME_FEATURE = "name";
	
	/**
	 * This standard implementation searches the tree for objects of the correct type
	 * with a name attribute matching the identifier.
	 */
	protected void resolve(String identifier, ContainerType container, org.eclipse.emf.ecore.EReference reference, int position, boolean resolveFuzzy, org.emftext.language.owl.resource.owl.IOwlReferenceResolveResult<ReferenceType> result) {
		// avoid duplicate resolvings
		if (!resolveFuzzy && result.getMappings().size() > 0) return;
		try {
			org.eclipse.emf.ecore.EClass type = reference.getEReferenceType();
			org.eclipse.emf.ecore.EObject root = org.emftext.language.owl.resource.owl.util.OwlEObjectUtil.findRootContainer(container);
			// first check whether the root element matches
			boolean continueSearch = checkElement(root, type, identifier, resolveFuzzy, true, result);
			if (!continueSearch) {
				return;
			}
			// then check the contents
			for (java.util.Iterator<org.eclipse.emf.ecore.EObject> iterator = root.eAllContents(); iterator.hasNext(); ) {
				org.eclipse.emf.ecore.EObject element = iterator.next();
				continueSearch = checkElement(element, type, identifier, resolveFuzzy, true, result);
				if (!continueSearch) {
					return;
				}
			}
			org.eclipse.emf.ecore.resource.Resource resource = container.eResource();
			if (resource != null) {
				org.eclipse.emf.common.util.URI uri = getURI(identifier, resource.getURI());
				if (uri != null) {
					org.eclipse.emf.ecore.EObject element = loadResource(container.eResource().getResourceSet(), uri);
					if (element == null) {
						return;
					}
					checkElement(element, type, identifier, resolveFuzzy, false, result);
				}
			}
		} catch (java.lang.RuntimeException rte) {
			// catch exception here to prevent EMF proxy resolution from swallowing it
			rte.printStackTrace();
		}
	}
	
	private boolean checkElement(org.eclipse.emf.ecore.EObject element, org.eclipse.emf.ecore.EClass type, String identifier, boolean resolveFuzzy, boolean checkStringWise, org.emftext.language.owl.resource.owl.IOwlReferenceResolveResult<ReferenceType> result) {
		if (element.eIsProxy()) {
			return true;
		}
		
		boolean hasCorrectType = hasCorrectType(element, type.getInstanceClass());
		if (!hasCorrectType) {
			return true;
		}
		
		String match;
		// do not compare string-wise if identifier is a URI
		if (checkStringWise) {
			match = matches(element, identifier, resolveFuzzy);
		} else {
			match = identifier;
		}
		if (match == null) {
			return true;
		}
		// we can safely cast 'element' to 'ReferenceType' here, because we've checked the
		// type of 'element' against the type of the reference. unfortunately the compiler
		// does not know that this is sufficient, so we must call cast(), which is not
		// type safe by itself.
		result.addMapping(match, cast(element));
		if (!resolveFuzzy) {
			return false;
		}
		return true;
	}
	
	/**
	 * This method encapsulates an unchecked cast from EObject to ReferenceType. We
	 * cannot do this cast strictly type safe, because type parameters are erased by
	 * compilation. Thus, an instanceof check cannot be performed at runtime.
	 */
	@SuppressWarnings("unchecked")	
	private ReferenceType cast(org.eclipse.emf.ecore.EObject element) {
		return (ReferenceType) element;
	}
	
	protected String produceDeResolveErrorMessage(org.eclipse.emf.ecore.EObject refObject, org.eclipse.emf.ecore.EObject container, org.eclipse.emf.ecore.EReference reference, org.emftext.language.owl.resource.owl.IOwlTextResource resource) {
		String msg = getClass().getSimpleName() + ": " + reference.getEType().getName() + " \"" + refObject.toString() + "\" not de-resolveable";
		return msg;
	}
	
	protected String deResolve(ReferenceType element, ContainerType container, org.eclipse.emf.ecore.EReference reference) {
		return getName(element);
	}
	
	private String matches(org.eclipse.emf.ecore.EObject element, String identifier, boolean matchFuzzy) {
		// first check for attributes that have set the ID flag to true
		java.util.List<org.eclipse.emf.ecore.EStructuralFeature> features = element.eClass().getEStructuralFeatures();
		for (org.eclipse.emf.ecore.EStructuralFeature feature : features) {
			if (feature instanceof org.eclipse.emf.ecore.EAttribute) {
				org.eclipse.emf.ecore.EAttribute attribute = (org.eclipse.emf.ecore.EAttribute) feature;
				if (attribute.isID()) {
					Object attributeValue = element.eGet(attribute);
					String match = matches(identifier, attributeValue, matchFuzzy);
					if (match != null) {
						return match;
					}
				}
			}
		}
		
		// then check for an attribute that is called 'name'
		org.eclipse.emf.ecore.EStructuralFeature nameAttr = element.eClass().getEStructuralFeature(NAME_FEATURE);
		if (nameAttr instanceof org.eclipse.emf.ecore.EAttribute) {
			Object attributeValue = element.eGet(nameAttr);
			return matches(identifier, attributeValue, matchFuzzy);
		} else {
			// try any other string attribute found
			for (org.eclipse.emf.ecore.EAttribute stringAttribute : element.eClass().getEAllAttributes()) {
				if ("java.lang.String".equals(stringAttribute.getEType().getInstanceClassName())) {
					Object attributeValue = element.eGet(stringAttribute);
					String match = matches(identifier, attributeValue, matchFuzzy);
					if (match != null) {
						return match;
					}
				}
			}
			
			for (org.eclipse.emf.ecore.EOperation o : element.eClass().getEAllOperations()) {
				if (o.getName().toLowerCase().endsWith(NAME_FEATURE) && o.getEParameters().size() == 0 ) {
					String result = (String) org.emftext.language.owl.resource.owl.util.OwlEObjectUtil.invokeOperation(element, o);
					String match = matches(identifier, result, matchFuzzy);
					if (match != null) {
						return match;
					}
				}
			}
		}
		return null;
	}
	
	private String matches(String identifier, Object attributeValue, boolean matchFuzzy) {
		if (attributeValue != null && attributeValue instanceof String) {
			String name = (String) attributeValue;
			if (name.equals(identifier) || matchFuzzy) {
				return name;
			}
		}
		return null;
	}
	
	private String getName(ReferenceType element) {
		org.eclipse.emf.ecore.EStructuralFeature nameAttr = element.eClass().getEStructuralFeature(NAME_FEATURE);
		if(element.eIsProxy()) {
			String fragment = ((org.eclipse.emf.ecore.InternalEObject) element).eProxyURI().fragment();
			if (fragment != null && fragment.startsWith(org.emftext.language.owl.resource.owl.IOwlContextDependentURIFragment.INTERNAL_URI_FRAGMENT_PREFIX)) {
				fragment = fragment.substring(org.emftext.language.owl.resource.owl.IOwlContextDependentURIFragment.INTERNAL_URI_FRAGMENT_PREFIX.length());
				fragment = fragment.substring(fragment.indexOf("_") + 1);
			}
			return fragment;
		}
		else if (nameAttr instanceof org.eclipse.emf.ecore.EAttribute) {
			return (String) element.eGet(nameAttr);
		} else {
			// try any other string attribute found
			for (org.eclipse.emf.ecore.EAttribute strAttribute : element.eClass().getEAllAttributes()) {
				if (!strAttribute.isMany() &&				strAttribute.getEType().getInstanceClassName().equals("String")) {
					return (String) element.eGet(strAttribute);
				}
			}
			for (org.eclipse.emf.ecore.EOperation o : element.eClass().getEAllOperations()) {
				if (o.getName().toLowerCase().endsWith(NAME_FEATURE) && o.getEParameters().size() == 0 ) {
					String result = (String) org.emftext.language.owl.resource.owl.util.OwlEObjectUtil.invokeOperation(element, o);
					if (result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}
	
	private boolean hasCorrectType(org.eclipse.emf.ecore.EObject element, Class<?> expectedTypeClass) {
		return expectedTypeClass.isInstance(element);
	}
	
	private org.eclipse.emf.ecore.EObject loadResource(org.eclipse.emf.ecore.resource.ResourceSet resourceSet, org.eclipse.emf.common.util.URI uri) {
		try {
			org.eclipse.emf.ecore.resource.Resource resource = resourceSet.getResource(uri, true);
			org.eclipse.emf.common.util.EList<org.eclipse.emf.ecore.EObject> contents = resource.getContents();
			if (contents.size() > 0) {
				return contents.get(0);
			}
		} catch (java.lang.RuntimeException re) {
			// do nothing here. if no resource can be loaded the uriString is probably not a
			// valid resource URI
		}
		return null;
	}
	
	private org.eclipse.emf.common.util.URI getURI(String identifier, org.eclipse.emf.common.util.URI baseURI) {
		if (identifier == null) {
			return null;
		}
		try {
			org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI.createURI(identifier);
			if (uri.isRelative()) {
				uri = uri.resolve(baseURI);
			}
			return uri;
		} catch (java.lang.IllegalArgumentException iae) {
			// the identifier string is not a valid URI
			return null;
		}
	}
}
