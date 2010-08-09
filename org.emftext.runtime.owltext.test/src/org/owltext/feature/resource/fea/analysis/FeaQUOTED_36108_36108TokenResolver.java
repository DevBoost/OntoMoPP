/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.owltext.feature.resource.fea.analysis;

public class FeaQUOTED_36108_36108TokenResolver implements org.owltext.feature.resource.fea.IFeaTokenResolver {
	
	private org.owltext.feature.resource.fea.analysis.FeaDefaultTokenResolver defaultTokenResolver = new org.owltext.feature.resource.fea.analysis.FeaDefaultTokenResolver();
	
	public java.lang.String deResolve(java.lang.Object value, org.eclipse.emf.ecore.EStructuralFeature feature, org.eclipse.emf.ecore.EObject container) {
		java.lang.String result = defaultTokenResolver.deResolve(value, feature, container);
		result += "$l";
		result = "$l" + result;
		return result;
	}
	
	public void resolve(java.lang.String lexem, org.eclipse.emf.ecore.EStructuralFeature feature, org.owltext.feature.resource.fea.IFeaTokenResolveResult result) {
		lexem = lexem.substring(2);
		lexem = lexem.substring(0, lexem.length() - 2);
		defaultTokenResolver.resolve(lexem, feature, result);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		defaultTokenResolver.setOptions(options);
	}
	
}
