package sawfowl.tablistboard.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.statistic.Statistic;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.regionguard.api.data.Region;
import sawfowl.tablistboard.TablistBoard;

public class ReplaceUtil {

	public static class Keys {

		public static final String PLAYER = "%player%";

		public static final String DISPLAY_NAME = "%display_name%";

		public static final String RANK = "%rank%";

		public static final String PREFIX = "%prefix%";

		public static final String SUFFIX = "%suffix%";

		public static final String WORLD = "%world%";

		public static final String PING = "%ping%";

		public static final String PLAYER_UUID = "%player-uuid%";

		public static final String PLAYER_LEVEL = "%player-level%";

		public static final String TPS = "%tps%";

		public static final String TIME = "%time%";

		public static final String ONLINE_PLAYERS = "%online-players%";

		public static final String STAFFS_ONLINE = "%staffs-online%";

		public static final String BALANCE = "%balance%";

		public static final String REGION = "%region%";

		public static final String TYPE = "%type%";

		public static final String OWNER = "%owner%";

		public static final String MIN = "%min%";

		public static final String MAX = "%max%";

		public static final String REGION_SIZE = "%region-size%";

		public static final String MEMBERS_SIZE = "%members-size%";

		public static final String REGION_ROLE = "%region-role%";

		public static final String DATE = "%date%";

		public static final String CLAIMS_CREATED = "%claims_created%";

		public static final String CLAIMS_LIMIT = "%claims_limit%";

		public static final String BLOCKS_CLAIMED = "%blocks_claimed%";

		public static final String BLOCKS_LIMIT = "%blocks_limit%";

		public static final String SUBDIVISIONS_LIMIT = "%subdivisions_limit%";

		public static final String MEMBERS_LIMIT = "%members_limit%";

		public static final String MAX_CLAIMS_LIMIT = "%max_claims_limit%";

		public static final String MAX_BLOCKS_LIMIT = "%max_blocks_limit%";

		public static final String MAX_SUBDIVISIONS_LIMIT = "%max_subdivisions_limit%";

		public static final String MAX_MEMBERS_LIMIT = "%max_members_limit%";
	
		public static String statisticKey(String statistic) {
			return "%statistic:" + statistic + "%";
		}
		
		public static String currency(String string) {
			return "%currency:" + string + "%";
		}
		
	}

	public static Component replacePlaceholders(Component component, ServerPlayer player) {
		String string = serialize(component);
		component = replace(component, string, Keys.PLAYER, player.name());
		component = replace(component, string, Keys.DISPLAY_NAME, player.displayName().get());
		component = replace(component, string, Keys.WORLD, player.world().key().value());
		component = replace(component, string, Keys.PLAYER_UUID, player.uniqueId().toString());
		component = replace(component, string, Keys.PING, String.valueOf(player.connection().latency()));
		component = replace(component, string, Keys.PLAYER_LEVEL, String.valueOf(player.experienceLevel().get()));
		component = replace(component, string, Keys.TPS, String.valueOf(Sponge.server().ticksPerSecond()));
		component = replace(component, string, Keys.TIME, player.world().properties().dayTime().hour() + ":" + player.world().properties().dayTime().minute());
		component = replace(component, string, Keys.ONLINE_PLAYERS, String.valueOf(Sponge.server().onlinePlayers().size()));
		component = replace(component, string, Keys.STAFFS_ONLINE, String.valueOf(Sponge.server().onlinePlayers().stream().filter(player2 -> (player2.hasPermission("tablistboard.staff"))).count()));
		component = replace(component, string, Keys.RANK, getOptionValue(player, "rank"));
		component = replace(component, string, Keys.PREFIX, getOptionValue(player, "prefix"));
		component = replace(component, string, Keys.SUFFIX, getOptionValue(player, "suffix"));
		component = replaceStatsPlaceholders(component, string, player);
		if(economyIsPresent()) {
			if(string.contains("%currency:")) {
				String currencyKey = getCurrencyKey(string);
				Currency currency = getCurrency(currencyKey);
				component = replace(component, string, Keys.currency(currencyKey), getCurrencySymbol(currency));
				component = replace(component, string, Keys.BALANCE, getBalance(player, currency));
			} else {
				component = replace(component, string, Keys.BALANCE, getBalance(player, getDefaultCurrency()));
			}
		}
		return component;
	}

