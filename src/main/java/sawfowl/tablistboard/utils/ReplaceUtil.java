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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.regionguard.api.data.Region;
import sawfowl.tablistboard.TablistBoard;

public class ReplaceUtil {

	public static class Keys {

		public static final String PLAYER = "%player%";

		public static final String RANK = "%rank%";

		public static final String PREFIX = "%prefix%";

		public static final String SUFFIX = "%suffix%";

		public static final String WORLD = "%world%";

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
		if(string.contains(Keys.PLAYER)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.PLAYER).replacement(player.name()).build());
		if(string.contains(Keys.RANK)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.RANK).replacement(getOptionValue(player, "rank")).build());
		if(string.contains(Keys.PREFIX)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.PREFIX).replacement(getOptionValue(player, "prefix")).build());
		if(string.contains(Keys.SUFFIX)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.SUFFIX).replacement(getOptionValue(player, "suffix")).build());
		if(string.contains(Keys.WORLD)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.WORLD).replacement(player.world().key().value()).build());
		component = replaceStatsPlaceholders(component, string, player);
		if(economyIsPresent()) {
			if(string.contains("%currency:")) {
				String currencyKey = getCurrencyKey(string);
				Currency currency = getCurrency(currencyKey);
				component = component.replaceText(TextReplacementConfig.builder().match(Keys.currency(currencyKey)).replacement(getCurrencySymbol(currency)).build());
				component = component.replaceText(TextReplacementConfig.builder().match(Keys.BALANCE).replacement(getBalance(player, currency)).build());
			} else {
				if(string.contains(Keys.BALANCE)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.BALANCE).replacement(getBalance(player, getDefaultCurrency())).build());
			}
		}
		return component;
	}

	public static Component replacePlaceholders(Component component, ServerPlayer player, Region region, SimpleDateFormat format) {
		component = replacePlaceholders(component, player);
		String string = serialize(component);
		if(string.contains(Keys.REGION) && region.getName(player.locale()).isPresent()) component = component.replaceText(TextReplacementConfig.builder().match(Keys.REGION).replacement(player.world().key().value()).build());
		if(string.contains(Keys.TYPE)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.TYPE).replacement(region.getType().toString()).build());
		if(string.contains(Keys.OWNER)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.OWNER).replacement(region.getOwnerName()).build());
		if(string.contains(Keys.MIN)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.MIN).replacement(region.getCuboid().getMin().toString()).build());
		if(string.contains(Keys.MAX)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.MAX).replacement(region.getCuboid().getMax().toString()).build());
		if(string.contains(Keys.REGION_SIZE)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.REGION_SIZE).replacement(String.valueOf(region.getCuboid().getSize())).build());
		if(string.contains(Keys.DATE)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.DATE).replacement(region.isGlobal() || region.isAdmin() ? "0" : getDateCreated(player, region.getCreationTime(), format)).build());
		if(string.contains(Keys.MEMBERS_SIZE)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.MEMBERS_SIZE).replacement(String.valueOf(region.getTotalMembers())).build());
		if(string.contains(Keys.REGION_ROLE)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.REGION_ROLE).replacement(region.getMemberData(player).isPresent() ? region.getMemberData(player).get().getTrustType().toString() : "-").build());
		if(string.contains(Keys.CLAIMS_CREATED)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.CLAIMS_CREATED).replacement(String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getClaimedRegions(player))).build());
		if(string.contains(Keys.CLAIMS_LIMIT)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.CLAIMS_LIMIT).replacement(String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitClaims(player))).build());
		if(string.contains(Keys.BLOCKS_CLAIMED)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.BLOCKS_CLAIMED).replacement(String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getClaimedBlocks(player))).build());
		if(string.contains(Keys.BLOCKS_LIMIT)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.BLOCKS_LIMIT).replacement(String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitBlocks(player))).build());
		if(string.contains(Keys.SUBDIVISIONS_LIMIT)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.SUBDIVISIONS_LIMIT).replacement(String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitSubdivisions(player))).build());
		if(string.contains(Keys.MEMBERS_LIMIT)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.MEMBERS_LIMIT).replacement(String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitMembers(player))).build());
		if(string.contains(Keys.MAX_CLAIMS_LIMIT)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.MAX_CLAIMS_LIMIT).replacement(String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitMaxClaims(player))).build());
		if(string.contains(Keys.MAX_BLOCKS_LIMIT)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.MAX_BLOCKS_LIMIT).replacement(String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitMaxBlocks(player))).build());
		if(string.contains(Keys.MAX_SUBDIVISIONS_LIMIT)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.MAX_SUBDIVISIONS_LIMIT).replacement(String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitMaxSubdivisions(player))).build());
		if(string.contains(Keys.MAX_MEMBERS_LIMIT)) component = component.replaceText(TextReplacementConfig.builder().match(Keys.MAX_MEMBERS_LIMIT).replacement(String.valueOf(TablistBoard.getInstance().getRegionUtil().getRegionAPI().getLimitMaxMembers(player))).build());
		return component;
	}

	private static Component replaceStatsPlaceholders(Component component, String plain, ServerPlayer player) {
		if(plain.contains("%statistic:")) {
			String statisticKey = getStatisticKey(plain);
			Optional<Statistic> optStatistic = getStatistic(player, statisticKey);
			if(optStatistic.isPresent()) {
				Statistic statistic = optStatistic.get();
				component = component.replaceText(TextReplacementConfig.builder().match(Keys.statisticKey(statisticKey)).replacement(String.valueOf(player.get(org.spongepowered.api.data.Keys.STATISTICS).get().get(statistic))).build());
			}
		}
		return component;
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
