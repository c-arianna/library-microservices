package mentoring.acomi.notificationservice.infrastructure.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    AuthorizationManager<Message<?>> authorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {

        return messages.simpSubscribeDestMatchers("/topic/books").authenticated()
        		        .simpSubscribeDestMatchers("/topic/bookRequests").authenticated()
                	    .simpSubscribeDestMatchers("/topic/loans").hasAnyAuthority("ROLE_ADMIN", "ROLE_LIBRARIAN")
                	    .simpSubscribeDestMatchers("/topic/users").hasAnyAuthority("ROLE_ADMIN", "ROLE_LIBRARIAN")
                        .simpSubscribeDestMatchers("/user/**").authenticated()
                        .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT, SimpMessageType.UNSUBSCRIBE, 
                        		            SimpMessageType.HEARTBEAT).permitAll()
                        .anyMessage().authenticated()
                        .build();

    }
    
    @Bean("csrfChannelInterceptor")
    ChannelInterceptor csrfChannelInterceptor() {
        return new ChannelInterceptor() {};
    }
}