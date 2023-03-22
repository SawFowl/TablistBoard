package sawfowl.tablistboard.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.locale.Locales;

import net.kyori.adventure.text.Component;
import sawfowl.tablistboard.TablistBoard;
import sawfowl.tablistboard.event.SetTablistEvent;

public class TablistUtil {

	private final TablistBoard plugin;
	private final boolean existRegionAPI;
	private Map<Locale, Integer> tabs = new HashMap<Locale, Integer>();
	private final Cause cause;
	public TablistUtil(TablistBoard plugin, boolean existRegionAPI) {
		this.plugin = plugin;
		this.existRegionAPI = existRegionAPI;
		cause = Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, plugin.getPluginContainer()).add(EventContextKeys.SUBJECT, Sponge.systemSubject()).build(), plugin.getPluginContainer());
		scheduleChangeTabNumber();
	}

	public void setTablist(ServerPlayer player) {
		SetTablistEvent tablistEvent = new SetTablistEvent() {

			Component header = replacer(plugin.getLocales().getTablists(player.locale()).get(getTabNumber(player.locale())).getHeader(), player);
			Component footer = replacer(plugin.getLocales().getTablists(player.locale()).get(getTabNumber(player.locale())).getFooter(), player);
			boolean cancelled;
			@Override
			public void setCancelled(boolean cancel) {
				cancelled = cancel;
			}
			@Override
			public boolean isCancelled() {
				return cancelled;
			}
			@Override
			public Cause cause() {
				return cause;
			}
			@Override
			public Component getHeader() {
				return header;
			}
			@Override
			public Component getFooter() {
				return footer;
			}
			@Override
			public void setHeader(Component component) {
				header = component;
			}
			@Override
			public void setFooter(Component component) {
				footer = component;
			}
			@Override
			public ServerPlayer getPlayer() {
				return player;
			}
		};
		Sponge.eventManager().post(tablistEvent);
		if(tablistEvent.isCancelled()) return;
		player.tabList().setHeaderAndFooter(tablistEvent.getHeader(), tablistEvent.getFooter());
		player.tabList().entries().forEach(entry -> {
			SetTablistEvent.SetEntry setEntry = new SetTablistEvent.SetEntry() {

				Component newDisplayName = replacer(plugin.getLocales().getTablists(player.locale()).get(getTabNumber(player.locale())).getPattern(), Sponge.server().player(entry.profile().uniqueId()).get());
				boolean cancelled;
				@Override
				public void setCancelled(boolean cancel) {
					cancelled = cancel;
				}
				@Override
				public boolean isCancelled() {
					return cancelled;
				}
				
				@Override
				public Cause cause() {
					return cause;
				}
				@Override
				public TabListEntry getTabListEntry() {
					return entry;
				}
				@Override
				public Component getNewDisplayName() {
					return newDisplayName;
				}
				@Override
				public void setDisplayName(Component component) {
					newDisplayName = component;
				}
			};
			Sponge.eventManager().post(setEntry);
			if(setEntry.isCancelled()) return;
			entry.setDisplayName(setEntry.getNewDisplayName());
		});
	}

	public void scheduleChangeTabNumber() {
		tabs.clear();
		plugin.getLocales().getTablists().keySet().forEach(k -> {
			tabs.put(k, 0);
		});
		Sponge.asyncScheduler().submit(Task.builder().delay(plugin.getConfig().getTablistSwitchInterval(), TimeUnit.SECONDS).plugin(plugin.getPluginContainer()).execute(() -> {
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
