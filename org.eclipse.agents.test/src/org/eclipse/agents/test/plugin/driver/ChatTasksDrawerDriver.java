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
 
package org.eclipse.agents.test.plugin.driver;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.agents.chat.ChatView;
import org.eclipse.agents.services.protocol.AcpSchema.PlanEntry;
import org.eclipse.agents.services.protocol.AcpSchema.PlanEntryPriority;
import org.eclipse.agents.services.protocol.AcpSchema.PlanEntryStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public final class ChatTasksDrawerDriver {

	
	@Test
	public void testWriteEditor() throws Exception {
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final ChatView view = (ChatView) page.showView(ChatView.ID, null, //$NON-NLS-1$
				IWorkbenchPage.VIEW_ACTIVATE);
		
		List<PlanEntry> entries = new ArrayList<PlanEntry>();
		entries.add(new PlanEntry(null, "Mapping your route and overnight stops", PlanEntryPriority.low, PlanEntryStatus.pending));
		entries.add(new PlanEntry(null, "Verifying international travel and vehicle requirements", PlanEntryPriority.low, PlanEntryStatus.pending));
		entries.add(new PlanEntry(null, "Finalizing itinerary details and pack essentials", PlanEntryPriority.low, PlanEntryStatus.pending));
		
		view.tasksChanged(entries.toArray(PlanEntry[]::new));
		sleep (4000);
	
		entries.set(0, new PlanEntry(null, "Mapping your route and overnight stops", PlanEntryPriority.low, PlanEntryStatus.in_progress));
		view.tasksChanged(entries.toArray(PlanEntry[]::new));
		sleep (4000);
		
		entries.set(0, new PlanEntry(null, "Mapping your route and overnight stops", PlanEntryPriority.low, PlanEntryStatus.completed));
		entries.set(1, new PlanEntry(null, "Verifying international travel and vehicle requirements", PlanEntryPriority.low, PlanEntryStatus.in_progress));
		view.tasksChanged(entries.toArray(PlanEntry[]::new));
		sleep (4000);
		
		entries.set(1, new PlanEntry(null, "Verifying international travel and vehicle requirements", PlanEntryPriority.low, PlanEntryStatus.completed));
		entries.set(2, new PlanEntry(null, "Finalizing itinerary details and pack essentials", PlanEntryPriority.low, PlanEntryStatus.in_progress));
		view.tasksChanged(entries.toArray(PlanEntry[]::new));
		sleep (4000);
		
		entries.set(2, new PlanEntry(null, "Task 3", PlanEntryPriority.low, PlanEntryStatus.completed));
		view.tasksChanged(entries.toArray(PlanEntry[]::new));
		sleep (4000);
		
	}
	
	public void sleep(long ms) {
		long start = System.currentTimeMillis();
		while (start + ms > System.currentTimeMillis()) {
			Display.getDefault().readAndDispatch();
		}
	}

}
