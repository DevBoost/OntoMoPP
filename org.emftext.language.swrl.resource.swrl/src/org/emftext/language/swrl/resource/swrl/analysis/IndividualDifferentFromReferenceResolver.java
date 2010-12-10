/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.swrl.resource.swrl.analysis;

public class IndividualDifferentFromReferenceResolver implements org.emftext.language.swrl.resource.swrl.ISwrlReferenceResolver<org.emftext.language.owl.Individual, org.emftext.language.owl.Individual> {
	
	private org.emftext.language.owl.resource.owl.analysis.IndividualDifferentFromReferenceResolver delegate = new org.emftext.language.owl.resource.owl.analysis.IndividualDifferentFromReferenceResolver();
	
	public void resolve(String identifier, org.emftext.language.owl.Individual container, org.eclipse.emf.ecore.EReference reference, int position, boolean resolveFuzzy, final org.emftext.language.swrl.resource.swrl.ISwrlReferenceResolveResult<org.emftext.language.owl.Individual> result) {
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, new org.emftext.language.owl.resource.owl.IOwlReferenceResolveResult<org.emftext.language.owl.Individual>() {
			
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
			
			public java.util.Collection<org.emftext.language.owl.resource.owl.IOwlReferenceMapping<org.emftext.language.owl.Individual>> getMappings() {
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
			
			public void addMapping(String identifier, org.emftext.language.owl.Individual target) {
				result.addMapping(identifier, target);
			}
			
			public void addMapping(String identifier, org.emftext.language.owl.Individual target, String warning) {
				result.addMapping(identifier, target, warning);
			}
			
			public java.util.Collection<org.emftext.language.owl.resource.owl.IOwlQuickFix> getQuickFixes() {
				return java.util.Collections.emptySet();
			}
			
			public void addQuickFix(final org.emftext.language.owl.resource.owl.IOwlQuickFix quickFix) {
				result.addQuickFix(new org.emftext.language.swrl.resource.swrl.ISwrlQuickFix() {
					
					public String getImageKey() {
						return quickFix.getImageKey();
					}
					
					public String getDisplayString() {
						return quickFix.getDisplayString();
					}
					
					public java.util.Collection<org.eclipse.emf.ecore.EObject> getContextObjects() {
						return quickFix.getContextObjects();
					}
					
					public String getContextAsString() {
						return quickFix.getContextAsString();
					}
					
					public String apply(String currentText) {
						return quickFix.apply(currentText);
					}
				});
			}
		});
		
	}
	
	public String deResolve(org.emftext.language.owl.Individual element, org.emftext.language.owl.Individual container, org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend
		// on any option
	}
	
}
