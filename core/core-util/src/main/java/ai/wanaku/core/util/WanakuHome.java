package ai.wanaku.core.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

/**
 * Centralized resolution of the Wanaku home directory.
 * <p>
 * The wanaku home directory is resolved in the following order:
 * <ol>
 *   <li>System property {@code wanaku.home}</li>
 *   <li>Environment variable {@code WANAKU_HOME}</li>
 *   <li>Default: {@code ${user.home}/.wanaku}</li>
 * </ol>
 */
public class WanakuHome {

    private static final String WANAKU_HOME_SYS_PROP = "wanaku.home";
    private static final String WANAKU_HOME_ENV_VAR = "WANAKU_HOME";

    private WanakuHome() {}

    /**
     * Returns the default Wanaku home directory path.
     * <p>
     * This is a method (not a static field) to avoid GraalVM native image
     * build-time evaluation which would capture the build machine's home directory.
     *
     * @return the default path to the Wanaku home directory
     */
    private static String defaultWanakuHome() {
        return Paths.get(System.getProperty("user.home"), ".wanaku").toString();
    }

    /**
     * Returns the resolved Wanaku home directory path.
     *
     * @return the path to the Wanaku home directory
     */
    public static String get() {
        String home = System.getProperty(WANAKU_HOME_SYS_PROP);
        if (home != null && !home.isBlank()) {
            return home;
        }

        home = System.getenv(WANAKU_HOME_ENV_VAR);
        if (home != null && !home.isBlank()) {
            return home;
        }

        return defaultWanakuHome();
    }

    /**
     * Expands placeholders in a path string.
     * <p>
     * Currently supported placeholders:
     * <ul>
     * <li>{@code ${wanaku.home}} - expands to the Wanaku home directory</li>
     * <li>{@code ${user.home}} - expands to the user's home directory</li>
     * </ul>
     * <p>
     * Uses {@link Path#normalize()} to avoid doubled separators when placeholders
     * resolve to paths whose trailing separator meets a slash between placeholders.
     *
     * @param path the path string potentially containing placeholders
     * @return the path with placeholders expanded
     */
    public static String expandPlaceholders(String path) {
        if (path == null) {
            return null;
        }
        return expandPlaceholders(path, WanakuHome::get, () -> System.getProperty("user.home"));
    }

    static String expandPlaceholders(String path, Supplier<String> wanakuHome, Supplier<String> userHome) {
        if (path == null) {
            return null;
        }
        String result = path.replace("${wanaku.home}", wanakuHome.get()).replace("${user.home}", userHome.get());
        return Paths.get(result).normalize().toString();
    }
}
