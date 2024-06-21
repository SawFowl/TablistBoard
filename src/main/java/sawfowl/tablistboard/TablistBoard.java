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
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
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
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.event.LocaleServiseEvent;
import sawfowl.localeapi.api.serializetools.SerializeOptions;
import sawfowl.tablistboard.configure.Config;
import sawfowl.tablistboard.configure.Locales;
import sawfowl.tablistboard.utils.RegionUtil;
import sawfowl.tablistboard.utils.ScoreboardUtil;
import sawfowl.tablistboard.utils.TablistUtil;

@Plugin("tablistboard")
public class TablistBoard {

	private static TablistBoard instance;
	private Logger logger;
	private PluginContainer pluginContainer;
	private Path configDirectory;
	private ConfigurationReference<CommentedConfigurationNode> configLoader;
	private ValueReference<Config, CommentedConfigurationNode> config;
	private LocaleService localeService;
	private Locales locales;
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

	public Locales getLocales() {
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
	}

	@Listener
	public void onConstruct(ConstructPluginEvent event) {
		if(Sponge.pluginManager().plugin("regionguard").isPresent()) {
			regionUtil = new RegionUtil(instance);
			Sponge.eventManager().registerListeners(pluginContainer, regionUtil);
		}
	}

	@Listener
	public void onLocaleServisePostEvent(LocaleServiseEvent.Construct event) {
		try {
			configLoader = SerializeOptions.createHoconConfigurationLoader(2).path(configDirectory.resolve("Config.conf")).build().loadToReference();
			config = configLoader.referenceTo(Config.class);
			config.setAndSave(getConfig());
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		localeService = event.getLocaleService();
		locales = new Locales(instance);
		Sponge.eventManager().registerListeners(pluginContainer, locales);
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
				.executor(new CommandExecutor() {
					@Override
					public CommandResult execute(CommandContext context) throws CommandException {
						Audience audience = context.cause().audience();
						reload();
						audience.sendMessage(getLocales().getLocale(audience instanceof ServerPlayer ? ((ServerPlayer) audience).locale() : localeService.getSystemOrDefaultLocale()).getReload());
						return CommandResult.success();
					}
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
		event.cause().first(Audience.class).ifPresent(audience -> {
			audience.sendMessage(getLocales().getLocale(event.cause().first(ServerPlayer.class).map(ServerPlayer::locale).orElse(localeService.getSystemOrDefaultLocale())).getReload());
		});
	}

	private void reload() {
		try {
			configLoader = SerializeOptions.createHoconConfigurationLoader(2).path(configDirectory.resolve("Config.conf")).build().loadToReference();
			config = configLoader.referenceTo(Config.class);
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		getLocaleService().getPluginLocales("tablistboard").values().forEach(locale -> {
			locale.reload();
		});
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
			tabScheduler =  Sponge.asyncScheduler().submit(Task.builder().interval(getConfig().getTablist(), TimeUnit.SECONDS).plugin(pluginContainer).execute(() -> {
				Sponge.server().onlinePlayers().forEach(player -> {
					if(player.isOnline()) tablistUtil.setTablist(player);
				});
			}).build());
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
