package sawfowl.tablistboard.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import sawfowl.localeapi.api.placeholders.Placeholders;
import sawfowl.regionguard.RegionGuard;
import sawfowl.regionguard.api.RegionAPI;
import sawfowl.regionguard.api.data.Region;
import sawfowl.tablistboard.TablistBoard;

public class RegionUtil {

	private RegionAPI regionAPI;
	public RegionUtil(TablistBoard plugin) {
		Placeholders.register(ServerPlayer.class, "RegionCreated", (original, player, def) -> original.replace(PlaceholderKeys.REGION_DATE, getDateCreated(player, getRegion(player).getCreationTime(), plugin.getLocales().getLocale(player.locale()).getDateTimeFormat())));
		Placeholders.register(ServerPlayer.class, "RegionTrustLevel", (original, player, def) -> original.replace(PlaceholderKeys.REGION_TRUST_LEVEL, getRegion(player).getMemberData(player).map(data -> data.getTrustType().toString()).orElse("-")));
		Placeholders.register(ServerPlayer.class, "BlocksCreated", (original, player, def) -> original.replace(PlaceholderKeys.REGIONGUARD_BLOCKS_CLAIMED, getRegionAPI().getClaimedBlocks(player)));
		Placeholders.register(ServerPlayer.class, "ClaimsCreated", (original, player, def) -> original.replace(PlaceholderKeys.REGIONGUARD_CLAIMS_CREATED, getRegionAPI().getClaimedRegions(player)));
		Placeholders.register(ServerPlayer.class, "LimitBlocks", (original, player, def) -> original.replace(PlaceholderKeys.REGIONGUARD_BLOCKS_LIMIT, getRegionAPI().getLimitBlocks(player)));
		Placeholders.register(ServerPlayer.class, "LimitClaims", (original, player, def) -> original.replace(PlaceholderKeys.REGIONGUARD_CLAIMS_LIMIT, getRegionAPI().getLimitClaims(player)));
		Placeholders.register(ServerPlayer.class, "LimitSubdivisions", (original, player, def) -> original.replace(PlaceholderKeys.REGIONGUARD_SUBDIVISIONS_LIMIT, getRegionAPI().getLimitSubdivisions(player)));
		Placeholders.register(ServerPlayer.class, "LimitMembers", (original, player, def) -> original.replace(PlaceholderKeys.REGIONGUARD_MEMBERS_LIMIT, getRegionAPI().getLimitMembers(player)));
		Placeholders.register(ServerPlayer.class, "MaxLimitBlocks", (original, player, def) -> original.replace(PlaceholderKeys.REGIONGUARD_MAX_BLOCKS_LIMIT, getRegionAPI().getLimitMaxBlocks(player)));
		Placeholders.register(ServerPlayer.class, "MaxLimitClaims", (original, player, def) -> original.replace(PlaceholderKeys.REGIONGUARD_MAX_CLAIMS_LIMIT, getRegionAPI().getLimitMaxClaims(player)));
		Placeholders.register(ServerPlayer.class, "MaxLimitSubdivisions", (original, player, def) -> original.replace(PlaceholderKeys.REGIONGUARD_MAX_SUBDIVISIONS_LIMIT, getRegionAPI().getLimitMaxSubdivisions(player)));
		Placeholders.register(ServerPlayer.class, "MaxLimitMembers", (original, player, def) -> original.replace(PlaceholderKeys.MAX_MEMBERS_LIMIT, getRegionAPI().getLimitMaxMembers(player)));
		Placeholders.register(ServerPlayer.class, "RegionName", (original, player, def) -> original.replace(PlaceholderKeys.REGION_NAME, getRegion(player).getName(player.locale())));
		Placeholders.register(Region.class, "RegionType", (original, region, def) -> original.replace(PlaceholderKeys.REGION_TYPE, region.getType().toString()));
		Placeholders.register(Region.class, "RegionOwner", (original, region, def) -> original.replace(PlaceholderKeys.REGION_OWNER, region.getOwnerName()));
		Placeholders.register(Region.class, "RegionMinPos", (original, region, def) -> original.replace(PlaceholderKeys.REGION_MIN, !region.isGlobal() ? region.getCuboid().getMin().toString() : region.getWorld().map(ServerWorld::min).orElse(Vector3i.ZERO)));
		Placeholders.register(Region.class, "RegionMaxPos", (original, region, def) -> original.replace(PlaceholderKeys.REGION_MAX, !region.isGlobal() ? region.getCuboid().getMax().toString() : region.getWorld().map(ServerWorld::max).orElse(Vector3i.ZERO)));
		Placeholders.register(Region.class, "RegionSize", (original, region, def) -> original.replace(PlaceholderKeys.REGION_SIZE, !region.isGlobal() ? region.getCuboid().getSizeXYZ().toInt().toString() : region.getWorld().map(ServerWorld::size).orElse(Vector3i.ZERO)));
		Placeholders.register(Region.class, "RegionMembers", (original, region, def) -> original.replace(PlaceholderKeys.REGION_MEMBERS_SIZE, region.getTotalMembers()));
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

	private Region getRegion(ServerPlayer player) {
		return getRegionAPI().findRegion(player.world(), player.blockPosition());
	}

	private String getDateCreated(ServerPlayer player, long time, SimpleDateFormat dateFormat) {
		Calendar calendar = Calendar.getInstance(player.locale());
		calendar.setTimeInMillis(time);
		return dateFormat.format(calendar.getTime());
	}

}
