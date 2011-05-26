/*******************************************************************************
 * Copyright (c) 2006-2011
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

public interface EMFTextOWLReasoner {

	public Map<String, String> getInconsistentFrames(String content)
			throws ReasoningException;

	public List<String> getAllSuperframes(String ontologyString,
			String ontologyUri, String completionClassIri)
			throws ReasoningException;


}
