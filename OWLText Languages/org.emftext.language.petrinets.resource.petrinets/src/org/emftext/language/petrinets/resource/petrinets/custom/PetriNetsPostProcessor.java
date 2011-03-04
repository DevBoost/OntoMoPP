package org.emftext.language.petrinets.resource.petrinets.custom;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.petrinets.BooleanExpression;
import org.emftext.language.petrinets.Function;
import org.emftext.language.petrinets.FunctionCall;
import org.emftext.language.petrinets.FunctionType;
import org.emftext.language.petrinets.Setting;
import org.emftext.language.petrinets.SettingOperator;
import org.emftext.language.petrinets.resource.petrinets.IPetrinetsOptionProvider;
import org.emftext.language.petrinets.resource.petrinets.IPetrinetsOptions;
import org.emftext.language.petrinets.resource.petrinets.IPetrinetsResourcePostProcessor;
import org.emftext.language.petrinets.resource.petrinets.IPetrinetsResourcePostProcessorProvider;
import org.emftext.language.petrinets.resource.petrinets.PetrinetsEProblemType;
import org.emftext.language.petrinets.resource.petrinets.analysis.FunctionCallAnalysisHelper;
import org.emftext.language.petrinets.resource.petrinets.mopp.PetrinetsResource;

public class PetriNetsPostProcessor implements IPetrinetsOptionProvider,
		IPetrinetsResourcePostProcessor,
		IPetrinetsResourcePostProcessorProvider {

	public IPetrinetsResourcePostProcessor getResourcePostProcessor() {
		return this;
	}

	public void process(PetrinetsResource resource) {
		EcoreUtil.resolveAll(resource);
		TreeIterator<EObject> allContents = resource.getAllContents();
		while (allContents.hasNext()) {
			EObject eObject = (EObject) allContents.next();
			if (eObject instanceof FunctionCall) {
				FunctionCall fc = (FunctionCall) eObject;
				Function function = fc.getFunction();
				if (!function.eIsProxy()
						&& function.getFunctionType()
								.equals(FunctionType.WRITE)) {
					resource.addError(
							"The invocation of functions that write data is not allowed.",
							PetrinetsEProblemType.ANALYSIS_PROBLEM, fc);
				}
			} else if (eObject instanceof BooleanExpression) {
				BooleanExpression be = (BooleanExpression) eObject;
				EClassifier leftType = FunctionCallAnalysisHelper.getInstance().getType(
						be.getLeft());
				if (leftType != null
						&& !leftType.getInstanceTypeName().equals("boolean")) {
					resource.addError(
							"Arguments to boolean expressions need to have a boolean return type.",
							PetrinetsEProblemType.ANALYSIS_PROBLEM,
							be.getLeft());
				}
				EClassifier rightType = FunctionCallAnalysisHelper.getInstance().getType(
						be.getRight());
				if (rightType != null
						&& !rightType.getInstanceTypeName().equals("boolean")) {
					resource.addError(
							"Arguments to boolean expressions need to have a boolean return type.",
							PetrinetsEProblemType.ANALYSIS_PROBLEM,
							be.getRight());
				}
			} else if (eObject instanceof Setting) {
				Setting setting = (Setting) eObject;
				if (setting.getSettingOperator().equals(SettingOperator.ADD)) {
					if (setting.getFeature() != null
							&& !setting.getFeature().eIsProxy()
							&& !setting.getFeature().isMany()) {
						resource.addError(
								"You can only add to features of type List.",
								PetrinetsEProblemType.ANALYSIS_PROBLEM, setting);
					}
				} else if (setting.getSettingOperator().equals(
						SettingOperator.ASSIGN)) {
					if (setting.getFeature() != null
							&& !setting.getFeature().eIsProxy()
							&& setting.getFeature().isMany()) {
						resource.addError(
								"You can assign values to features of non-List type.",
								PetrinetsEProblemType.ANALYSIS_PROBLEM, setting);
					}
				}

			}

		}

	}

	public void terminate() {
		// TODO Auto-generated method stub

	}

	public Map<?, ?> getOptions() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(IPetrinetsOptions.RESOURCE_POSTPROCESSOR_PROVIDER, this);
		return map;
	}

}