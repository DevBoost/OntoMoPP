/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.owl.resource.owl.analysis;

public  class FeatureReferenceFeatureReferenceResolver implements org.emftext.language.owl.resource.owl.IOwlReferenceResolver<org.emftext.language.owl.FeatureReference, org.emftext.language.owl.Feature> {
	
	private org.emftext.language.owl.resource.owl.analysis.OwlDefaultResolverDelegate<org.emftext.language.owl.FeatureReference, org.emftext.language.owl.Feature> delegate = new org.emftext.language.owl.resource.owl.analysis.OwlDefaultResolverDelegate<org.emftext.language.owl.FeatureReference, org.emftext.language.owl.Feature>();
	
	public void resolve(java.lang.String identifier, org.emftext.language.owl.FeatureReference container, org.eclipse.emf.ecore.EReference reference, int position, boolean resolveFuzzy, final org.emftext.language.owl.resource.owl.IOwlReferenceResolveResult<org.emftext.language.owl.Feature> result) {
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, result);
	}
	
	public java.lang.String deResolve(org.emftext.language.owl.Feature element, org.emftext.language.owl.FeatureReference container, org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend on any option
	}
	
}
