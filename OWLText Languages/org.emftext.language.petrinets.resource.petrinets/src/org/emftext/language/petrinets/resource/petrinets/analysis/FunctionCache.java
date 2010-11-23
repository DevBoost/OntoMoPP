package org.emftext.language.petrinets.resource.petrinets.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.emftext.language.petrinets.Function;
import org.emftext.language.petrinets.Parameter;
import org.emftext.language.petrinets.PetrinetsFactory;
import org.emftext.language.petrinets.impl.PetrinetsFactoryImpl;

public class FunctionCache {

	private static FunctionCache theInstance;
	Map<EClassifier, List<Function>> functionCache ;
	
	private FunctionCache() {
		functionCache = new HashMap<EClassifier, List<Function>>();
	}
	
	public static FunctionCache getInstance() {
		if (theInstance == null) {
			theInstance = new FunctionCache();
		}
		return theInstance;
	}

	public void addFunctions(List<Function> functions, EClassifier type) {
		List<Function> cachedFunctions = functionCache.get(type);
		if (cachedFunctions == null) {
			cachedFunctions = calculateFunctions(type);
			functionCache.put(type, cachedFunctions);
		}
		functions.addAll(cachedFunctions);
	}

	private List<Function> calculateFunctions(EClassifier type) {
		List<Function> functions = new ArrayList<Function>();
		if (type instanceof EClass) {
			EClass cls = (EClass) type;
			EList<EStructuralFeature> eStructuralFeatures = cls.getEAllStructuralFeatures();
			for (EStructuralFeature eStructuralFeature : eStructuralFeatures) {
				PetrinetsFactory factory = PetrinetsFactoryImpl.eINSTANCE;
				Function getter = factory.createFunction();
				getter.setName("get" + eStructuralFeature.getName());
				getter.setType(eStructuralFeature.getEType());
				functions.add(getter);
				
				
				Function setter = factory.createFunction();
				getter.setName("set" + eStructuralFeature.getName());
				Parameter parameter = factory.createParameter();
				parameter.setType(eStructuralFeature.getEType());
				getter.getParameters().add(parameter);
				functions.add(setter);
			}
		}
		return functions;
	}
}
