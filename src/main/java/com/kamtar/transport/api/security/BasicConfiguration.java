package com.kamtar.transport.api.security;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.kamtar.transport.api.service.OperationService;
import com.kamtar.transport.api.service.VehiculeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kamtar.transport.api.swagger.BasicAuthenticationPoint;

@Configuration
@EnableScheduling
@EnableWebSecurity
public class BasicConfiguration extends WebSecurityConfigurerAdapter {


	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(BasicConfiguration.class);

	// pour authent swagger
	@Autowired 
	private BasicAuthenticationPoint basicAuthenticationPoint;

	@Autowired
	private OperationService operationService;

	@Value("${swagger.login}")
	private String swagger_login;

	@Value("${swagger.password}")
	private String swagger_password;

	@Autowired
	private VehiculeService vehiculeService;

	@Value("${kamtar.env}")
	private String kamtar_env;


	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
		.cors()
		.and()
		.csrf().disable();

		// pour autent swagger
		http
		.authorizeRequests()               
		.antMatchers("/swagger*/**", "/v2/api-docs").authenticated()
		.and()
		.httpBasic().authenticationEntryPoint(basicAuthenticationPoint);
	}

	/**
	 * pour authent swagger
	 * @param auth
	 * @throws Exception
	 */
	@Autowired 
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception { 
		auth.inMemoryAuthentication()
		.withUser(swagger_login).password("{noop}" + swagger_password).roles("USER");
	}  

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token", "Token", "origin", "accept", "x-requested-with", "client-security-token", "Cache-Control", "X-File-Name", "Pays"));
		configuration.setExposedHeaders(Arrays.asList("x-auth-token", "Token", "origin", "accept", "x-requested-with", "client-security-token", "Pays"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}


	@Scheduled(cron = "0 0 0 * * ?") // tous les jours à 0h00
	public void scheduleMinuit() {
		logger.info("CRON tous les jours "+ System.currentTimeMillis() / 1000);

		vehiculeService.setVehiculesIndispo();

	}

	@Scheduled(cron = "0 0 3 * * ?") // tous les jours à 3h00
	public void scheduleFixedRateTask() {
		logger.info("CRON tous les jours "+ System.currentTimeMillis() / 1000);

		operationService.refreshPredictifEveryDay();

	}

	@Scheduled(cron = "0 0 2 * * ?") // tous les jours à 2h00
	public void scheduleFixedRateTask3() {
		logger.info("CRON tous les jours "+ System.currentTimeMillis() / 1000);

		// doit être fait avant le calcul du prédictif
		operationService.creerOperationsRecurrentes(new Date(), 1);

	}

	@Scheduled(cron = "0 3 * * * ?") // tous les heures à Xh03
	public void scheduleFixedRateTask2() {
		logger.info("CRON toutes les heures "+ System.currentTimeMillis() / 1000);

		operationService.refreshPredictifEveryHour();

	}

	@Scheduled(cron = "0 0 19 * * ?") // tous les jours à 19h
	public void scheduleFixedRateTask4() {
		logger.info("CRON tous les jours à 19h "+ System.currentTimeMillis() / 1000);

		// evol 3
		if ("RECETTE".equals(kamtar_env)) {
			operationService.listeOperationsRapportJournalier(new Date());
		}

	}

	@Scheduled(cron = "0 0 10 1 * ?") // le 1er de chaque mois ) 10h
	public void scheduleFixedRateTask5() {
		logger.info("CRON tous les jours à 19h "+ System.currentTimeMillis() / 1000);

		// evol 16
		if ("RECETTE".equals(kamtar_env)) {
			operationService.listeOperationsRapportMensuel(new Date());
		}

	}

}