/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.swrl.resource.swrl.analysis;

public class IndividualPropertyAtomPropertyReferenceResolver implements org.emftext.language.swrl.resource.swrl.ISwrlReferenceResolver<org.emftext.language.swrl.IndividualPropertyAtom, org.emftext.language.owl.ObjectProperty> {
	
	private org.emftext.language.swrl.resource.swrl.analysis.SwrlDefaultResolverDelegate<org.emftext.language.swrl.IndividualPropertyAtom, org.emftext.language.owl.ObjectProperty> delegate = new org.emftext.language.swrl.resource.swrl.analysis.SwrlDefaultResolverDelegate<org.emftext.language.swrl.IndividualPropertyAtom, org.emftext.language.owl.ObjectProperty>();
	
	public void resolve(String identifier, org.emftext.language.swrl.IndividualPropertyAtom container, org.eclipse.emf.ecore.EReference reference, int position, boolean resolveFuzzy, final org.emftext.language.swrl.resource.swrl.ISwrlReferenceResolveResult<org.emftext.language.owl.ObjectProperty> result) {
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, result);
	}
	
	public String deResolve(org.emftext.language.owl.ObjectProperty element, org.emftext.language.swrl.IndividualPropertyAtom container, org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend
		// on any option
	}
	
}
