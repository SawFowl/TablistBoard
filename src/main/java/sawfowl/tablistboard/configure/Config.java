package sawfowl.tablistboard.configure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class Config {

	public Config(){}

	@Setting("TablistTimer")
	@Comment("Sets the refresh rate. A value of zero or negative will cause a turn off.")
	private int tablist = 15;
	@Setting("ScoreboardTimer")
	@Comment("Sets the refresh rate. A value of zero or negative will cause a turn off.")
	private int scoreboard = 5;
	@Setting("SwitchTablist")
	@Comment("Seconds before moving to the next variant in the localization config.")
	private int switchTablist = 15;
	@Setting("SwitchScoreboard")
	@Comment("Seconds before moving to the next variant in the localization config.")
	private int switchScoreboard = 5;

	public int getTablist() {
		return tablist;
	}

	public int getScoreboard() {
		return scoreboard;
	}

	public int getSwitchTablist() {
		return switchTablist;
	}

	public int getSwitchScoreboard() {
		return switchScoreboard;
	}

}
