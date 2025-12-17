package miroshka.holograms.util;

import lombok.experimental.UtilityClass;
import org.allaymc.api.message.I18n;
import org.allaymc.api.message.LangCode;
import org.allaymc.api.player.Player;

@UtilityClass
public class MessageUtils {

    private static final String PREFIX = "holograms:";

    public static String tr(Player player, String key, Object... args) {
        LangCode langCode = player.getLoginData().getLangCode();
        return I18n.get().tr(langCode, PREFIX + key, args);
    }

    public static void send(Player player, String key, Object... args) {
        if (player == null) {
            return;
        }
        player.sendMessage(tr(player, key, args));
    }
}
