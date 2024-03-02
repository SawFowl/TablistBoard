package sawfowl.tablistboard.configure;

import java.text.SimpleDateFormat;
import java.util.List;

import net.kyori.adventure.text.Component;
import sawfowl.localeapi.api.LocaleReference;

public interface AbstractLocale extends LocaleReference{

	List<Tablist> getTablists();

	List<Scoreboard> getScoreboards();

	Component getReload();

	SimpleDateFormat getDateTimeFormat();

}
