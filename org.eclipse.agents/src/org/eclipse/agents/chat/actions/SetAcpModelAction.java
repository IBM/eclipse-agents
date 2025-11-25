/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.agents.chat.actions;

import org.eclipse.agents.chat.controller.agent.IAgentController;
import org.eclipse.jface.action.Action;

public class SetAcpModelAction extends Action {

	private IAgentController service;
	
	public SetAcpModelAction(IAgentController service) {
		super(service.getName());
		this.service = service;
	}

	@Override
	public void run() {
//		AgentClientController.instance().setAcpService(service);
	}
	
	public IAgentController getAgentService() {
		return service;
	}
}
