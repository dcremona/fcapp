package fcweb;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

import fcweb.backend.service.AttoreRepository;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "fcapp-v24")
@EnableScheduling
public class Application implements AppShellConfigurator {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    SqlDataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource,
            SqlInitializationProperties properties, AttoreRepository repository) {
        // This bean ensures the database is only initialized when empty
        return new SqlDataSourceScriptDatabaseInitializer(dataSource, properties) {
            @Override
            public boolean initializeDatabase() {
                if (repository.count() == 0L) {
                    return super.initializeDatabase();
                }
                return false;
            }
        };
    }
}
