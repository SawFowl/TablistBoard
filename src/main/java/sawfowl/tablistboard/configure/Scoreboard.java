package sawfowl.tablistboard.configure;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;

import sawfowl.localeapi.api.Text;
import sawfowl.tablistboard.TablistBoard;

@ConfigSerializable
public class Scoreboard {

	public Scoreboard(){}
	public Scoreboard(Component objectiveName, Map<Integer, Component> scores) {
		this.objectiveName = objectiveName;
		this.scores = scores;
	}

	@Setting("Objective")
	private Component objectiveName = Component.text("Objective");
	@Setting("Scores")
	private Map<Integer, Component> scores;

	public Scoreboard copy() {
		return new Scoreboard(objectiveName, scores);
	}

	public Component getObjectiveName(ServerPlayer player) {
		if(TablistBoard.getInstance().getRegionUtil() != null) return Text.of(objectiveName).applyPlaceholders(Component.empty(), player, player.world(), player.location(), player.serverLocation(), player.position(), TablistBoard.getInstance().getRegionUtil().getRegionAPI().findRegion(player.world(), player.blockPosition())).get();
		return Text.of(objectiveName).applyPlaceholders(Component.empty(), player, player.world(), player.location(), player.serverLocation(), player.position()).get();
	}

	public Map<Integer, Component> getScores(ServerPlayer player) {
		if(scores == null || scores.isEmpty()) return new HashMap<Integer, Component>();
		if(TablistBoard.getInstance().getRegionUtil() != null) return scores.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> Text.of(entry.getValue()).applyPlaceholders(Component.empty(), player, player.world(), player.location(), player.serverLocation(), player.position(), TablistBoard.getInstance().getRegionUtil().getRegionAPI().findRegion(player.world(), player.blockPosition())).get()));
		return scores.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> Text.of(entry.getValue()).applyPlaceholders(Component.empty(), player, player.world(), player.location(), player.serverLocation(), player.position()).get()));
	}

}
