package sawfowl.tablistboard.configure;

import java.util.Locale;
import java.util.Map;

import org.spongepowered.configurate.ConfigurateException;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.PluginLocale;
import sawfowl.tablistboard.TablistBoard;

public class Locales {

	private final TablistBoard plugin;
	public Locales(TablistBoard plugin) {
		this.plugin = plugin;
		plugin.getLocaleService().createPluginLocale("tablistboard", ConfigTypes.HOCON, org.spongepowered.api.util.locale.Locales.DEFAULT);
		plugin.getLocaleService().createPluginLocale("tablistboard", ConfigTypes.HOCON, org.spongepowered.api.util.locale.Locales.RU_RU);
		plugin.getLocaleService().setDefaultReference(plugin.getPluginContainer(), LocaleReference.class);
		generateDefault();
		generateRu();
	}

	public AbstractLocale getLocale(Locale locale) {
		return getPluginLocale(getLocales().containsKey(locale) ? locale : org.spongepowered.api.util.locale.Locales.DEFAULT).asReference(AbstractLocale.class);
	}

	public Map<Locale, PluginLocale> getLocales() {
		return plugin.getLocaleService().getPluginLocales("tablistboard");
	}

	private void generateDefault() {
		PluginLocale pluginLocale = getPluginLocale(org.spongepowered.api.util.locale.Locales.DEFAULT);
		if(pluginLocale.getLocaleRootNode().empty()) try {
			pluginLocale.setLocaleReference(new LocaleReference());
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
	}

	private void generateRu() {
		PluginLocale pluginLocale = getPluginLocale(org.spongepowered.api.util.locale.Locales.RU_RU);
		if(pluginLocale.getLocaleRootNode().empty()) try {
			pluginLocale.setLocaleReference(LocaleReference.createRussianLocale());
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
	}

	private PluginLocale getPluginLocale(Locale locale) {
		return getLocales().get(locale);
	}

}
