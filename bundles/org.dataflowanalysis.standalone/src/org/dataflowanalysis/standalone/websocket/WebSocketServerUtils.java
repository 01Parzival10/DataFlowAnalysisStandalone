package org.dataflowanalysis.standalone.websocket;

import java.time.Duration;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;

public class WebSocketServerUtils {
	public static Thread startWebSocketServer() {        
        Thread websocketServer =  new Thread(() -> startServer());
        websocketServer.start();
        
        return websocketServer;
	}
	
	public static void shutDownFrontend() {
		WebSocketServerHandler.shutDownFrontend();
	}
	
	private static void startServer() {		
		try {				 
			var server = new Server();
	        var connector = new ServerConnector(server);
	        server.addConnector(connector);
	        server.addConnector(WebSocketServerSecurity.getHttpsConnector(server));

	        // Setup the basic application "context" for this application at "/"
	        // This is also known as the handler tree (in jetty speak)
	        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        context.setContextPath("/");
	       
	        
	        HandlerList handlers = new HandlerList();
	        handlers.addHandler(context);
	        handlers.addHandler(new SecuredRedirectHandler());
	        

	        // Configure specific websocket behavior
	        JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) ->
	        {
	            // Configure default max size
	            wsContainer.setMaxTextMessageSize(Long.MAX_VALUE);
	            wsContainer.setIdleTimeout(Duration.ofMinutes(20));

	            // Add websockets
	            wsContainer.addMapping("/events/*", WebSocketServerHandler.class);
	        });
	        
	        server.setHandler(handlers);
	        
	        connector.setPort(3000);
	        server.start();
	        server.join();
	        
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
