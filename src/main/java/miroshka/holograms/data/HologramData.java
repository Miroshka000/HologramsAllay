package miroshka.holograms.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HologramData {

    private String id;
    private String world;
    private double x;
    private double y;
    private double z;
    @Builder.Default
    private List<String> lines = new ArrayList<>();
    @Builder.Default
    private Map<String, List<String>> localizedLines = new HashMap<>();
    @Builder.Default
    private int updateInterval = -1;
    @Builder.Default
    private boolean multiLine = true;
    @Builder.Default
    private double lineSpacing = 0.25;

    public List<String> getLinesForLocale(String locale) {
        if (localizedLines.containsKey(locale)) {
            return localizedLines.get(locale);
        }
        return lines;
    }

    public void setLinesForLocale(String locale, List<String> localeLines) {
        localizedLines.put(locale, localeLines);
    }

    public void removeLinesForLocale(String locale) {
        localizedLines.remove(locale);
    }

    public boolean hasLocalizedLines(String locale) {
        return localizedLines.containsKey(locale);
    }

    public List<String> getAvailableLocales() {
        return new ArrayList<>(localizedLines.keySet());
    }
}
