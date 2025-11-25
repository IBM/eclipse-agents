package org.eclipse.agents.chat.controller;

import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.protocol.AcpSchema.CancelNotification;
import org.eclipse.agents.chat.protocol.AcpSchema.ContentBlock;
import org.eclipse.agents.chat.protocol.AcpSchema.PromptRequest;
import org.eclipse.agents.chat.protocol.AcpSchema.PromptResponse;
import org.eclipse.agents.chat.protocol.AcpSchema.StopReason;

public class SessionController {

	SessionListener session;
	AgentClientController agentClientController;
	
	public SessionController(AgentClientController agentClientController, SessionListener session) {
		this.agentClientController = agentClientController;
		this.session = session;
	}
	
	public void prompt(String sessionId, ContentBlock[] contentBlocks) {
		PromptRequest request = new PromptRequest(null, contentBlocks, sessionId);
		agentClientController.clientRequests(request);
		agentClientController.getAgentService().getAgent().prompt(request).whenComplete((result, ex) -> {
	        if (ex != null) {
	        	Tracer.trace().trace(Tracer.CHAT, "prompt error", ex); //$NON-NLS-1$
	            ex.printStackTrace();
	            
	            // Gemini CLI: cancel before first thought throws JSONRPC error
	            agentClientController.agentResponds(new PromptResponse(null, StopReason.refusal));
	        } else {
	        	agentClientController.agentResponds(result);
	        }
	    });
	}
	
	public void stopPromptTurn(String sessionId) {
		CancelNotification notification = new CancelNotification(null, sessionId);
		agentClientController.clientNotifies(notification);
		try {
			agentClientController.getAgentService().getAgent().cancel(notification);
		} catch (Exception ex) {
			Tracer.trace().trace(Tracer.CHAT, "stop prompt error", ex); //$NON-NLS-1$
			ex.printStackTrace();
		}
	}
}
