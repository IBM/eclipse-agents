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
package org.eclipse.agents.chat.controller;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.controller.agent.GeminiAgent;
import org.eclipse.agents.chat.controller.agent.IAgentController;
import org.eclipse.agents.chat.protocol.AcpSchema.AgentNotification;
import org.eclipse.agents.chat.protocol.AcpSchema.AgentRequest;
import org.eclipse.agents.chat.protocol.AcpSchema.AgentResponse;
import org.eclipse.agents.chat.protocol.AcpSchema.CancelNotification;
import org.eclipse.agents.chat.protocol.AcpSchema.ClientNotification;
import org.eclipse.agents.chat.protocol.AcpSchema.ClientRequest;
import org.eclipse.agents.chat.protocol.AcpSchema.ClientResponse;
import org.eclipse.agents.chat.protocol.AcpSchema.ContentBlock;
import org.eclipse.agents.chat.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.agents.chat.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.agents.chat.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.agents.chat.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.agents.chat.protocol.AcpSchema.PromptRequest;
import org.eclipse.agents.chat.protocol.AcpSchema.PromptResponse;
import org.eclipse.agents.chat.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.agents.chat.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.agents.chat.protocol.AcpSchema.ReleaseTerminalRequest;
import org.eclipse.agents.chat.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.agents.chat.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.agents.chat.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.agents.chat.protocol.AcpSchema.SessionNotification;
import org.eclipse.agents.chat.protocol.AcpSchema.SetSessionModeRequest;
import org.eclipse.agents.chat.protocol.AcpSchema.SetSessionModeResponse;
import org.eclipse.agents.chat.protocol.AcpSchema.StopReason;
import org.eclipse.agents.chat.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.agents.chat.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.agents.chat.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.agents.chat.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.agents.chat.protocol.AcpSchema.WriteTextFileResponse;
import org.eclipse.agents.chat.view.ChatView;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

public class AgentClientController {

	private static AgentClientController instance;
	
	private IAgentController activeAgent = null;
	private String activeSessionId = null;
	
	private static Map<String, SessionListener> sessions = new HashMap<String, SessionListener>();
	
	private ListenerList<ISessionListener> listenerList;

	private StartServiceJob initializationJob;

	static {
		instance = new AgentClientController();
	}
	
	IAgentController[] agentServices;
	private AgentClientController() {
		agentServices = new IAgentController[] { 
			new GeminiAgent()
//			new GooseAgent()
		};
		listenerList = new  ListenerList<ISessionListener>();
	}
	
	public static AgentClientController instance() {
		return instance;
	}
	
	public IAgentController[] getAgents() {
		return agentServices;
	}

