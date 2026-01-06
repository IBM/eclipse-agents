package org.eclipse.agents.chat.controller.workspace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.eclipse.agents.services.agent.AbstractService;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;

public class CheckpointController {

	public static final String CHECKPOINTS = "checkpoints";
	
	String sessionId;
	File checkpointsHome;
	File sessionHome;
	
	private List<Checkpoint> checkpoints = new ArrayList<Checkpoint>();

//	Map<IProject, File> projects = new HashMap<IProject, File>();
//	Map<IProject, List<String[]>> commits = new HashMap<IProject, List<String[]>>();
	
	
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
		    
		    Git git = null;
		
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
				
				pm.subTask("Initializing");
				Git.init().setGitDir(gitHome).setInitialBranch("main").call()
				   .commit().setAllowEmpty(true).setMessage("Initial commit").call();
				
				recordCheckpoint(project, "Initial Checkpoint", pm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void recordCheckpoint(IProject project, String message, IProgressMonitor pm) {
		Git git = null;
		
		try {
			git = git(project);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		if (git != null) {
			
			try {
				pm.subTask("Adding new files");
				git.add().addFilepattern(".").call();
				RevCommit commit = git.commit().setMessage(message).setNoVerify(true).call();
				String hash = commit.getName();
				pm.subTask("Creating Checkpoint");
				logCheckpoint(project, hash, message);
				
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				git.close();
			}
		}
	}
	
	
	
	public boolean destroy() {
		//TODO Doesn't work
		return sessionHome.delete();
	}
	
	
	
	
	public List<Checkpoint> getCheckpoints() {
		return checkpoints;
	}
	
	/**
	 * For best performance, forward only projects anticipated to be affected by agent activity
	 * @param affectedProjects
	 */
	public void createCheckpoint(IProject[] projects, String message, String command, IProgressMonitor pm) {
		
		
	}
	

	

	public void restoreCheckpoint(Checkpoint checkpoint) {
		
	}

	public class Checkpoint {
		Date date;
		String name;
		String command;
		List<FolderCheckpoint> folderCheckpoints;
		
		public Checkpoint(String name, String command) {
			this.name = name;
			this.command = command;
			this.date = new Date();
			this.folderCheckpoints = new ArrayList<FolderCheckpoint>();
		}
		
		
		public void addNewCheckpoint(IProject[] projects, String message, String command, IProgressMonitor pm) {
			Checkpoint checkpoint = new Checkpoint(message, command);
			for (FolderCheckpoint prior: this.folderCheckpoints) {
				checkpoint.folderCheckpoints.add(prior.clone());
			}
			
			ProjectCheckpoint pc = null;
			for (IProject project: projects) {
				for (ProjectCheckpoint prior: projectCheckpoints) {
					if (prior.project.equals(project)) {
						pc = prior;
					}
				}
			}
			
			if (pc == null) {
				pc = new ProjectCheckpoint(project);
			}
			
			for (if (last != null) {
				for (ProjectCheckpoint pc: last.projectCheckpoints) {
					if (pc.project.equals(project)) {
						return pc;
					}
				}
			}
		}
	}

	public class FolderCheckpoint implements Cloneable {
		
		//Folder the agent is modifying
		File sourceFolder;
		
		// the .git folder for the session's shadow repository of the source folder
		File gitFolder;
		
		String commitHash;
		UUID id;
		
		public FolderCheckpoint(File sourceFolder) {
			this.sourceFolder = sourceFolder;
			this.id = UUID.randomUUID();
		}

		public FolderCheckpoint(File sourceFolder, File gitFolder, String commitHash, UUID id) {
			super();
			this.sourceFolder = sourceFolder;
			this.gitFolder = gitFolder;
			this.commitHash = commitHash;
			this.id = id;
		}
		
		public boolean contains(File file) {
			//TODO validate
			return file.getAbsolutePath().startsWith(this.sourceFolder.getAbsolutePath());
		}

		public void init(IProgressMonitor pm) {
			pm.subTask("Initializing");

			File projectHome = new File(sessionHome.getAbsolutePath() + File.separator + id.toString());

		    if (!projectHome.exists() && !projectHome.mkdirs()) {
		    	throw new RuntimeException("Could not create " + CHECKPOINTS + " in agents directory");
		    }
		    this.gitFolder = new File(projectHome.getAbsolutePath() + File.separator + ".git");
		    
		    Git git = null;
		
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
				
				
				Git.init().setGitDir(this.gitFolder).setInitialBranch("main").call()
				   .commit().setAllowEmpty(true).setMessage("Initial commit").call();
				
				this.commit("Initial Checkpoint", pm);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public boolean isDirty() {
			return false;
		}
		
		public void commit(String message, IProgressMonitor pm) {
			Git git = null;
			
			try {
				git = git();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			if (git != null) {
				
				try {
					pm.subTask("Adding files");
					git.add().addFilepattern(".").call();
					
					pm.subTask("Committing");
					RevCommit commit = git.commit().setMessage(message).setNoVerify(true).call();
					this.commitHash = commit.getName();
					
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					git.close();
				}
			}
		}
		
		public void restore(String commit, IProgressMonitor pm) {
			pm.subTask("Restoring");
			Git git = null;
			
			try {
				git = git();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			if (git != null) {
				
				try {
					//TODO git.restore
					//TODO await repo.raw(['restore', '--source', commitHash, '.']);
					git.checkout().setName(commit);
				    // Removes any untracked files that were introduced post snapshot.
					git.clean().setCleanDirectories(true).setForce(true).call();
					
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					git.close();
				}
			}
			
		}
		
		public void destroy() {
			//TODO
		}

		@Override
		protected FolderCheckpoint clone() throws CloneNotSupportedException {
			return new FolderCheckpoint(sourceFolder, gitFolder, commitHash, id);
		}
		
		private Git git() throws InvalidRefNameException, IOException {
			RepositoryBuilder builder = new RepositoryBuilder();
			//TODO linked projects / git complexities
			builder.setWorkTree(this.sourceFolder);
			builder.setInitialBranch("main");
			builder.setGitDir(this.gitFolder);
			Repository repo = builder.build();
			return new Git(repo);
		}

	}
	
}
