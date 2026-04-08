package sawfowl.tablistboard;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocalesList;
import sawfowl.localeapi.api.config.ReferencedConfig;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.api.services.ConfigurationService;
import sawfowl.localeapi.api.services.LocaleService;
import sawfowl.tablistboard.configure.Config;
import sawfowl.tablistboard.configure.PluginLocale;
import sawfowl.tablistboard.utils.RegionUtil;
import sawfowl.tablistboard.utils.ScoreboardUtil;
import sawfowl.tablistboard.utils.TablistUtil;

@Plugin("tablistboard")
public class TablistBoard {

	private static TablistBoard instance;
	private Logger logger;
	private PluginContainer pluginContainer;
	private Path configDirectory;
	private ReferencedConfig<Config> config;
	private LocaleService localeService;
	private LocalesList<PluginLocale> locales;
	private RegionUtil regionUtil;
	private TablistUtil tablistUtil;
	private ScoreboardUtil scoreboardUtil;

	private ScheduledTask tabScheduler;
	private ScheduledTask scoreboardScheduler;

	public static TablistBoard getInstance() {
		return instance;
	}

	public PluginContainer getPluginContainer() {
		return pluginContainer;
	}

	public LocaleService getLocaleService() {
		return localeService;
	}

	public Logger getLogger() {
		return logger;
	}

	public Path getConfigDirectory() {
		return configDirectory;
	}

	public Config getConfig() {
		return config.get();
	}

	public LocalesList<PluginLocale> getLocales() {
		return locales;
	}

	public RegionUtil getRegionUtil() {
		return regionUtil;
	}

	@Inject
	public TablistBoard(PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDirectory) {
		instance = this;
		this.pluginContainer = pluginContainer;
		this.configDirectory = configDirectory;
		logger = LogManager.getLogger("TablistBoard");
		locales = LocaleService.getInstance().createLocales(pluginContainer, PluginLocale.class);
		if(!locales.contains(Locales.DEFAULT)) locales.createReferencedTranslation(ConfigTypes.HOCON, Locales.DEFAULT, PluginLocale.class);
		if(!locales.contains(Locales.RU_RU)) locales.createReferencedTranslation(ConfigTypes.HOCON, Locales.RU_RU, PluginLocale.createRussianLocale());
		config = ConfigurationService.getInstance().createReferencedConfig(pluginContainer, Config.class).setPath(configDirectory).setName("Config").setType(ConfigTypes.HOCON).setItemStackSerializerType(ItemStackSerializerType.JSON).build();
	}

	@Listener
	public void onConstruct(ConstructPluginEvent event) {
		if(Sponge.pluginManager().plugin("regionguard").isPresent()) regionUtil = new RegionUtil();
		tablistUtil = new TablistUtil(instance);
		scoreboardUtil = new ScoreboardUtil(instance);
	}

	@Listener
	public void onEnable(StartedEngineEvent<Server> event) throws ConfigurateException {
		scheduleTabAndBoard();
		if(!Sponge.server().serviceProvider().economyService().isPresent()) logger.warn("Economy plugin not found. Some of the placeholders will not work.");
	}

	@Listener
	public void onRegisterCommands(RegisterCommandEvent<Command.Parameterized> event) {
		Command.Parameterized commandReload = Command.builder()
				.shortDescription(Component.text("Reload plugin"))
				.permission("tablistboard.reload")
				.executor(context -> {
					Audience audience = context.cause().root() instanceof Audience a ? a : context.cause().audience();
					reload();
					audience.sendMessage(getLocales().getAsReferenced(audience instanceof ServerPlayer ? ((ServerPlayer) audience).locale() : localeService.getSystemOrDefaultLocale()).getReload());
					return CommandResult.success();
				})
				.build();
		event.register(pluginContainer, commandReload, "tbreload");
	}

	@Listener
	public void onConnect(ServerSideConnectionEvent.Join event) {
		Sponge.asyncScheduler().submit(Task.builder().plugin(pluginContainer).delay(5, TimeUnit.SECONDS).execute(() -> {
			tablistUtil.setTablist(event.player());
			scoreboardUtil.setScoreboard(event.player());
		}).build());
	}

	@Listener
	public void onRefresh(RefreshGameEvent event) {
		reload();
		event.cause().first(Audience.class).ifPresent(audience -> 
			audience.sendMessage(getLocales().getAsReferenced(event.cause().first(ServerPlayer.class).map(ServerPlayer::locale).orElse(localeService.getSystemOrDefaultLocale())).getReload())
		);
	}

	private void reload() {
		config.load();
		locales.forEach(locale -> locale.load());
		if(tabScheduler != null) {
			tabScheduler.cancel();
			tabScheduler = null;
		}
		if(scoreboardScheduler != null) {
			scoreboardScheduler.cancel();
			scoreboardScheduler = null;
		}
		Sponge.asyncScheduler().tasks(pluginContainer).clear();
		scheduleTabAndBoard();
	}

	private void scheduleTabAndBoard() {
		if(getConfig().getTablist() > 0) { 
			tablistUtil.scheduleChangeTabNumber();
			tabScheduler =  Sponge.asyncScheduler().submit(Task.builder().interval(getConfig().getTablist(), TimeUnit.SECONDS).plugin(pluginContainer).execute(() -> 
				Sponge.server().onlinePlayers().forEach(player -> {
					if(player.isOnline()) tablistUtil.setTablist(player);
				})
			).build());
		}
		if(getConfig().getScoreboard() > 0) {
			scoreboardUtil.scheduleChangeBoardNumber();
			scoreboardScheduler = Sponge.asyncScheduler().submit(Task.builder().interval(getConfig().getScoreboard(), TimeUnit.SECONDS).plugin(pluginContainer).execute(() -> {
				Sponge.server().onlinePlayers().forEach(player -> {
					if(player.isOnline()) scoreboardUtil.setScoreboard(player);
				});
			}).build());
		}
	}

	private Currency getDefaultCurrency() {
		return Sponge.server().serviceProvider().economyService().get().defaultCurrency();
	}

	public List<Currency> getCurrencies() {
		List<Currency> currencies = new ArrayList<Currency>();
		Sponge.game().findRegistry(RegistryTypes.CURRENCY).ifPresent(registry -> {
			if(registry.stream().count() > 0) currencies.addAll(registry.stream().collect(Collectors.toList()));
		});
		return !currencies.isEmpty() ? currencies : Arrays.asList(getDefaultCurrency());
	}

}