	public void setAcpService(ChatView view, IAgentController agent) {
		Tracer.trace().trace(Tracer.CHAT, "setAcpService: " + agent.getName()); //$NON-NLS-1$
		view.agentDisconnected();
		activeSessionId = null;
		this.activeAgent = agent;
		if (!agent.isRunning()) {
			agent.stop();
			
           if (initializationJob != null) {
               initializationJob.cancel();
           }
                       
			initializationJob = new StartServiceJob(activeAgent, null);
			initializationJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					if (event.getJob().getResult().isOK()) {
						StartServiceJob job = (StartServiceJob) event.getJob();

						String sessionId = job.getSessionId();
						if (sessionId != null && !sessions.containsKey(sessionId)) {
							
							activeSessionId = sessionId;

							SessionListener model = new SessionListener(
								agent,
								sessionId,
								job.getCwd(),
								job.getMcpServers(),
								job.getModes());
							
							sessions.put(sessionId, model);
							
							model.setView(view);
							view.agentConnected();
							
							clientRequests(agent.getInitializeRequest());
							agentResponds(agent.getInitializeResponse());
							
							
						} else {
							//TODO
							Tracer.trace().trace(Tracer.CHAT, "setAcpService: found a pre-existing matching session id");
						}
					} else {
						Tracer.trace().trace(Tracer.CHAT, "initialization job has an error");
						Tracer.trace().trace(Tracer.CHAT, event.getJob().getResult().getMessage(), event.getJob().getResult().getException());
						if (event.getJob().getResult().getException() != null) {
							event.getJob().getResult().getException().printStackTrace();
						}
					}
				}
			});
			initializationJob.schedule();
		}
	}
	
	public IAgentController getAgentService() {
		return activeAgent;
	}
	
	public String getActiveSessionId() {
		return activeSessionId;
	}
	
	public SessionListener getActiveSession() {
		return sessions.get(activeSessionId);
	}
	
	public void addAcpListener(ISessionListener listener) {
		listenerList.add(listener);
	}
	
	public void removeAcpListener(ISessionListener listener) {
		listenerList.remove(listener);
	}
	
	public void clientRequests(ClientRequest req) {
		for (ISessionListener listener: listenerList) {
//			if (req instanceof InitializeRequest) {
//				listener.accept((InitializeRequest)req);	
//			} else if (req instanceof AuthenticateRequest) {
//				listener.accept((AuthenticateRequest)req);
//			} else if (req instanceof NewSessionRequest) {
//				listener.accept((NewSessionRequest)req);
//			} else if (req instanceof LoadSessionRequest) {
//				listener.accept((LoadSessionRequest)req);
			if (req instanceof SetSessionModeRequest) {
				listener.accept((SetSessionModeRequest)req);
			} else if (req instanceof PromptRequest) {
				listener.accept((PromptRequest)req);
			}
		}
	}
	
	public void clientResponds(ClientResponse resp) {
		for (ISessionListener listener: listenerList) {
			if (resp instanceof WriteTextFileResponse) {
				listener.accept((WriteTextFileResponse)resp);
			} else if (resp instanceof ReadTextFileResponse) {
				listener.accept((ReadTextFileResponse)resp);
			} else if (resp instanceof RequestPermissionResponse) {
				listener.accept((RequestPermissionResponse)resp);
			} else if (resp instanceof CreateTerminalResponse) {
				listener.accept((CreateTerminalResponse)resp);
			} else if (resp instanceof TerminalOutputResponse) {
				listener.accept((TerminalOutputResponse)resp);
			} else if (resp instanceof ReleaseTerminalResponse) {
				listener.accept((ReleaseTerminalResponse)resp);
			} else if (resp instanceof WaitForTerminalExitResponse) {
				listener.accept((WaitForTerminalExitResponse)resp);
			} else if (resp instanceof KillTerminalCommandResponse) {
				listener.accept((KillTerminalCommandResponse)resp);
			}							
		}
	}
	
	public void clientNotifies(ClientNotification notification) {
		for (ISessionListener listener: listenerList) {
			if (notification instanceof CancelNotification) {
				listener.accept((CancelNotification)notification);
			}
		}
	}
	
	public void agentRequests(AgentRequest req) {
		for (ISessionListener listener : listenerList) {
			if (req instanceof ReadTextFileRequest) {
				listener.accept((ReadTextFileRequest)req);
			} else if (req instanceof RequestPermissionRequest) {
				listener.accept((RequestPermissionRequest)req);
			} else if (req instanceof CreateTerminalRequest) {
				listener.accept((CreateTerminalRequest)req);
			} else if (req instanceof TerminalOutputRequest) {
				listener.accept((TerminalOutputRequest)req);
			} else if (req instanceof ReleaseTerminalRequest) {
				listener.accept((ReleaseTerminalRequest)req);
			} else if (req instanceof WaitForTerminalExitRequest) {
				listener.accept((WaitForTerminalExitRequest)req);
			} else if (req instanceof KillTerminalCommandRequest) {
				listener.accept((KillTerminalCommandRequest)req);
			}
		}
	}
	
	public void agentResponds(AgentResponse resp) {
		for (ISessionListener listener: listenerList) {
//			if (resp instanceof AuthenticateResponse) {
//				listener.accept((AuthenticateResponse)resp);
//			} else if (resp instanceof NewSessionResponse) {
//				listener.accept((NewSessionResponse)resp);
//			} else if (resp instanceof LoadSessionResponse) {
//				listener.accept((LoadSessionResponse)resp);
			if (resp instanceof SetSessionModeResponse) {
				listener.accept((SetSessionModeResponse)resp);
			} else if (resp instanceof PromptResponse) {
				listener.accept((PromptResponse)resp);
			}
		}
	}
	
	public void agentNotifies(AgentNotification notification) {
		for (ISessionListener listener: listenerList) {
			if (notification instanceof SessionNotification) {
				listener.accept((SessionNotification)notification);
			}
		}
	}
}
