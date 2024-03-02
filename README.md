# TablistBoard
Scoreboard and Tablist for Sponge on version 1.16.5+.
####  ***[LocaleAPI](https://ore.spongepowered.org/Semenkovsky-Ivan/LocaleAPI) plugin is required.***
All text is specified in the locale files.

Reload command - `/tbreload`. Permission - `tablistboard.reload`


##### Placeholders:
`%name%` - player name \
`%entity-display-name%` - player custom name \
`%player-ping%` - player ping \
`%entity-uuid%` - player UUID \
`%player-level%` - player level \
`%world%` - player world \
`%location%` - player location on server \
`%position%` - player double position \
`%block-position%` - player integer position \
`%server-tps%` - server tps \
`%time%`- minecraft world time(hour:minute) \
`%online-players%`- current online all players \
`%staffs-online%`- current online staffs players(staff permission - `tablistboard.staff`) \
`%player-rank%` - player rank(metaperm) \
`%player-prefix%` - player prefix(metaperm) \
`%player-suffix%` - player suffix(metaperm) \
`%player-balance%` - player balance \
`%currency:currency_name_or_id%` - the displayed currency. Affects the %player-balance% placeholder \
`%statistic:statistic_name_or_id%` - player stat
##### RegionGuard Placeholders(optional):
`%regionguard:region-name%` - current player region \
`%regionguard:region-type%` - region type \
`%regionguard:region-owner%` - region owner \
`%regionguard:region-trust-level%` - the type of trust a player has in the region \
`%regionguard:region-min%` - region's minimum position \
`%regionguard:region-max%` - region's maximum position \
`%regionguard:region-size%` - region size \
`%regionguard:region-members-size%` - members in region \
`%regionguard:region-created%` - region creation date \
`%regionguard:claims-created%` - the number of regions created by the player \
`%regionguard:claims-limit%` - player's regions limit \
`%regionguard:blocks-claimed%` - the number of blocks claimed by the player \
`%regionguard:blocks-limit%` - player's blocks limit \
`%regionguard:subdivisions-limit%` - player's subdivisions limit \
`%regionguard:members-limit%` - the limit of participants per region per player \
`%regionguard:max-claims-limit%` - the maximum limit of regions a player has (using the economy) \
`%regionguard:max-blocks-limit%` - the maximum limit of blocks a player has (using the economy) \
`%regionguard:max-subdivisions-limit%` - the maximum limit of subdivisions a player has (using the economy) \
`%regionguard:max-members-limit%` - the maximum limit of members a player has (using the economy) \

Placeholders can be used by other plugins without depending on this plugin, as long as they use the placeholder API from the LocaleAPI plugin.
