package sawfowl.tablistboard.configure;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.api.statistic.Statistics;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import sawfowl.localeapi.api.Translation;
import sawfowl.tablistboard.utils.PlaceholderKeys;

@ConfigSerializable
public class PluginLocale implements Translation {

	public PluginLocale(){}

	@Setting("Tablist")
	private List<Tablist> tablists = Arrays.asList(new Tablist("&eTablist header\n&dSubheading", "&eTablist footer\n&dString 2", "%player-prefix% %entity-display-name% %player-suffix%"));
	@Setting("Scoreboard")
	private List<Scoreboard> scoreboards = createScoreboards();
	@Setting("ReloadMessage")
	private Component reload = toText("&aPlugin has been reloaded.");
	@Setting("DateTimeFormat")
	private String dateTimeFormat = "d.MM.yyyy HH:mm:s";
	@Setting("EconomyNotFound")
	private String economyNotFound = "Economy plugin not found. Some of the placeholders will not work.";
	private SimpleDateFormat format;

	public List<Tablist> getTablists() {
		return tablists;
	}

	public List<Scoreboard> getScoreboards() {
		return scoreboards;
	}

	public Component getReload() {
		return reload;
	}

	public SimpleDateFormat getDateTimeFormat() {
		return format == null ? format = new SimpleDateFormat(dateTimeFormat) : format;
	}

	public String getEconomyNotFound() {
		return economyNotFound;
	}

	public static PluginLocale createRussianLocale() {
		PluginLocale localeReference = new PluginLocale();
		localeReference.tablists = Arrays.asList(new Tablist("&eЗаголовок таблиста\n&dПодзаголовок", "&eПодвал таблиста\n&dСтрока 2", "%player-prefix% %entity-display-name% %player-suffix%"));
		Map<Integer, Component> scoreboard = new HashMap<Integer, Component>();
		scoreboard.put(4, toText("&aБаланс&f:&b %currency:dollar%%player-balance%"));
		scoreboard.put(3, toText("&aПрыжков&f:&b " + PlaceholderKeys.statisticKey(Statistics.JUMP.location().asString())));
		scoreboard.put(2, toText("&aПрефикс&f:&b %player-prefix%"));
		scoreboard.put(1, toText("&eНик:&3 %name%"));
		scoreboard.put(0, toText("&5Пример строки"));
		localeReference.scoreboards = Arrays.asList(new Scoreboard(toText("&6&lНазвание борда"), scoreboard));
		localeReference.reload = toText("&aПлагин перезагружен.");
		localeReference.economyNotFound = "Плагин экономики не найден. Часть плейсхолдеров не будут работать.";
		return localeReference;
	}

	private List<Scoreboard> createScoreboards() {
		Map<Integer, Component> scoreboard = new HashMap<Integer, Component>();
		scoreboard.put(4, toText("&aBalance&f:&b %currency:dollar%%player-balance%"));
		scoreboard.put(3, toText("&aJumps&f:&b " + PlaceholderKeys.statisticKey(Statistics.JUMP.location().asString())));
		scoreboard.put(2, toText("&aPrefix&f:&b %player-prefix%"));
		scoreboard.put(1, toText("&eNickname:&3 %name%"));
		scoreboard.put(0, toText("&5Example line"));
		return Arrays.asList(new Scoreboard(toText("&6&lBoardName"), scoreboard));
	}

	private static Component toText(String string) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
	}

}
