package pax.tecs.ws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Value("${S3_LOG_PASSWORD}")
	private String password;
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.authorizeRequests().requestMatchers("/s3logs").authenticated().and().formLogin().loginPage("/login")
				.defaultSuccessUrl("/psconfig").permitAll().and().logout().permitAll().and().csrf().disable();
		http.headers().disable();
		return http.build();

	}
	
	@Bean
	public UserDetailsService userDetailService() {
		UserDetails users = User.builder().username("user").password(passwordEnconder().encode(password))
				.roles("USER").build();
		return new InMemoryUserDetailsManager(users);

	}

	@Bean
	public PasswordEncoder passwordEnconder() {
		return new BCryptPasswordEncoder();
	}

}
