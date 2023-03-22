package sawfowl.tablistboard.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.util.locale.Locales;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.tablistboard.TablistBoard;
import sawfowl.tablistboard.event.SetScoreboardEvent;

public class ScoreboardUtil {

	private final TablistBoard plugin;
	private final boolean existRegionAPI;
	private Map<Locale, Integer> boards = new HashMap<Locale, Integer>();
	private final Cause cause;
	public ScoreboardUtil(TablistBoard plugin, boolean existRegionAPI) {
		this.plugin = plugin;
		this.existRegionAPI = existRegionAPI;
		cause = Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, plugin.getPluginContainer()).add(EventContextKeys.SUBJECT, Sponge.systemSubject()).build(), plugin.getPluginContainer());
		scheduleChangeBoardNumber();
	}

	public void setScoreboard(ServerPlayer player) {
		int boardNumber = getBoardNumber(player.locale());
		SetScoreboardEvent scoreboardEvent = new SetScoreboardEvent() {

			sawfowl.tablistboard.configure.Scoreboard scoreboard = plugin.getLocales().getScoreboards(player.locale()).get(boardNumber).copy();
			Objective objective = Objective.builder().criterion(Criteria.DUMMY.get()).displayName(replacer(scoreboard.getObjectiveName(), player)).name("obj").build();
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
			public sawfowl.tablistboard.configure.Scoreboard getScoreboard() {
				return scoreboard;
			}
			@Override
			public Objective getObjective() {
				return objective;
			}
			@Override
			public ServerPlayer getPlayer() {
				return player;
			}
			@Override
			public int getBoardNumber() {
				return boardNumber;
			}
			@Override
			public void setObjective(Objective objective) {
				this.objective = objective;
			}
			@Override
			public void setScoreboard(sawfowl.tablistboard.configure.Scoreboard scoreboard) {
				this.scoreboard = scoreboard;
			}
		};
		Sponge.eventManager().post(scoreboardEvent);
		if(scoreboardEvent.isCancelled()) return;
		scoreboardEvent.getScoreboard().getScores().forEach((k, v) -> {
			Component component = replacer(v, player);
			if(getLength(component) <= 40) scoreboardEvent.getObjective().findOrCreateScore(component).setScore(k);
		});
		Scoreboard scoreboard = Scoreboard.builder().build();
		scoreboard.addObjective(scoreboardEvent.getObjective());
		scoreboard.updateDisplaySlot(scoreboardEvent.getObjective(), DisplaySlots.SIDEBAR.get());
		try {
			player.setScoreboard(scoreboard);
		} catch (Exception e) {
		}
	}

	public void scheduleChangeBoardNumber() {
		boards.clear();
		plugin.getLocales().getScoreboards().keySet().forEach(k -> {
			boards.put(k, 0);
		});
		Sponge.asyncScheduler().submit(Task.builder().delay(plugin.getConfig().getScoreboardSwitchInterval(), TimeUnit.SECONDS).plugin(plugin.getPluginContainer()).execute(() -> {
			boards.forEach((k, v) -> {
				if(v + 1 < plugin.getLocales().getScoreboards(k).size()) {
					v++;
				} else v = 0;
			});
		}).build());
	}

	private Component replacer(Component component, ServerPlayer player) {
		return existRegionAPI ? ReplaceUtil.replacePlaceholders(component, player, plugin.getRegionUtil().getRegionAPI().findRegion(player.world(), player.blockPosition()), plugin.getConfig().getSimpleDateFormat()) : ReplaceUtil.replacePlaceholders(component, player);
	}

	private int getBoardNumber(Locale locale) {
		return boards.containsKey(locale) ? boards.get(locale) : boards.get(Locales.DEFAULT);
	}

	private int getLength(Component component) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(component).length();
	}

}
