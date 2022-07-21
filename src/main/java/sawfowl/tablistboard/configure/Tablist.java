package sawfowl.tablistboard.configure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@ConfigSerializable
public class Tablist {

	public Tablist(){}
	public Tablist(String header, String footer, String pattern) {
		this.header = header;
		this.footer = footer;
		this.pattern = pattern;
	}

	@Setting("Header")
	private String header;
	@Setting("Footer")
	private String footer;
	@Setting("Pattern")
	private String pattern;

	public Component getHeader() {
		return deserialize(header);
	}

	public Component getFooter() {
		return deserialize(footer);
	}

	public Component getPattern() {
		return deserialize(pattern);
	}

	private Component deserialize(String string) {
		try {
			return GsonComponentSerializer.gson().deserialize(string);
		} catch (Exception e) {
			return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
		}
	}

}
