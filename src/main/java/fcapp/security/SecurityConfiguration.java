package fcapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;

import fcapp.ui.views.login.LoginView;

@EnableWebSecurity
@Configuration
//public class SecurityConfiguration extends VaadinWebSecurity{
public class SecurityConfiguration {
//	@Bean
//	public PasswordEncoder passwordEncoder() {
//		return new BCryptPasswordEncoder();
//	}
//
//	@SuppressWarnings("removal")
//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//
//		http.authorizeHttpRequests().requestMatchers(new AntPathRequestMatcher("/images/*.png")).permitAll();
//
//		// Icons from the line-awesome addon
//		http.authorizeHttpRequests().requestMatchers(new AntPathRequestMatcher("/line-awesome/**/*.svg")).permitAll();
//		super.configure(http);
//		setLoginView(http, LoginView.class);
//	}
	
	@Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
        });
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

//@EnableWebSecurity
//@Configuration
//public class SecurityConfiguration extends VaadinWebSecurity {   // or use VaadinSecurityConfigurer
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        // Permit Vaadin internals + error page
//        http.authorizeHttpRequests(auth -> auth
//            .requestMatchers(
//                "/error",
//                "/VAADIN/**",
//                "/frontend/**",
//                "/frontend-es5/**",
//                "/frontend-es6/**",
//                "/images/**",
//                "/icons/**"
//            ).permitAll()
//        );
//
//        // Important: ignore CSRF for Vaadin (it has its own protection)
//        http.csrf(csrf -> csrf.ignoringRequestMatchers(
//            new AntPathRequestMatcher("/VAADIN/**"),
//            new AntPathRequestMatcher("/**")   // or more specific if you have REST APIs
//        ));
//
//        super.configure(http);   // must come after the custom rules in most cases
//        setLoginView(http, LoginView.class);
//    }
//    
//  @Bean
//  public PasswordEncoder passwordEncoder() {
//      return new BCryptPasswordEncoder();
//  }
//
//}