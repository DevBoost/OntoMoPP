/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.petrinets.resource.petrinets.analysis;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.emftext.language.petrinets.Expression;
import org.emftext.language.petrinets.Function;
import org.emftext.language.petrinets.FunctionCall;
import org.emftext.language.petrinets.Parameter;
import org.emftext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolveResult;

public class FunctionCallFunctionReferenceResolver
		implements
		org.emftext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolver<org.emftext.language.petrinets.FunctionCall, org.emftext.language.petrinets.Function> {

	private org.emftext.language.petrinets.resource.petrinets.analysis.PetrinetsDefaultResolverDelegate<org.emftext.language.petrinets.FunctionCall, org.emftext.language.petrinets.Function> delegate = new org.emftext.language.petrinets.resource.petrinets.analysis.PetrinetsDefaultResolverDelegate<org.emftext.language.petrinets.FunctionCall, org.emftext.language.petrinets.Function>();

	public void resolve(
			String identifier,
			org.emftext.language.petrinets.FunctionCall container,
			org.eclipse.emf.ecore.EReference reference,
			int position,
			boolean resolveFuzzy,
			final org.emftext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolveResult<org.emftext.language.petrinets.Function> result) {
		List<Function> candidates = FunctionCache.getInstance().getDeclaredFunctions(container);
		filterFunctions(candidates, container, identifier, resolveFuzzy, result);
	}

	private void filterFunctions(List<Function> candidates,
			FunctionCall container, String identifier, boolean resolveFuzzy,
			IPetrinetsReferenceResolveResult<Function> result) {
		for (Function function : candidates) {
			if (resolveFuzzy) {
				result.addMapping(function.getName(), function);
			} else {
				if (function.getName().equals(identifier)) {
					if (parametersMatch(function.getParameters(),
							container.getParameters())) {
						result.addMapping(identifier, function);
					}
				}
			}
		}

	}

	private boolean parametersMatch(EList<Parameter> expected,
			EList<Expression> found) {
		if (expected.size() != found.size()) {
			return false;
		}
		parameterloop: for (int i = 0; i < expected.size(); i++) {
			EClassifier parameterType = expected.get(i).getType();
			EClassifier type = FunctionCache.getInstance()
					.getType(found.get(i));
			if (type.getInstanceClassName().equals(parameterType.getInstanceClassName())) {
				continue;
			}
			if (parameterType instanceof EClass && type instanceof EClass) {
				EClass parameterClass = (EClass) parameterType;
				EClass typeClass = (EClass) type;
				EList<EClass> eAllSuperTypes = typeClass.getEAllSuperTypes();
				for (EClass supertype : eAllSuperTypes) {
					if (supertype.getInstanceClassName().equals(parameterClass.getInstanceClassName())) {
						break parameterloop;
					}
				}
			}
			return false;
		}
		return true;
	}

	public String deResolve(org.emftext.language.petrinets.Function element,
			org.emftext.language.petrinets.FunctionCall container,
			org.eclipse.emf.ecore.EReference reference) {
		return element.getName();
	}

	

	public void setOptions(java.util.Map<?, ?> options) {
		// save options in a field or leave method empty if this resolver does
		// not depend
		// on any option
	}

}
