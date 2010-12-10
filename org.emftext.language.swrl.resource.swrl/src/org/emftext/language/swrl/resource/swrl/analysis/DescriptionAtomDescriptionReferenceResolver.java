/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.swrl.resource.swrl.analysis;

public class DescriptionAtomDescriptionReferenceResolver implements org.emftext.language.swrl.resource.swrl.ISwrlReferenceResolver<org.emftext.language.swrl.DescriptionAtom, org.emftext.language.owl.Description> {
	
	private org.emftext.language.swrl.resource.swrl.analysis.SwrlDefaultResolverDelegate<org.emftext.language.swrl.DescriptionAtom, org.emftext.language.owl.Description> delegate = new org.emftext.language.swrl.resource.swrl.analysis.SwrlDefaultResolverDelegate<org.emftext.language.swrl.DescriptionAtom, org.emftext.language.owl.Description>();
	
	public void resolve(String identifier, org.emftext.language.swrl.DescriptionAtom container, org.eclipse.emf.ecore.EReference reference, int position, boolean resolveFuzzy, final org.emftext.language.swrl.resource.swrl.ISwrlReferenceResolveResult<org.emftext.language.owl.Description> result) {
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, result);
	}
	
	public String deResolve(org.emftext.language.owl.Description element, org.emftext.language.swrl.DescriptionAtom container, org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend
		// on any option
	}
	
}
