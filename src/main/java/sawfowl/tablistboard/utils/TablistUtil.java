package sawfowl.tablistboard.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.locale.Locales;

import net.kyori.adventure.text.Component;
import sawfowl.tablistboard.TablistBoard;

public class TablistUtil {

	private final TablistBoard plugin;
	private final boolean existRegionAPI;
	private Map<Locale, Integer> tabs = new HashMap<Locale, Integer>();
	public TablistUtil(TablistBoard plugin, boolean existRegionAPI) {
		this.plugin = plugin;
		this.existRegionAPI = existRegionAPI;
		scheduleChangeTabNumber();
	}

	public void setTablist(ServerPlayer player) {
		player.tabList().setHeaderAndFooter(replacer(plugin.getLocales().getTablists(player.locale()).get(getTabNumber(player.locale())).getHeader(), player), replacer(plugin.getLocales().getTablists(player.locale()).get(getTabNumber(player.locale())).getFooter(), player));
		player.tabList().entries().forEach(entry -> {
			entry.setDisplayName(replacer(plugin.getLocales().getTablists(player.locale()).get(getTabNumber(player.locale())).getPattern(), Sponge.server().player(entry.profile().uniqueId()).get()));
		});
	}

	public void scheduleChangeTabNumber() {
		tabs.clear();
		plugin.getLocales().getTablists().keySet().forEach(k -> {
			tabs.put(k, 0);
		});
		Sponge.asyncScheduler().submit(Task.builder().delay(Ticks.of(5)).plugin(plugin.getPluginContainer()).execute(() -> {
			tabs.forEach((k, v) -> {
				if(v + 1 < plugin.getLocales().getTablists(k).size()) {
					v++;
				} else v = 0;
			});
		}).build());
	}

	private Component replacer(Component component, ServerPlayer player) {
		return existRegionAPI ? ReplaceUtil.replacePlaceholders(component, player, plugin.getRegionUtil().getRegionAPI().findRegion(player.world(), player.blockPosition()), plugin.getConfig().getSimpleDateFormat()) : ReplaceUtil.replacePlaceholders(component, player);
	}

	private int getTabNumber(Locale locale) {
		return tabs.containsKey(locale) ? tabs.get(locale) : tabs.get(Locales.DEFAULT);
	}

}
