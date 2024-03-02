package sawfowl.tablistboard.configure;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;

import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.api.TextUtils;
import sawfowl.tablistboard.TablistBoard;

@ConfigSerializable
public class Tablist {

	public Tablist(){}
	public Tablist(String header, String footer, String pattern) {
		this.header = TextUtils.deserialize(header);
		this.footer = TextUtils.deserialize(footer);
		this.pattern = TextUtils.deserialize(pattern);
	}

	@Setting("Header")
	private Component header;
	@Setting("Footer")
	private Component footer;
	@Setting("Pattern")
	private Component pattern;

	public Component getHeader(ServerPlayer player) {
		if(TablistBoard.getInstance().getRegionUtil() != null) return Text.of(header).applyPlaceholders(Component.empty(), player, player.world(), player.location(), player.serverLocation(), player.position(), TablistBoard.getInstance().getRegionUtil().getRegionAPI().findRegion(player.world(), player.blockPosition())).get();
		return Text.of(header).applyPlaceholders(Component.empty(), player, player.world(), player.location(), player.serverLocation(), player.position()).get();
	}

	public Component getFooter(ServerPlayer player) {
		if(TablistBoard.getInstance().getRegionUtil() != null) return Text.of(footer).applyPlaceholders(Component.empty(), player, player.world(), player.location(), player.serverLocation(), player.position(), TablistBoard.getInstance().getRegionUtil().getRegionAPI().findRegion(player.world(), player.blockPosition())).get();
		return Text.of(footer).applyPlaceholders(Component.empty(), player, player.world(), player.location(), player.serverLocation(), player.position()).get();
	}

	public Component getPattern(ServerPlayer player) {
		if(TablistBoard.getInstance().getRegionUtil() != null) return Text.of(pattern).applyPlaceholders(Component.empty(), player, player.world(), player.location(), player.serverLocation(), player.position(), TablistBoard.getInstance().getRegionUtil().getRegionAPI().findRegion(player.world(), player.blockPosition())).get();
		return Text.of(pattern).applyPlaceholders(Component.empty(), player, player.world(), player.location(), player.serverLocation(), player.position()).get();
	}

}
