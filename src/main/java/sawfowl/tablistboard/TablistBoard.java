package sawfowl.tablistboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.spongepowered.api.ResourceKey;
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
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.world.server.ServerWorld;
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
import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.api.TextUtils;
import sawfowl.localeapi.api.event.LocaleServiseEvent;
import sawfowl.localeapi.api.placeholders.Placeholder;
import sawfowl.localeapi.api.placeholders.Placeholders;
import sawfowl.localeapi.api.serializetools.SerializeOptions;
import sawfowl.tablistboard.configure.Config;
import sawfowl.tablistboard.configure.Locales;
import sawfowl.tablistboard.utils.PlaceholderKeys;
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

	private boolean isPresentRegistry = false;

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
		if(!Sponge.pluginManager().plugin("regionguard").isPresent()) logger.warn("The RegionGuard plugin was not found. Some of the placeholders will not work.");
		tablistUtil = new TablistUtil(instance);
		scoreboardUtil = new ScoreboardUtil(instance);
		Placeholders.register(ServerPlayer.class, "PlayerLevel", (original, player, def) -> original.replace(PlaceholderKeys.PLAYER_LEVEL, player.experienceLevel().get()));
		Placeholders.register(ServerPlayer.class, "TPS", (original, player, def) -> original.replace(PlaceholderKeys.SERVER_TPS, BigDecimal.valueOf(Sponge.server().ticksPerSecond()).setScale(2, RoundingMode.HALF_UP).doubleValue()));
		Placeholders.register(ServerWorld.class, "Time", (original, player, def) -> original.replace(PlaceholderKeys.WORLD_TIME, player.world().properties().dayTime().hour() + ":" + player.world().properties().dayTime().minute()));
		Placeholders.register(ServerPlayer.class, "OnlinePlayers", (original, player, def) -> original.replace(PlaceholderKeys.ONLINE_PLAYERS, Sponge.server().onlinePlayers().size()));
		Placeholders.register(ServerPlayer.class, "StaffsOnline", (original, player, def) -> original.replace(PlaceholderKeys.STAFFS_ONLINE, Sponge.server().onlinePlayers().stream().filter(player2 -> (player2.hasPermission("tablistboard.staff"))).count()));
		Placeholders.register(ServerPlayer.class, "PlayerBalance", new Placeholder<ServerPlayer>() {
			@Override
			public Text apply(Text original, ServerPlayer player, Component def) {
				if(economyIsPresent()) {
					String plain = original.toPlain();
					if(plain.contains("%currency:")) {
						String currencyKey = getCurrencyKey(plain);
						Currency currency = getCurrency(currencyKey);
						original.replace(PlaceholderKeys.currency(currencyKey), getCurrencySymbol(currency)).replace(PlaceholderKeys.PLAYER_BALANCE, getBalance(player, getDefaultCurrency()));
					} else original.replace(PlaceholderKeys.PLAYER_BALANCE, getBalance(player, getDefaultCurrency()));
					plain = null;
				}
				return original;
			}
		});
		Placeholders.register(ServerPlayer.class, "PlayerStat", new Placeholder<ServerPlayer>() {
			@Override
			public Text apply(Text original, ServerPlayer player, Component def) {
				String plain = original.toPlain();
				if(!plain.contains("%statistic:")) return original;
				String statisticKey = getStatisticKey(plain);
				getStatistic(player, statisticKey).ifPresent(stat -> {
					original.replace(PlaceholderKeys.statisticKey(statisticKey), String.valueOf(player.get(org.spongepowered.api.data.Keys.STATISTICS).get().get(stat)));
				});
				plain = null;
				return original;
			}
		});
	}

	@Listener
	public void onEnable(StartedEngineEvent<Server> event) throws ConfigurateException {
		scheduleTabAndBoard();
		if(!Sponge.server().serviceProvider().economyService().isPresent()) logger.warn("Economy plugin not found. Some of the placeholders will not work.");
		isPresentRegistry = RegistryTypes.CURRENCY.find().isPresent();
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

	private boolean economyIsPresent() {
		return Sponge.server().serviceProvider().economyService().isPresent();
	}

	private String getStatisticKey(String string) {
		return string.split("%statistic:")[1].split("%")[0];
	}

	private String getCurrencyKey(String string) {
		return string.split("%currency:")[1].split("%")[0];
	}

	private String getBalance(ServerPlayer player, Currency currency) {
		try {
			Optional<UniqueAccount> uOpt = Sponge.server().serviceProvider().economyService().get().findOrCreateAccount(player.uniqueId());
			if (uOpt.isPresent()) {
				return uOpt.get().balance(currency).setScale(2).toPlainString();
			}
		} catch (Exception ignored) {
		}
		return "";
	}

	private Currency getDefaultCurrency() {
		return Sponge.server().serviceProvider().economyService().get().defaultCurrency();
	}

	private String getCurrencySymbol(Currency currency) {
		return TextUtils.clearDecorations(currency.symbol());
	}

	private Currency getCurrency(String string) {
		if(string == null) return getDefaultCurrency();
		Optional<Currency> optCurrency = getCurrencies().stream().filter(currency -> ((isPresentRegistry && RegistryTypes.CURRENCY.find().get().findValue(ResourceKey.resolve(string)).isPresent()) || TextUtils.clearDecorations(currency.displayName()).equalsIgnoreCase(string) || TextUtils.clearDecorations(currency.symbol()).equalsIgnoreCase(string))).findFirst();
		return optCurrency.isPresent() ? optCurrency.get() : getDefaultCurrency();
	}

	public List<Currency> getCurrencies() {
		List<Currency> currencies = new ArrayList<Currency>();
		Sponge.game().findRegistry(RegistryTypes.CURRENCY).ifPresent(registry -> {
			if(registry.stream().count() > 0) currencies.addAll(registry.stream().collect(Collectors.toList()));
		});
		return !currencies.isEmpty() ? currencies : Arrays.asList(getDefaultCurrency());
	}

	private static Optional<Statistic> getStatistic(ServerPlayer player, String key) {
		return player.statistics().keySet().stream().filter(statistic -> (statistic.toString().contains(key.replace(':', '.')))).findFirst();
	}

}
