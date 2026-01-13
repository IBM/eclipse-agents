package org.eclipse.agents.chat;

import java.text.MessageFormat;

import org.eclipse.agents.chat.controller.workspace.WorkspaceChange;
import org.eclipse.agents.services.protocol.AcpSchema.PlanEntry;
import org.eclipse.agents.services.protocol.AcpSchema.PlanEntryStatus;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class ChatTasksDrawer {

	ExpandableComposite section;
	Composite composite;
	Table table;
	String header = "";
	boolean show = false;

	public ChatTasksDrawer(Composite parent) {
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		section = toolkit.createExpandableComposite(parent,
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE /*| Section.EXPANDED*/ );
		
		section.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		((GridData)section.getLayoutData()).verticalIndent = 0;
		section.setLayout(new GridLayout(1, true));
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				updatePresentation();
//				table.setSize(SWT.DEFAULT, 100);
			}
		});
		section.setText("File Changes");
//		section.setDescription("List of modified files. Does not include added, removed or moved files");
		
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		TableColumnLayout tableLayout =new TableColumnLayout();
		sectionClient.setLayout(tableLayout);
		sectionClient.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridData)sectionClient.getLayoutData()).heightHint = 120;

		table = new Table(sectionClient, SWT.BORDER | SWT.SINGLE | SWT.RESIZE | SWT.V_SCROLL);
		table.setLinesVisible(false);
		table.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		((GridData)table.getLayoutData()).heightHint = 120;

		for (int i = 0; i < 2; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			if (i == 0) {
				tableLayout.setColumnData(column, new ColumnWeightData(0, 40, false));
			} else if (i == 1) {
				tableLayout.setColumnData(column, new ColumnWeightData(150, 200, true));
			}
		}
		
		updatePresentation();

	}
	
	public void tasksChanged(PlanEntry[] planEntries) {
		table.removeAll();
		header = planEntries.length == 1 ? "1 task" : MessageFormat.format("{0} tasks", planEntries.length);
		show = false;
		
		for (int i = 0; i < planEntries.length; i++) {
			
			PlanEntry entry = planEntries[i];
			TableItem item = new TableItem(table, SWT.NONE);
			
			if (PlanEntryStatus.pending.equals(entry.status())) {
				item.setText(0, "○");
				show = true;
			} else if (PlanEntryStatus.in_progress.equals(entry.status())) {
				item.setText(0, "☉");
				show = true;
				header = MessageFormat.format("{0} ({1} of {2})", entry.content(), i + 1, planEntries.length);
			} else if (PlanEntryStatus.completed.equals(entry.status())) {
				item.setText(0, "◉");
			}
				
			item.setText(1, entry.content());

		}

		updatePresentation();

	}
	
	public void updatePresentation() {
		section.setText(header);
		boolean isVisible = show;
		section.setVisible(isVisible);
		((GridData)section.getLayoutData()).exclude = !isVisible;
		
		section.layout(true);
		section.getParent().layout(true);
		section.getParent().getParent().layout(true);
	}
	
	public void dispose() {

	}

}
