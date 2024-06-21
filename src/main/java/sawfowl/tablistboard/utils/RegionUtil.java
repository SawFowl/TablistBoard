package sawfowl.tablistboard.utils;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;

import sawfowl.regionguard.RegionGuard;
import sawfowl.regionguard.api.RegionAPI;
import sawfowl.tablistboard.TablistBoard;

public class RegionUtil {

	private RegionAPI regionAPI;
	public RegionUtil(TablistBoard plugin) {
	}

	@Listener
	public void onPostAPI(RegionAPI.PostAPI event) {
		regionAPI = event.getAPI();
		Sponge.eventManager().unregisterListeners(this);
	}

	public RegionAPI getRegionAPI() {
		if(regionAPI == null) regionAPI = ((RegionGuard) Sponge.pluginManager().plugin("regionguard").get().instance()).getAPI();
		return regionAPI;
	}

}
