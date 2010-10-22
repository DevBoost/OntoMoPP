/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.owlcl.resource.owlcl.analysis;

public class OWLCLSpecConstrainedMetamodelReferenceResolver implements org.emftext.language.owlcl.resource.owlcl.IOwlclReferenceResolver<org.emftext.language.owlcl.OWLCLSpec, org.eclipse.emf.ecore.EPackage> {
	
	private org.emftext.language.owlcl.resource.owlcl.analysis.OwlclDefaultResolverDelegate<org.emftext.language.owlcl.OWLCLSpec, org.eclipse.emf.ecore.EPackage> delegate = new org.emftext.language.owlcl.resource.owlcl.analysis.OwlclDefaultResolverDelegate<org.emftext.language.owlcl.OWLCLSpec, org.eclipse.emf.ecore.EPackage>();
	
	public void resolve(String identifier, org.emftext.language.owlcl.OWLCLSpec container, org.eclipse.emf.ecore.EReference reference, int position, boolean resolveFuzzy, final org.emftext.language.owlcl.resource.owlcl.IOwlclReferenceResolveResult<org.eclipse.emf.ecore.EPackage> result) {
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, result);
	}
	
	public String deResolve(org.eclipse.emf.ecore.EPackage element, org.emftext.language.owlcl.OWLCLSpec container, org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend
		// on any option
	}
	
}
