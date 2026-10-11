
/**

$$$License$$$

**/

/**
 * This bot is incomplete:
 * 	- En passant is not covered
 * 	- Neither is Rocade
 * 
 * -> The bot does get the Rocade move from the server
 * but does not calculate it down the tree...
 * -> En passant (both double as single) are not passed to the bot.
 **/

package org.bamboomy.c44;

import java.io.IOException;
import java.util.Properties;

import org.bamboomy.c44.rest.GamePoller;
import org.bamboomy.c44.rest.Poller;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication(scanBasePackages = { "org.bamboomy.c44" })
public class Application extends SpringBootServletInitializer {
	
	/*
	 * This bot is just plays 7 moves on pieces value,
	 * after that it tries to mate the opponents.
	 * 
	 *  You are free to improve this bot (and provide it to us)
	 *  given the license you can also build your own engine
	 *  based on this bot but you are invited to not do so.
	 */

	public static String BOT_NAME = "NotSoWeakBot";

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

		return application.sources(Application.class);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/index.html").allowedOrigins("http://localhost:8080");
			}
		};
	}

	public static void main(String[] args) throws Exception {

		Properties prop = new Properties();

		String serverUrl = null;

		String identifierHash = null;

		try {

			prop.load(Poller.class.getClassLoader().getResourceAsStream("application.properties"));

			serverUrl = prop.getProperty("server.url");

			System.out.println(prop.getProperty("identifierHash"));

			identifierHash = prop.getProperty("identifierHash");

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		SpringApplication.run(Application.class, args);

		System.out.println("waiting...");

		try {

			Thread.sleep(3_000);

		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		(new Thread(new GamePoller(serverUrl, identifierHash))).start();
	}
}