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

import org.eclipse.agents.chat.controller.AgentClientController;
import org.eclipse.agents.chat.controller.agent.IAgentController;
import org.eclipse.agents.chat.view.ChatView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;

public class ToolbarSessionSelector extends AbstractDynamicToolbarDropdown {

	List<SessionAction> actions;
	
	public ToolbarSessionSelector(ChatView view) {
		super("SessionListener", "Select a session", view);
		
		actions = new ArrayList<SessionAction>();
		for (IAgentController agent: AgentClientController.instance().getAgents()) {
			actions.add(new SessionAction(agent));
		}
		setEnabled(false);
	}

	@Override
	protected void fillMenu(MenuManager menuManager) {
		for (SessionAction action: actions) {
			menuManager.add(action);
			action.setChecked(action.getAgent() ==  AgentClientController.instance().getAgentService());
		}
	}

	class SessionAction extends Action {
		IAgentController agent;
		
		public SessionAction(IAgentController agent) {
			super(agent.getName());
			this.agent = agent;
		}

		@Override
		public void run() {
			AgentClientController.instance().setAcpService(getView(), agent);
			ToolbarSessionSelector.this.updateText(agent.getName());
		}
		
		public IAgentController getAgent() {
			return agent;
		}
	}
}
