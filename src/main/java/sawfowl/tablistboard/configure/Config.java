package sawfowl.tablistboard.configure;

import java.text.SimpleDateFormat;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import io.leangen.geantyref.TypeToken;
import sawfowl.localeapi.serializetools.TypeTokens;
import sawfowl.tablistboard.TablistBoard;

public class Config {

	private final TablistBoard plugin;
	private boolean save = false;
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private CommentedConfigurationNode rootNode;
	private SimpleDateFormat format;
	public Config(TablistBoard plugin) {
		this.plugin = plugin;
		configLoader = HoconConfigurationLoader.builder().defaultOptions(plugin.getLocaleService().getConfigurationOptions()).path(plugin.getConfigDirectory().resolve("Config.conf")).build();
		loadConfig();
		generate();
	}

	public void loadConfig() {
		try {
			rootNode = configLoader.load();
			format = new SimpleDateFormat(getNode("DateTimeFormat").getString("d.MM.yyyy HH:mm:s"));
		} catch (ConfigurateException e) {
			plugin.getLogger().error(e.getLocalizedMessage());
		}
	}

	public void saveConfig() {
		try {
			configLoader.save(rootNode);
		} catch (ConfigurateException e) {
			plugin.getLogger().error(e.getLocalizedMessage());
		}
	}

	public int getTablistTimer() {
		return getNode("Timers", "Tablist").getInt(5);
	}

	public int getScoreboardTimer() {
		return getNode("Timers", "Scoreboard").getInt(5);
	}

	public boolean isJsonLocales() {
		return getNode("LocaleJsonSerialize").getBoolean(false);
	}

	public SimpleDateFormat getSimpleDateFormat() {
		return format;
	}

	private void generate() {
		check(getNode("Timers"), "Refresh intervals.", null, null);
		check(getNode("Timers", "Tablist"), null, 15, TypeTokens.INTEGER_TOKEN);
		check(getNode("Timers", "Scoreboard"), null, 5, TypeTokens.INTEGER_TOKEN);
		check(getNode("LocaleJsonSerialize"), null, true, TypeTokens.BOOLEAN_TOKEN);
		check(getNode("DateTimeFormat"), null, "d.MM.yyyy HH:mm:s", TypeTokens.STRING_TOKEN);
		format = new SimpleDateFormat(getNode("DateTimeFormat").getString());
		if(save) saveConfig();
	}

	private CommentedConfigurationNode getNode(Object... node) {
		return rootNode.node(node);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void check(CommentedConfigurationNode node, String comment, Object value, TypeToken typeToken) {
        if(!node.virtual()) return;
    	save = true;
    	if(comment != null) {
        	node.comment(comment);
    	}
    	if(value != null) {
			try {
				node.set(typeToken, value);
			} catch (SerializationException e) {
				plugin.getLogger().error(e.getLocalizedMessage());
			}
    	}
    }

}
