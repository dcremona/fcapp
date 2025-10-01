package fcweb.config;

//import org.apache.catalina.connector.Connector;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
//import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
//import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectorConfig{

	// # Original port configuration for HTTP, now being used for HTTPS
	// server.port=${PORT:8443}
	// # Our own custom field (this isn't defined by Spring)
	// server.http.port=${HTTP_PORT:14939}
	// # Tell Spring Security (if used) to require requests over HTTPS
	// security.require-ssl=true
	// # The format used for the keystore
	// server.ssl.key-store-type=PKCS12
	// # The path to the keystore containing the certificate
	// server.ssl.key-store=classpath:keystore.p12
	// # The password used to generate the certificate
	// server.ssl.key-store-password=fclt2024
	// # The alias mapped to the certificate
	// server.ssl.key-alias=tomcat

	// @Value("${server.http.port}")
	// private int SERVER_HTTP_PORT;
	//
	// @Bean
	// public ServletWebServerFactory servletContainer() {
	// Connector connector = new
	// Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
	// connector.setPort(SERVER_HTTP_PORT);
	//
	// TomcatServletWebServerFactory tomcat = new
	// TomcatServletWebServerFactory();
	// tomcat.addAdditionalTomcatConnectors(connector);
	//
	// return tomcat;
	// }

	// #server.port=${PORT:8080}
	// #server.port=8443
	// #server.ssl.key-store-type=PKCS12
	// #server.ssl.key-store=classpath:springboot.p12
	// #server.ssl.key-store-password=fclt2024
	// #server.ssl.key-alias=springboot
	// #server.ssl.key-password=fclt2024

	// @Bean
	// public TomcatServletWebServerFactory servletContainer() {
	// TomcatServletWebServerFactory tomcat = new
	// TomcatServletWebServerFactory() {
	// @Override
	// protected void postProcessContext(Context context) {
	// SecurityConstraint securityConstraint = new SecurityConstraint();
	// securityConstraint.setUserConstraint("CONFIDENTIAL");
	// SecurityCollection collection = new SecurityCollection();
	// collection.addPattern("/*");
	// securityConstraint.addCollection(collection);
	// context.addConstraint(securityConstraint);
	// }
	// };
	// tomcat.addAdditionalTomcatConnectors(getHttpConnector());
	// return tomcat;
	// }
	//
	// private Connector getHttpConnector() {
	// Connector connector = new
	// Connector("org.apache.coyote.http11.Http11NioProtocol");
	// connector.setScheme("http");
	// connector.setPort(8080);
	// connector.setSecure(false);
	// connector.setRedirectPort(8443);
	// return connector;
	// }
}