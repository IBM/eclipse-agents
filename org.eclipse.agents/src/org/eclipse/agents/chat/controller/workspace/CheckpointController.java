package org.eclipse.agents.chat.controller.workspace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.agents.services.agent.AbstractService;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.AbortedByHookException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.ServiceUnavailableException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;

public class CheckpointController {

	public static final String CHECKPOINTS = "checkpoints";
	
	String sessionId;
	File checkpointsHome;
	File sessionHome;
	
	Map<IProject, File> projects = new HashMap<IProject, File>();
	Map<IProject, List<String[]>> commits = new HashMap<IProject, List<String[]>>();
	
	/**
	 * An already created directory for agent folders and the checkpoint folder
	 * @param agentsDir
	 */
	public CheckpointController(String sessionId) {
		this.sessionId =sessionId;
		
		File agentsHome = AbstractService.getAgentsHomeDirectory();
		
		checkpointsHome = new File(agentsHome.getAbsolutePath() + File.separator + CHECKPOINTS);

	    if (!checkpointsHome.exists()) {
	    	if (!checkpointsHome.mkdirs()) {
	    		throw new RuntimeException("Could not create " + CHECKPOINTS + " in agents directory");
	    	}
	    }
	    checkpointsHome.deleteOnExit();
	    
	    sessionHome = new File(checkpointsHome.getAbsolutePath() + File.separator + sessionId);
	    if (sessionHome.exists()) {
	    	// shouldnt happen
	    } else if (!sessionHome.mkdirs()) {
	    	throw new RuntimeException("Could not create " + CHECKPOINTS + " in agents directory");
	    }
	}
	
	public void init(IProject project, IProgressMonitor pm) {
		//TODO validate hash
		if (!projects.containsKey(project)) {
			File projectHome = new File(sessionHome.getAbsolutePath() + File.separator + project.getName());

		    if (!projectHome.exists() && !projectHome.mkdirs()) {
		    	throw new RuntimeException("Could not create " + CHECKPOINTS + " in agents directory");
		    }
		    File gitHome = new File(projectHome.getAbsolutePath() + File.separator + ".git");
		    
		    projects.put(project, gitHome);
		    
		    try {

				// -- Configure .gitconfig
				File gitconfig = new File(projectHome.getAbsolutePath() + File.separator + ".gitconfig");
				if (!gitconfig.exists()) {
					if (gitconfig.createNewFile()) {
						FileWriter writer = new FileWriter(gitconfig, Charset.defaultCharset());
						writer.write("[user]\n  name = Eclipse Coding Agents\n  email = agent@eclipse.org\n[commit]\n  gpgsign = false\n");
						writer.close();
					}
				}
				
				// -- Configure .gitignore
				File gitignore = new File(projectHome.getAbsolutePath() + File.separator + ".gitignore");
				if (!gitignore.exists()) {
					if (gitignore.createNewFile()) {
						FileWriter writer = new FileWriter(gitignore, Charset.defaultCharset());
						//TODO
						writer.write("TODO");
						writer.close();
					}
				}
				
				Git.init().setGitDir(gitHome).setInitialBranch("main").call()
				   .commit().setAllowEmpty(true).setMessage("Initial commit").call();
				
				RepositoryBuilder builder = new RepositoryBuilder();
				builder.setWorkTree(new File(project.getFullPath().toOSString()));
				builder.setInitialBranch("main");
				//TODO projectHome or projectHome/.git ?
				
				
				builder.setGitDir(gitHome);
				Repository repo = builder.build();
				Git git = new Git(repo);
				
				git.add().addFilepattern(".").call();
				
				RevCommit commit = git.commit().setMessage("Initial Snapshot").setNoVerify(true).call();
				String hash = commit.getName();
				System.err.println(commit.getName());
				git.close();
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AbortedByHookException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConcurrentRefUpdateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoHeadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoMessageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnmergedPathsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrongRepositoryStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidRefNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	public boolean destroy() {
		//TODO Doesn't work
		return sessionHome.delete();
	}
	
}
