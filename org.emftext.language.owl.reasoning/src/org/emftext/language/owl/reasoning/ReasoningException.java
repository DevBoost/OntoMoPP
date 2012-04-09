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
package org.emftext.language.owl.reasoning;

public class ReasoningException extends Exception {

	public ReasoningException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReasoningException(String message) {
		super(message);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -1339374607465418809L;

}
