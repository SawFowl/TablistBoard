package sawfowl.tablistboard.configure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.statistic.Statistics;
import org.spongepowered.configurate.serialize.SerializationException;

import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.event.LocaleEvent;
import sawfowl.localeapi.utils.AbstractLocaleUtil;
import sawfowl.tablistboard.TablistBoard;
import sawfowl.tablistboard.utils.ReplaceUtil;

public class Locales {

	private final TablistBoard plugin;
	private final boolean json;
	private final TypeToken<List<Scoreboard>> SCORES_TOKEN;
	private final TypeToken<List<Tablist>> TABS_TOKEN;
	private Map<Locale, List<Scoreboard>> scoreboards = new HashMap<Locale, List<Scoreboard>>();
	private Map<Locale, List<Tablist>> tablists = new HashMap<Locale, List<Tablist>>();
	public Locales(TablistBoard plugin) {
		this.plugin = plugin;
		json = plugin.getConfig().isJsonLocales();
		SCORES_TOKEN = new TypeToken<List<Scoreboard>>() {};
		TABS_TOKEN = new TypeToken<List<Tablist>>() {};;
		plugin.getLocaleService().createPluginLocale("tablistboard", ConfigTypes.JSON, org.spongepowered.api.util.locale.Locales.DEFAULT);
		plugin.getLocaleService().createPluginLocale("tablistboard", ConfigTypes.JSON, org.spongepowered.api.util.locale.Locales.RU_RU);
		generateDefault();
		generateRu();
		loadBoards();
		loadTablists();
	}

	@Listener
	public void onUpdate(LocaleEvent event) {
		loadBoards();
		loadTablists();
	}

	public Component getText(Locale locale, Object... path) {
		return getAbstractLocaleUtil(locale).getComponent(json, path);
	}

	public Component getTextWithReplaced(Locale locale, Map<String, String> map, Object... path) {
		return replace(getText(locale, path), map);
	}

	public Component getTextReplaced(Locale locale, Map<String, Component> map, Object... path) {
		return replaceComponent(getText(locale, path), map);
	}

	public Component getTextFromDefault(Object... path) {
		return getAbstractLocaleUtil(org.spongepowered.api.util.locale.Locales.DEFAULT).getComponent(json, path);
	}

	public void loadTablists() {
		tablists.clear();
		plugin.getLocaleService().getPluginLocales("tablistboard").forEach((locale, localeUtil) -> {
			try {
				tablists.put(locale, localeUtil.getLocaleNode(LocalesPaths.TABLIST).get(TABS_TOKEN));
			} catch (SerializationException e) {
				plugin.getLogger().error(e.getLocalizedMessage());
			}
		});
	}

	public void loadBoards() {
		scoreboards.clear();
		plugin.getLocaleService().getPluginLocales("tablistboard").forEach((locale, localeUtil) -> {
			try {
				scoreboards.put(locale, localeUtil.getLocaleNode(LocalesPaths.SCOREBOARD).get(SCORES_TOKEN));
			} catch (SerializationException e) {
				plugin.getLogger().error(e.getLocalizedMessage());
			}
		});
	}

	public Map<Locale, List<Tablist>> getTablists() {
		return tablists;
	}

	public List<Tablist> getTablists(Locale locale) {
		return tablists.containsKey(locale) ? tablists.get(locale) : tablists.get(org.spongepowered.api.util.locale.Locales.DEFAULT);
	}

	public List<Scoreboard> getScoreboards(Locale locale) {
		return scoreboards.containsKey(locale) ? scoreboards.get(locale) : scoreboards.get(org.spongepowered.api.util.locale.Locales.DEFAULT);
	}

	public Map<Locale, List<Scoreboard>> getScoreboards() {
		return scoreboards;
	}