	public static Component replacePlaceholders(Component component, ServerPlayer player, Region region, SimpleDateFormat format) {
		component = replacePlaceholders(component, player);
		String string = serialize(component);
		component = replace(component, string, Keys.REGION, tryDeserialize(!region.isGlobal() && region.getName(player.locale()).isPresent() ? region.getName(player.locale()).get() : region.getOwnerName()));
		component = replace(component, string, Keys.TYPE, region.getType().toString());
		component = replace(component, string, Keys.OWNER, region.getOwnerName());
		component = replace(component, string, Keys.MIN, !region.isGlobal() ? region.getCuboid().getMin().toString() : player.world().min().toInt().toString());
		component = replace(component, string, Keys.MAX, !region.isGlobal() ? region.getCuboid().getMax().toString() : player.world().max().toInt().toString());
		component = replace(component, string, Keys.REGION_SIZE, !region.isGlobal() ? region.getCuboid().getSizeXYZ().toInt().toString() : player.world().size().toInt().toString());
		component = replace(component, string, Keys.DATE, getDateCreated(player, region.getCreationTime(), format));
		component = replace(component, string, Keys.MEMBERS_SIZE, String.valueOf(region.getTotalMembers()));
		component = replace(component, string, Keys.REGION_ROLE, region.getMemberData(player).isPresent() ? region.getMemberData(player).get().getTrustType().toString() : "-");
		component = replace(component, string, Keys.CLAIMS_CREATED, String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getClaimedRegions(player)));
		component = replace(component, string, Keys.CLAIMS_LIMIT, String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitClaims(player)));
		component = replace(component, string, Keys.BLOCKS_CLAIMED, String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getClaimedBlocks(player)));
		component = replace(component, string, Keys.BLOCKS_LIMIT, String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitBlocks(player)));
		component = replace(component, string, Keys.SUBDIVISIONS_LIMIT, String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitSubdivisions(player)));
		component = replace(component, string, Keys.MEMBERS_LIMIT, String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitMembers(player)));
		component = replace(component, string, Keys.MAX_CLAIMS_LIMIT, String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitMaxClaims(player)));
		component = replace(component, string, Keys.MAX_BLOCKS_LIMIT, String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitMaxBlocks(player)));
		component = replace(component, string, Keys.MAX_SUBDIVISIONS_LIMIT, String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitMaxSubdivisions(player)));
		component = replace(component, string, Keys.MAX_MEMBERS_LIMIT, String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitMaxMembers(player)));
		return component;
	}

	private static Component replaceStatsPlaceholders(Component component, String plain, ServerPlayer player) {
		if(plain.contains("%statistic:")) {
			String statisticKey = getStatisticKey(plain);
			Optional<Statistic> optStatistic = getStatistic(player, statisticKey);
			if(optStatistic.isPresent()) {
				Statistic statistic = optStatistic.get();
				component = replace(component, plain, Keys.statisticKey(statisticKey), String.valueOf(player.get(org.spongepowered.api.data.Keys.STATISTICS).get().get(statistic)));
			}
		}
		return component;
	}

	private static Component replace(Component component, String plain, String placeholder, String value) {
		return plain.contains(placeholder) ? component.replaceText(TextReplacementConfig.builder().match(placeholder).replacement(value).build()) : component;
	}

	private static Component replace(Component component, String plain, String placeholder, Component value) {
		return plain.contains(placeholder) ? component.replaceText(TextReplacementConfig.builder().match(placeholder).replacement(value).build()) : component;
	}

	private static Component getOptionValue(ServerPlayer player, String option) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(player.option(option).orElse(""));
	}

	private static String getDateCreated(ServerPlayer player, long time, SimpleDateFormat dateFormat) {
		Calendar calendar = Calendar.getInstance(player.locale());
		calendar.setTimeInMillis(time);
		return dateFormat.format(calendar.getTime());
	}

	private static Optional<Statistic> getStatistic(ServerPlayer player, String key) {
		return player.statistics().keySet().stream().filter(statistic -> (statistic.toString().contains(key.replace(':', '.')))).findFirst();
	}

	private static String serialize(Component component) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(component);
	}

	private static Component tryDeserialize(String string) {
		try {
			return GsonComponentSerializer.gson().deserialize(string);
		} catch (Exception e) {
			return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
		}
	}

	private static String getStatisticKey(String string) {
		return string.split("%statistic:")[1].split("%")[0];
	}

	private static String getCurrencyKey(String string) {
		return string.split("%currency:")[1].split("%")[0];
	}

	private static String getBalance(ServerPlayer player, Currency currency) {
        try {
            Optional<UniqueAccount> uOpt = Sponge.server().serviceProvider().economyService().get().findOrCreateAccount(player.uniqueId());
            if (uOpt.isPresent()) {
                return uOpt.get().balance(currency).setScale(2).toPlainString();
            }
        } catch (Exception ignored) {
        }
		return "";
	}

	private static Currency getDefaultCurrency() {
		return Sponge.server().serviceProvider().economyService().get().defaultCurrency();
	}

	private static String getCurrencySymbol(Currency currency) {
		return serialize(currency.symbol());
	}

	private static Currency getCurrency(String string) {
		if(string == null) return getDefaultCurrency();
		Optional<Currency> optCurrency = getCurrencies().stream().filter(currency -> (serialize(currency.displayName()).equalsIgnoreCase(string) || serialize(currency.symbol()).equalsIgnoreCase(string))).findFirst();
		return optCurrency.isPresent() ? optCurrency.get() : getDefaultCurrency();
	}

	public static List<Currency> getCurrencies() {
		List<Currency> currencies = new ArrayList<Currency>();
		Sponge.game().findRegistry(RegistryTypes.CURRENCY).ifPresent(registry -> {
			if(registry.stream().count() > 0) currencies.addAll(registry.stream().collect(Collectors.toList()));
		});
		return !currencies.isEmpty() ? currencies : Arrays.asList(getDefaultCurrency());
	}

	private static boolean economyIsPresent() {
		return Sponge.server().serviceProvider().economyService().isPresent();
	}

}
