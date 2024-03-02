package sawfowl.tablistboard.utils;

public class PlaceholderKeys {

	public static final String PLAYER_LEVEL = "%player-level%";

	public static final String SERVER_TPS = "%server-tps%";

	public static final String WORLD_TIME = "%world-time%";

	public static final String ONLINE_PLAYERS = "%online-players%";

	public static final String STAFFS_ONLINE = "%staffs-online%";

	public static final String PLAYER_BALANCE = "%player-balance%";


	//RegionGuard placeholders:
	public static final String REGION_TYPE = "%regionguard:region-type%";

	public static final String REGION_NAME = "%regionguard:region-name%";

	public static final String REGION_OWNER = "%regionguard:region-owner%";

	public static final String REGION_MIN = "%regionguard:region-min%";

	public static final String REGION_MAX = "%regionguard:region-max%";

	public static final String REGION_SIZE = "%regionguard:region-size%";

	public static final String REGION_MEMBERS_SIZE = "%regionguard:region-members-size%";

	public static final String REGION_TRUST_LEVEL = "%regionguard:region-trust-level%";

	public static final String REGION_DATE = "%regionguard:region-created%";

	public static final String REGIONGUARD_CLAIMS_CREATED = "%regionguard:claims-created%";

	public static final String REGIONGUARD_CLAIMS_LIMIT = "%regionguard:claims-limit%";

	public static final String REGIONGUARD_BLOCKS_CLAIMED = "%regionguard:blocks-claimed%";

	public static final String REGIONGUARD_BLOCKS_LIMIT = "%regionguard:blocks-limit%";

	public static final String REGIONGUARD_SUBDIVISIONS_LIMIT = "%regionguard:subdivision-limit%";

	public static final String REGIONGUARD_MEMBERS_LIMIT = "%regionguard:members-limit%";

	public static final String REGIONGUARD_MAX_CLAIMS_LIMIT = "%regionguard:max-claims-limit%";

	public static final String REGIONGUARD_MAX_BLOCKS_LIMIT = "%regionguard:max-blocks-limit%";

	public static final String REGIONGUARD_MAX_SUBDIVISIONS_LIMIT = "%regionguard:max-subdivisions-limit%";

	public static final String MAX_MEMBERS_LIMIT = "%regionguard:max-members-limit%";
	
	public static String statisticKey(String statistic) {
		return "%statistic:" + statistic + "%";
	}
	
	public static String currency(String string) {
		return "%currency:" + string + "%";
	}

}
