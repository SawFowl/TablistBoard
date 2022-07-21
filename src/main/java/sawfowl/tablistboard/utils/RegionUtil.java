package sawfowl.tablistboard.utils;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;

import sawfowl.regionguard.RegionGuard;
import sawfowl.regionguard.api.RegionAPI;
import sawfowl.regionguard.api.events.RegionAPIPostEvent;

public class RegionUtil {

	private RegionAPI regionAPI;

	@Listener
	public void onPostAPI(RegionAPIPostEvent.PostAPI event) {
		regionAPI = event.getAPI();
	}

	public RegionAPI getRegionAPI() {
		if(regionAPI == null) regionAPI = ((RegionGuard) Sponge.pluginManager().plugin("regionguard").get().instance()).getAPI();
		return regionAPI;
	}

}
