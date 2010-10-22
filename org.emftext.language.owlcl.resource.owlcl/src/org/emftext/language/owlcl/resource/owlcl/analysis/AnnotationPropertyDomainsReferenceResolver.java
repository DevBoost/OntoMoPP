/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.owlcl.resource.owlcl.analysis;

public class AnnotationPropertyDomainsReferenceResolver implements org.emftext.language.owlcl.resource.owlcl.IOwlclReferenceResolver<org.emftext.language.owl.AnnotationProperty, org.emftext.language.owl.IRIIdentified> {
	
	private org.emftext.language.owl.resource.owl.analysis.AnnotationPropertyDomainsReferenceResolver delegate = new org.emftext.language.owl.resource.owl.analysis.AnnotationPropertyDomainsReferenceResolver();
	
	public void resolve(String identifier, org.emftext.language.owl.AnnotationProperty container, org.eclipse.emf.ecore.EReference reference, int position, boolean resolveFuzzy, final org.emftext.language.owlcl.resource.owlcl.IOwlclReferenceResolveResult<org.emftext.language.owl.IRIIdentified> result) {
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, new org.emftext.language.owl.resource.owl.IOwlReferenceResolveResult<org.emftext.language.owl.IRIIdentified>() {
			
			public boolean wasResolvedUniquely() {
				return result.wasResolvedUniquely();
			}
			
			public boolean wasResolvedMultiple() {
				return result.wasResolvedMultiple();
			}
			
			public boolean wasResolved() {
				return result.wasResolved();
			}
			
			public void setErrorMessage(String message) {
				result.setErrorMessage(message);
			}
			
			public java.util.Collection<org.emftext.language.owl.resource.owl.IOwlReferenceMapping<org.emftext.language.owl.IRIIdentified>> getMappings() {
				throw new UnsupportedOperationException();
			}
			
			public String getErrorMessage() {
				return result.getErrorMessage();
			}
			
			public void addMapping(String identifier, org.eclipse.emf.common.util.URI newIdentifier) {
				result.addMapping(identifier, newIdentifier);
			}
			
			public void addMapping(String identifier, org.eclipse.emf.common.util.URI newIdentifier, String warning) {
				result.addMapping(identifier, newIdentifier, warning);
			}
			
			public void addMapping(String identifier, org.emftext.language.owl.IRIIdentified target) {
				result.addMapping(identifier, target);
			}
			
			public void addMapping(String identifier, org.emftext.language.owl.IRIIdentified target, String warning) {
				result.addMapping(identifier, target, warning);
			}
		});
		
	}
	
	public String deResolve(org.emftext.language.owl.IRIIdentified element, org.emftext.language.owl.AnnotationProperty container, org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend
		// on any option
	}
	
}
