/*******************************************************************************
 * Copyright (c) 2006-2012
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 ******************************************************************************/
package org.emftext.language.owl.test.resolving;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.compare.diff.metamodel.DiffElement;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.metamodel.MatchElement;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.compare.util.ModelUtils;
import org.eclipse.emf.ecore.EObject;

/**
 * Tells whether two models match.
 * 
 * @author Erik Tittel
 * 
 */
public final class ModelComparator {

	private EObject model1;
	private EObject model2;
	private MatchModel match;
	private DiffModel diff;
	private String fileName1 = "";
	private String fileName2 = "";

	public boolean areModelsEqualRegardingToMatch(String fileName1, String fileName2) {
		loadModels(fileName1, fileName2);
		saveFileNames(fileName1, fileName2);
		return areModelsEqualRegardingToMath(model1, model2);
	}

	public boolean areModelsEqualRegardingToDiff(String fileName1, String fileName2) {
		loadModels(fileName1, fileName2);
		saveFileNames(fileName1, fileName2);
		return areModelsEqualRegardingToDiff(model1, model2);
	}
	
	private void loadModels(String fileName1, String fileName2) {
		model1 = ModelStorageUtil.loadModelFromFileName(fileName1);
		model2 = ModelStorageUtil.loadModelFromFileName(fileName2);
	}

	private void saveFileNames(String fileName1, String fileName2) {
		this.fileName1 = fileName1;
		this.fileName2 = fileName2;
	}

	public boolean areModelsEqualRegardingToMath(EObject model1, EObject model2) {
		prepareMatchAndDiffModel(model1, model2);
		return checkIfAllElementsMatch(match.getMatchedElements());
	}
	
	public boolean areModelsEqualRegardingToDiff(EObject model1, EObject model2) {
		prepareMatchAndDiffModel(model1, model2);
		return checkIfNoDifferentElementsExist();
	}

	private void prepareMatchAndDiffModel(EObject model1, EObject model2) {
		try {
			createMatchModelFromModels(model1, model2);
			printModel("Match Model", fileName1 + ", " + fileName2, match);
			createDiffModel();
			printModel("Diff Model", fileName1 + ", " + fileName2, diff);
		} catch (InterruptedException e) {
			throw new RuntimeException("Exception during matching of models");
		}
	}

	private void createMatchModelFromModels(EObject model1, EObject model2) throws InterruptedException {
		match = MatchService.doMatch(model1, model2, Collections.<String, Object> emptyMap());
	}

	private void createDiffModel() {
		diff = DiffService.doDiff(match, false);
	}

	private void printModel(String name, String fileName, EObject object) {
		try {
			System.out.println(name + "(" + fileName + "):\n"); //$NON-NLS-1$
			System.out.println(ModelUtils.serialize(object));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean checkIfNoDifferentElementsExist() {
		EList<DiffElement> ownedElements = diff.getOwnedElements();
		EList<DiffElement> subDiffElements = ownedElements.get(0).getSubDiffElements();
		return subDiffElements.size() == 0;
	}

	/**
	 * Recursive algorithm, that checks if the similarity of all elements is
	 * 1.0.
	 */
	private boolean checkIfAllElementsMatch(EList<MatchElement> matchedElements) {
		if (matchedElements.isEmpty()) {
			return true;
		}
		boolean result = true;
		for (MatchElement matchElement : matchedElements) {
			if (matchElement.getSimilarity() == 1.0) {
				result = checkIfAllElementsMatch(matchElement.getSubMatchElements());
				if (!result) {
					break;
				}
			} else {
				result = false;
				break;
			}
		}
		return result;
	}

}
