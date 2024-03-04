package fcweb.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfig{
	public static final String MAIL_SENDER_PRIMARY_KEY = "spring.mail.primary";
	public static final String MAIL_SENDER_PRIMARY_PROPERTIES_KEY = "spring.mail.primary.properties";
	public static final String MAIL_SENDER_SECONDARY_KEY = "spring.mail.secondary";
	public static final String MAIL_SENDER_SECONDARY_PROPERTIES_KEY = "spring.mail.secondary.properties";

	public static final String KEY_SEPARATOR = ".";
	public static final String EMPTY_STRING = "";

	@Autowired
	Environment env;

	@Bean
	@ConfigurationProperties(prefix = MAIL_SENDER_PRIMARY_KEY)
	public JavaMailSender primarySender() {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
		return javaMailSenderWithProperties(javaMailSender, MAIL_SENDER_PRIMARY_PROPERTIES_KEY);
	}

	@Bean
	@ConfigurationProperties(prefix = MAIL_SENDER_SECONDARY_KEY)
	public JavaMailSender secondarySender() {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

		return javaMailSenderWithProperties(javaMailSender, MAIL_SENDER_SECONDARY_PROPERTIES_KEY);
	}

	@SuppressWarnings("rawtypes")
	private JavaMailSender javaMailSenderWithProperties(
			JavaMailSenderImpl javaMailSender, String prefix) {
		Properties props = new Properties();
		if (env instanceof ConfigurableEnvironment) {
			for (PropertySource<?> propertySource : ((ConfigurableEnvironment) env).getPropertySources()) {
				if (propertySource instanceof EnumerablePropertySource) {
					for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
						if (key.startsWith(prefix)) {
							props.setProperty(key.replaceAll(prefix + KEY_SEPARATOR, EMPTY_STRING), propertySource.getProperty(key).toString());
						}
					}
				}
			}
		}

		javaMailSender.setJavaMailProperties(props);
		return javaMailSender;
	}
}