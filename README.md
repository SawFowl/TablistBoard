# TablistBoard
Scoreboard and Tablist for Sponge on version 1.16.5.
####  ***[LocaleAPI](https://ore.spongepowered.org/Semenkovsky_Ivan/LocaleAPI) plugin is required.***
All text is specified in the locale files.

Reload command - `/tbreload`. Permission - `tablistboard.reload`
##### Placeholders:
`%player%` - player name \
`%display_name%` - player custom name \
`%ping%` - player ping \
`%player-uuid%` - player UUID \
`%player-level%` - player level \
`%tps%` - server tps \
`%time%`- minecraft world time(hour:minute) \
`%online-players%`- current online all players \
`%staffs-online%`- current online staffs players(staff permission - `tablistboard.staff`) \
`%world%` - player world \
`%rank%` - player rank(metaperm) \
`%prefix%` - player prefix(metaperm) \
`%suffix%` - player suffix(metaperm) \
`%balance%` - player balance \
`%currency:currency_name%` - the displayed currency. Affects the %balance% placeholder \
`%statistic:statistic_name%` - player stat
##### RegionGuard Placeholders(optional):
`%region%` - current player region \
`%type%` - region type \
`%owner%` - region owner \
`%region-role%` - the type of trust a player has in the region \
`%min%` - region's minimum position \
`%max%` - region's maximum position \
`%region-size%` - region size \
`%members-size%` - members in region \
`%date%` - region creation date \
`%claims_created%` - the number of regions created by the player \
`%claims_limit%` - player's regions limit \
`%blocks_claimed%` - the number of blocks claimed by the player \
`%blocks_limit%` - player's blocks limit \
`%subdivisions_limit%` - player's subdivisions limit \
`%members_limit%` - the limit of participants per region per player \
`%max_claims_limit%` - the maximum limit of regions a player has (using the economy) \
`%max_blocks_limit%` - the maximum limit of blocks a player has (using the economy) \
`%max_subdivisions_limit%` - the maximum limit of subdivisions a player has (using the economy) \
`%max_members_limit%` - the maximum limit of members a player has (using the economy)


**For developers:** \
**Use events:**
```java
@Plugin("pluginid")
public class Main {

    @Listener
    public void onSetTablistEvent(SetTablistEvent event) {
      //Your code
    }

    @Listener
    public void onSetEntry(SetTablistEvent.SetEntry event) {
      //Your code
    }

    @Listener
    public void onSetScoreboardEvent(SetScoreboardEvent event) {
      //Your code
    }

}
```
**Gradle:**
```gradle
repositories {
	...
	maven { 
		name = "JitPack"
		url 'https://jitpack.io' 
	}
}
dependencies {
	...
	implementation 'com.github.SawFowl:TablistBoard:1.1'
}
```
