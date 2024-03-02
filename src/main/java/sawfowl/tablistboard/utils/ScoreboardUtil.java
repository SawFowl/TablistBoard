package sawfowl.tablistboard.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.scheduler.ScheduledTask;
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
	private Map<Locale, Integer> boards = new HashMap<Locale, Integer>();
	private final Cause cause;
	private ScheduledTask task;
	public ScoreboardUtil(TablistBoard plugin) {
		this.plugin = plugin;
		cause = Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, plugin.getPluginContainer()).add(EventContextKeys.SUBJECT, Sponge.systemSubject()).build(), plugin.getPluginContainer());
		scheduleChangeBoardNumber();
	}

	public void setScoreboard(ServerPlayer player) {
		int boardNumber = getBoardNumber(player.locale());
		SetScoreboardEvent scoreboardEvent = new SetScoreboardEvent() {

			sawfowl.tablistboard.configure.Scoreboard scoreboard = plugin.getLocales().getLocale(player.locale()).getScoreboards().get(boardNumber);
			Objective objective = Objective.builder().criterion(Criteria.DUMMY.get()).displayName(scoreboard.getObjectiveName(player)).name("obj").build();
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
		scoreboardEvent.getScoreboard().getScores(player).forEach((k, v) -> {
			if(getLength(v) <= 40) scoreboardEvent.getObjective().findOrCreateScore(v).setScore(k);
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
		if(task != null) {
			task.cancel();
			task = null;
		}
		boards.clear();
		boards = plugin.getLocales().getLocales().keySet().stream().collect(Collectors.toMap(locale -> locale, locale -> 0));
		task = Sponge.asyncScheduler().submit(Task.builder().delay(plugin.getConfig().getSwitchScoreboard(), TimeUnit.SECONDS).plugin(plugin.getPluginContainer()).execute(() -> {
			boards.forEach((k, v) -> {
				if(v + 1 < plugin.getLocales().getLocale(k).getScoreboards().size()) {
					v++;
				} else v = 0;
			});
		}).build());
	}

	private int getBoardNumber(Locale locale) {
		return boards.containsKey(locale) ? boards.get(locale) : boards.get(Locales.DEFAULT);
	}

	private int getLength(Component component) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(component).length();
	}

}
