package mentoring.acomi.notificationservice.infrastructure.websocket;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LogManager.getLogger(JwtChannelInterceptor.class);

    private final JwtDecoder jwtDecoder;

    public JwtChannelInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

        	try {

	            String authHeader = accessor.getFirstNativeHeader("Authorization");
	
	            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	            	
	            	logger.warn("WebSocket CONNECT rejected: missing Authorization header");
	            	throw new AccessDeniedException("Missing token");
	            }
	
	            String token = authHeader.substring(7);
	
	            Jwt jwt = jwtDecoder.decode(token);
	            
	            logger.info("WebSocket authenticated user {}", jwt.getSubject());
	            
	            Authentication authentication = new UsernamePasswordAuthenticationToken(jwt.getSubject(), null, List.of());
	
	            accessor.setUser(authentication);
            
	        }catch (JwtException ex) {
	            logger.error("Invalid websocket token", ex);
	            throw new AccessDeniedException("Invalid JWT");
	        }catch (Exception ex) {
	        	logger.error("WebSocket authentication failed", ex);
	        	throw new AccessDeniedException("Invalid token");
	        }
        }else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            logger.info("User {} subscribed to {}", accessor.getUser() != null ? accessor.getUser().getName() : "anonymous",
                accessor.getDestination());
        }
        
        return message;
    }
}