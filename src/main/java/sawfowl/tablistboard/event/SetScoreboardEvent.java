package sawfowl.tablistboard.event;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.scoreboard.objective.Objective;

import sawfowl.tablistboard.configure.Scoreboard;

public interface SetScoreboardEvent extends Event, Cancellable {

	public ServerPlayer getPlayer();

	public Objective getObjective();

	public Scoreboard getScoreboard();

	public int getBoardNumber();

	public void setObjective(Objective objective);

	public void setScoreboard(Scoreboard scoreboard);

}
