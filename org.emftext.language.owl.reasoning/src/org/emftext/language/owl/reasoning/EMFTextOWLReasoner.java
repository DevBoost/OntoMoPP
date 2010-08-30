/*******************************************************************************
 * Copyright (c) 2006-2010 
 * Software Technology Group, Dresden University of Technology
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *      - initial API and implementation
 ******************************************************************************/
package org.emftext.language.owl.reasoning;

import java.util.List;
import java.util.Map;

import org.emftext.language.owl.Ontology;

public interface EMFTextOWLReasoner {

	public Map<String, String> getInconsistentFrames(String content)
			throws ReasoningException;

	public List<String> getInferredSuperframes(String ontologyString, Ontology ontology, String completionClassIri);

}
