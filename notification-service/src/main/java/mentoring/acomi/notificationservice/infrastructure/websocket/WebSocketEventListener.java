package mentoring.acomi.notificationservice.infrastructure.websocket;

import java.security.Principal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LogManager.getLogger(WebSocketEventListener.class);

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {

        Principal user = SimpMessageHeaderAccessor.getUser(event.getMessage().getHeaders());

        logger.info("WebSocket connected: {}", user != null ? user.getName() : "anonymous");
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        Principal user = event.getUser();

        logger.info("WebSocket disconnected: {} session={}", user != null ? user.getName() : "unknown", event.getSessionId());
    }
}