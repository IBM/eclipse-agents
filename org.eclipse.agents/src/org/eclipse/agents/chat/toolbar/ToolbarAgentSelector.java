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

public class ToolbarAgentSelector extends AbstractDynamicToolbarDropdown {

	List<ModelAction> actions;
	
	public ToolbarAgentSelector(ChatView view) {
		super("Coding Agent...", "Select a coding agent", view);
		
		actions = new ArrayList<ModelAction>();
		for (IAgentController agent: AgentClientController.instance().getAgents()) {
			actions.add(new ModelAction(agent));
		}
	}

	@Override
	protected void fillMenu(MenuManager menuManager) {
		for (ModelAction action: actions) {
			menuManager.add(action);
			action.setChecked(action.getAgent() ==  AgentClientController.instance().getAgentService());
		}
	}

	class ModelAction extends Action {
		IAgentController agent;
		
		public ModelAction(IAgentController agent) {
			super(agent.getName());
			this.agent = agent;
		}

		@Override
		public void run() {
			AgentClientController.instance().setAcpService(getView(), agent);
			ToolbarAgentSelector.this.updateText(agent.getName());
		}
		
		public IAgentController getAgent() {
			return agent;
		}
	}
}
