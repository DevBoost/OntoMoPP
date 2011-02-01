/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.owl.resource.owl.analysis;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.emftext.language.owl.resource.owl.IOwlTokenResolveResult;
import org.emftext.language.owl.resource.owl.analysis.custom.CharacterEscaper;

public class OwlSTRING_LITERALTokenResolver implements
		org.emftext.language.owl.resource.owl.IOwlTokenResolver {

	private org.emftext.language.owl.resource.owl.analysis.OwlDefaultTokenResolver defaultTokenResolver = new org.emftext.language.owl.resource.owl.analysis.OwlDefaultTokenResolver();

	public String deResolve(Object value, EStructuralFeature feature,
			EObject container) {
		String result = defaultTokenResolver.deResolve(value, feature,
				container);

		// escape escapes
		result = CharacterEscaper.escapeEscapedCharacters(result);

		result = '"' + result + '"';
		return result;
	}

	public void resolve(java.lang.String lexem,
			org.eclipse.emf.ecore.EStructuralFeature feature,
			IOwlTokenResolveResult result) {
		// remove double quotes
		assert lexem.charAt(0) == '"';
		assert lexem.charAt(lexem.length() - 1) == '"';
		lexem = lexem.substring(1, lexem.length() - 1);

		lexem = CharacterEscaper.unescapeEscapedCharacters(lexem);

		result.setResolvedToken(lexem);
	}

	public void setOptions(java.util.Map<?, ?> options) {
		defaultTokenResolver.setOptions(options);
	}

}
