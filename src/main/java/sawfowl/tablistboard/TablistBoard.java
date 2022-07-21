package sawfowl.tablistboard;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

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
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.event.LocaleServiseEvent;
import sawfowl.tablistboard.configure.Config;
import sawfowl.tablistboard.configure.LocalesPaths;
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
	private Config config;
	private LocaleService localeService;
	private Locales locales;
	private RegionUtil regionUtil;
	private TablistUtil tablistUtil;
	private ScoreboardUtil scoreboardUtil;

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
		return config;
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
			regionUtil = new RegionUtil();
			Sponge.eventManager().registerListeners(pluginContainer, regionUtil);
		}
	}

	@Listener
	public void onLocaleServisePostEvent(LocaleServiseEvent.Construct event) {
		localeService = event.getLocaleService();
		config = new Config(instance);
		locales = new Locales(instance);
		if(Sponge.pluginManager().plugin("regionguard").isPresent()) {
			tablistUtil = new TablistUtil(instance, true);
			scoreboardUtil = new ScoreboardUtil(instance, true);
		} else {
			tablistUtil = new TablistUtil(instance, false);
			scoreboardUtil = new ScoreboardUtil(instance, false);
			logger.warn("The RegionGuard plugin was not found. Some of the placeholders will not work.");
		}
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
						if(!context.associatedObject().isPresent()) return CommandResult.success();
						config.loadConfig();
						getLocaleService().getPluginLocales("tablistboard").values().forEach(locale -> {
							locale.reload();
						});
						locales.loadTablists();
						locales.loadBoards();
						Sponge.asyncScheduler().tasks(pluginContainer).clear();
						scheduleTabAndBoard();
						tablistUtil.scheduleChangeTabNumber();
						((Audience) context.associatedObject().get()).sendMessage(getLocales().getText(((LocaleSource) context.associatedObject().get()).locale(), LocalesPaths.RELOAD_MESSAGE));
						return CommandResult.success();
					}
				})
				.build();
		event.register(pluginContainer, commandReload, "tbreload");
	}

	@Listener
	public void onConnect(ServerSideConnectionEvent.Join event) {
		Sponge.asyncScheduler().submit(Task.builder().plugin(pluginContainer).execute(() -> {
			tablistUtil.setTablist(event.player());
			scoreboardUtil.setScoreboard(event.player());
		}).build());
		event.player().statistics();
	}

	private void scheduleTabAndBoard() {
		Sponge.asyncScheduler().submit(Task.builder().interval(config.getTablistTimer(), TimeUnit.SECONDS).plugin(pluginContainer).execute(() -> {
			Sponge.server().onlinePlayers().forEach(player -> {
				tablistUtil.setTablist(player);
			});
		}).build());
		Sponge.asyncScheduler().submit(Task.builder().interval(config.getScoreboardTimer(), TimeUnit.SECONDS).plugin(pluginContainer).execute(() -> {
			Sponge.server().onlinePlayers().forEach(player -> {
				scoreboardUtil.setScoreboard(player);
			});
		}).build());
	}

}
