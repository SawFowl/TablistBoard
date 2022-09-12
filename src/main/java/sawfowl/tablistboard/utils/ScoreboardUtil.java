package sawfowl.tablistboard.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.locale.Locales;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.tablistboard.TablistBoard;

public class ScoreboardUtil {

	private final TablistBoard plugin;
	private final boolean existRegionAPI;
	private Map<Locale, Integer> boards = new HashMap<Locale, Integer>();
	public ScoreboardUtil(TablistBoard plugin, boolean existRegionAPI) {
		this.plugin = plugin;
		this.existRegionAPI = existRegionAPI;
		scheduleChangeBoardNumber();
	}

	public void setScoreboard(ServerPlayer player) {
		int boardNumber = getBoardNumber(player.locale());
		sawfowl.tablistboard.configure.Scoreboard scoreboard = plugin.getLocales().getScoreboards(player.locale()).get(boardNumber);
		Objective objective = Objective.builder().criterion(Criteria.DUMMY.get()).displayName(replacer(scoreboard.getObjectiveName(), player)).name("obj").build();
		scoreboard.getScores().forEach((k, v) -> {
			Component component = replacer(v, player);
			if(getLength(component) <= 40) objective.findOrCreateScore(component).setScore(k);
		});
		Scoreboard toSet = Scoreboard.builder().build();
		toSet.addObjective(objective);
		toSet.updateDisplaySlot(objective, DisplaySlots.SIDEBAR.get());
		try {
			player.setScoreboard(toSet);
		} catch (Exception e) {
		}
	}

	public void scheduleChangeBoardNumber() {
		boards.clear();
		plugin.getLocales().getScoreboards().keySet().forEach(k -> {
			boards.put(k, 0);
		});
		Sponge.asyncScheduler().submit(Task.builder().delay(Ticks.of(5)).plugin(plugin.getPluginContainer()).execute(() -> {
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
