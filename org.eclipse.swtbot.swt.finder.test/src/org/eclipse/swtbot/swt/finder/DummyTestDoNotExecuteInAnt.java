/*******************************************************************************
 *  Copyright 2007 new SWTBot, http://swtbot.org/
 * Copyright (c) 2008 Ketan Padegaonkar and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ketan Padegaonkar - initial API and implementation
 *******************************************************************************/
package org.eclipse.swtbot.swt.finder;

public class DummyTestDoNotExecuteInAnt extends SWTBotTestCase {
	public DummyTestDoNotExecuteInAnt() {
	}

	public void testThisFails() throws Exception {
		fail();
	}
}