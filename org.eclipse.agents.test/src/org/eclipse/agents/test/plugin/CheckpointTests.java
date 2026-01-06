/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
 
package org.eclipse.agents.test.plugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.agents.chat.controller.workspace.CheckpointController;
import org.eclipse.agents.chat.controller.workspace.WorkspaceController;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public final class CheckpointTests {

	String[] lines = new String[] {
			"public class HelloWorld {",
			"    public static void main(String[] args) {",
			"        System.out.println(\"Hello, World!\");",
			"    }",
			"}"
	};
	
	String[] changedLines = new String[] {
			"// Class Hello World",
			"public class HelloWorld {",
			"    // Main Method",
			"    public static void main(String[] args) {",
			"        System.out.println(\"Hello, World!!!\");",
			"    }",
			"}"
	};
	
	
	
	String content = String.join("\n", Arrays.asList(lines));
	String modifiedContent = String.join("\n", Arrays.asList(changedLines));
 
	IFile file;
	ITextEditor editor;
	CheckpointController controller;
	

	@BeforeAll
	public void setup() throws CoreException, IOException {
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		final IProject project = workspace.getRoot().getProject("Project");
		try {
			IWorkspaceRunnable create = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					project.create(null, null);
					project.open(null);
				}
			};
			workspace.run(create, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		file = project.getFile("HelloWorld.java");

		final File f = new File(file.getFullPath().toOSString());
		System.out.println(f.toURI());
//				 file.getFullPath()

		controller = new CheckpointController(UUID.randomUUID().toString());
		
		
		if (!file.exists()) {
			byte[] bytes = content.getBytes();

			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			file.create(stream, true, null);
			stream.close();
		}
		
		project.refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IWorkbenchPart part = page.getActivePart();
					part.dispose();

					editor = (ITextEditor)IDE.openEditor(page, file, true);
					if (editor instanceof ITextEditor) {
						ITextEditor textEditor = (ITextEditor) editor;
						textEditor.selectAndReveal(7, 5);
						page.activate(textEditor);
					}
					Map attr = new HashMap();
					attr.put(IMarker.MESSAGE, "There is a problem");
					attr.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

					attr.put(IMarker.CHAR_START, 7);
					attr.put(IMarker.CHAR_END, 12);
					attr.put(IMarker.LINE_NUMBER, 1);

					file.createMarker(IMarker.PROBLEM, attr);

					page.getActivePart();
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	//------------------------
	// READ FILE TESTS
	//------------------------
	
	@Test
	@Order(1)
	public void checkpointInit() {
		controller.init(file.getProject(), new NullProgressMonitor());
	}
	
	@Test
	@Order(2)
	public void writeToFile() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		Path path = (Path)file.getRawLocation();
		controller.writeToFile(path, modifiedContent);
	}
	
	@Test
	@Order(1)
	public void checkpoint2() {
		controller.recordCheckpoint(file.getProject(), "Second Checkpoint", new NullProgressMonitor());
	}
	
	@Test
	@Order(3)
	public void checkpointDestroy() {
		controller.destroy();
	}	
	
	////////////////
	

	public void testEquals(String left, String right) {
		System.out.println(left + " == " + right);
		Assert.assertEquals(left, right);
	}

	public void testArrayEquals(String message, String[] left, String[] right) {
		System.out.println(message + ":: " + Arrays.toString(left) + " == " + Arrays.toString(right));
		Assert.assertArrayEquals(message, left, right);
	}

}