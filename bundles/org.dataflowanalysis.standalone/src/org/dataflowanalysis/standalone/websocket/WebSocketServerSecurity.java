package org.dataflowanalysis.standalone.websocket;

import java.net.URL;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.resource.Resource;

public class WebSocketServerSecurity {
	private static final int httpsPort = 8443;
	
	protected static ServerConnector getHttpsConnector(Server server) {
		 ServerConnector httpsConnector = new ServerConnector(server,
		            new SslConnectionFactory(getSSLContextFactory(), "http/1.1"),
		            new HttpConnectionFactory(getHttpsConfiguration()));
		        httpsConnector.setPort(httpsPort);
		        
       return httpsConnector;
	}
	
	private static SslContextFactory.Server getSSLContextFactory() {
		  SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
		  sslContextFactory.setKeyStoreType("PKCS12");
		  sslContextFactory.setKeyStorePath("C:\\Users\\Huell\\mock.p12");
		  sslContextFactory.setKeyStorePassword("1234");
	        sslContextFactory.setWantClientAuth(true); // Turn on javax.net.ssl.SSLEngine.wantClientAuth
	        sslContextFactory.setNeedClientAuth(true);    
	        return sslContextFactory;
	}
	
	private static HttpConfiguration getHttpsConfiguration() {
		HttpConfiguration httpsConf = new HttpConfiguration();
        httpsConf.setSecureScheme("https");
        httpsConf.setSecurePort(httpsPort);
        httpsConf.addCustomizer(new SecureRequestCustomizer());
        return httpsConf;
	}
	
	private static Resource findKeyStore() 
    {
		try {
        ClassLoader cl = WebSocketServerSecurity.class.getClassLoader();
        String keystoreResource = "ssl/keystore";
        URL f = cl.getResource(keystoreResource);
        if (f == null)
        {
            throw new RuntimeException("Unable to find " + keystoreResource);
        }

        return Resource.newResource(f.toURI());
		} catch (Exception e) {
			e.printStackTrace();
			return null;	
		}
    }
}
