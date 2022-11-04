package sawfowl.tablistboard.event;

import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;

import net.kyori.adventure.text.Component;

public interface SetTablistEvent extends Event, Cancellable {

	public Component getHeader();

	public Component getFooter();

	public void setHeader(Component component);

	public void setFooter(Component component);

	interface SetEntry extends Event, Cancellable {

		public TabListEntry getTabListEntry();

		public Component getNewDisplayName();

		public void setDisplayName(Component component);

	}

}
