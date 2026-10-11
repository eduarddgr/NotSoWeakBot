package org.bamboomy.c44.profiles;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.bamboomy.c44.bot.player.Player;

@Configuration(proxyBeanMethods = false)
@Profile("mate")
public class MateConfiguration {

	{
		Player.MATE_PROFILE = true;
	}
}
