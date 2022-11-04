package sawfowl.tablistboard.configure;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@ConfigSerializable
public class Scoreboard {

	public Scoreboard(){}
	public Scoreboard(String objectiveName, Map<Integer, String> scores) {
		this.objectiveName = objectiveName;
		this.scores = scores;
	}

	@Setting("Objective")
	private String objectiveName = "Objective";
	@Setting("Scores")
	private Map<Integer, String> scores;

	public Scoreboard copy() {
		return new Scoreboard(objectiveName, scores);
	}

	public Component getObjectiveName() {
		return deserialize(objectiveName);
	}

	public Map<Integer, Component> getScores() {
		Map<Integer, Component> map = new HashMap<>();
		if(scores != null) scores.forEach((k,v) -> {
			map.put(k,deserialize(v));
		});
		return map;
	}

	private Component deserialize(String string) {
		try {
			return GsonComponentSerializer.gson().deserialize(string);
		} catch (Exception e) {
			return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
		}
	}

}
