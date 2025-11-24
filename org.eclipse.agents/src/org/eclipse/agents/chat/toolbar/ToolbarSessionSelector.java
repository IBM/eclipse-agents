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
package org.eclipse.agents.chat.toolbar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.agents.chat.AcpView;
import org.eclipse.agents.services.AcpService;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;

public class ToolbarSessionSelector extends AbstractDynamicToolbarDropdown {

	List<SessionAction> actions;
	
	public ToolbarSessionSelector(AcpView view) {
		super("Session", "Select a session", view);
		
		actions = new ArrayList<SessionAction>();
		for (IAgentService agent: AcpService.instance().getAgents()) {
			actions.add(new SessionAction(agent));
		}
		setEnabled(false);
	}

	@Override
	protected void fillMenu(MenuManager menuManager) {
		for (SessionAction action: actions) {
			menuManager.add(action);
			action.setChecked(action.getAgent() ==  AcpService.instance().getAgentService());
		}
	}

	class SessionAction extends Action {
		IAgentService agent;
		
		public SessionAction(IAgentService agent) {
			super(agent.getName());
			this.agent = agent;
		}

		@Override
		public void run() {
			AcpService.instance().setAcpService(getView(), agent);
			ToolbarSessionSelector.this.updateText(agent.getName());
		}
		
		public IAgentService getAgent() {
			return agent;
		}
	}
}