	private void generateDefault() {
		Locale locale = org.spongepowered.api.util.locale.Locales.DEFAULT;
		boolean save = check(locale, toText("&aPlugin has been reloaded."), null, LocalesPaths.RELOAD_MESSAGE);
		save = checkTabs(locale, Arrays.asList(new Tablist("&eTablist header\n&dSubheading", "&eTablist footer\n&dString 2", "%prefix% %player% %suffix%")), LocalesPaths.TABLIST) || save;
		Map<Integer, String> scoreboard = new HashMap<Integer, String>();
		scoreboard.put(4, serializeScoreString("&aBalance&f:&b %currency:dollar%%balance%"));
		scoreboard.put(3, serializeScoreString("&aJumps&f:&b " + ReplaceUtil.Keys.statisticKey(Statistics.JUMP.location().asString())));
		scoreboard.put(2, serializeScoreString("&aPrefix&f:&b " + ReplaceUtil.Keys.PREFIX));
		scoreboard.put(1, serializeScoreString("&eNickname:&3 " + ReplaceUtil.Keys.PLAYER));
		scoreboard.put(0, serializeScoreString("&5Example line"));
		save = checkScores(locale, Arrays.asList(new Scoreboard(serializeScoreString("&6&lBoardName"), scoreboard)), LocalesPaths.SCOREBOARD) || save;
		if(save) save(locale);
	}

	private void generateRu() {
		Locale locale = org.spongepowered.api.util.locale.Locales.RU_RU;
		boolean save = check(locale, toText("&aПлагин перезагружен."), null, LocalesPaths.RELOAD_MESSAGE);
		save = checkTabs(locale, Arrays.asList(new Tablist("&eЗаголовок таблиста\n&dПодзаголовок", "&eПодвал таблиста\n&dСтрока 2", "%prefix% %player% %suffix%")), LocalesPaths.TABLIST) || save;
		Map<Integer, String> scoreboard = new HashMap<Integer, String>();
		scoreboard.put(4, serializeScoreString("&aБаланс&f:&b %currency:dollar%%balance%"));
		scoreboard.put(3, serializeScoreString("&aПрыжков&f:&b " + ReplaceUtil.Keys.statisticKey(Statistics.JUMP.location().asString())));
		scoreboard.put(2, serializeScoreString("&aПрефикс&f:&b " + ReplaceUtil.Keys.PREFIX));
		scoreboard.put(1, serializeScoreString("&eНик:&3 " + ReplaceUtil.Keys.PLAYER));
		scoreboard.put(0, serializeScoreString("&5Пример строки"));
		save = checkScores(locale, Arrays.asList(new Scoreboard(serializeScoreString("&6&lНазвание борда"), scoreboard)), LocalesPaths.SCOREBOARD) || save;
		if(save) save(locale);
	}

	private Component replace(Component component, Map<String, String> map) {
		for(Entry<String, String> entry : map.entrySet()) {
			component = component.replaceText(TextReplacementConfig.builder().match(entry.getKey()).replacement(Component.text(entry.getValue())).build());
		}
		return component;
	}

	private Component replaceComponent(Component component, Map<String, Component> map) {
		for(Entry<String, Component> entry : map.entrySet()) {
			component = component.replaceText(TextReplacementConfig.builder().match(entry.getKey()).replacement(entry.getValue()).build());
		}
		return component;
	}

	private AbstractLocaleUtil getAbstractLocaleUtil(Locale locale) {
		return plugin.getLocaleService().getPluginLocales("tablistboard").get(locale);
	}

	private Component toText(String string) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
	}

	private String serializeJson(Component component) {
		return GsonComponentSerializer.gson().serialize(component);
	}

	private String serializeScoreString(String string) {
		if(json) string = serializeJson(toText(string));
		return string;
	}

	private boolean check(Locale locale, Component value, String comment, Object... path) {
		return getAbstractLocaleUtil(locale).checkComponent(json, value, comment, path);
	}

	private boolean checkTabs(Locale locale, List<Tablist> value, Object... path) {
		if(!getAbstractLocaleUtil(locale).getLocaleNode(path).virtual()) return false;
		try {
			getAbstractLocaleUtil(locale).getLocaleNode(path).set(TABS_TOKEN, value);
		} catch (SerializationException e) {
			plugin.getLogger().error(e.getLocalizedMessage());
		}
		return true;
	}

	private boolean checkScores(Locale locale, List<Scoreboard> value, Object... path) {
		if(!getAbstractLocaleUtil(locale).getLocaleNode(path).virtual()) return false;
		try {
			getAbstractLocaleUtil(locale).getLocaleNode(path).set(SCORES_TOKEN, value);
		} catch (SerializationException e) {
			plugin.getLogger().error(e.getLocalizedMessage());
		}
		return true;
	}

	private void save(Locale locale) {
		getAbstractLocaleUtil(locale).saveLocaleNode();
	}

}
