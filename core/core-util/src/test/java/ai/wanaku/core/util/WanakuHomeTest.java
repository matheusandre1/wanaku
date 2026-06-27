package ai.wanaku.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class WanakuHomeTest {

    @Test
    void expandPlaceholders_expandsWanakuHome() {
        String result = WanakuHome.expandPlaceholders("${wanaku.home}/data", () -> "/custom/wanaku", () -> "");
        assertNotNull(result);
        assertEquals("/custom/wanaku/data", result);
    }

    @Test
    void expandPlaceholders_expandsUserHome() {
        String result = WanakuHome.expandPlaceholders("${user.home}/data", () -> "", () -> "/test/user/home");
        assertEquals("/test/user/home/data", result);
    }

    @Test
    void expandPlaceholders_expandsBoth() {
        String result = WanakuHome.expandPlaceholders(
                "${wanaku.home}/${user.home}/data", () -> "/custom/wanaku", () -> "/test/user/home");
        assertEquals("/custom/wanaku/test/user/home/data", result);
    }

    @Test
    void expandPlaceholders_returnsOriginalWhenNoPlaceholders() {
        String result = WanakuHome.expandPlaceholders("/plain/path", () -> "/ignored", () -> "/ignored");
        assertEquals("/plain/path", result);
    }

    @Test
    void expandPlaceholders_returnsNullForNull() {
        String result = WanakuHome.expandPlaceholders(null, () -> "/x", () -> "/y");
        assertNull(result);
    }

    @Test
    void expandPlaceholders_handlesEmptyString() {
        String result = WanakuHome.expandPlaceholders("", () -> "/x", () -> "/y");
        assertEquals("", result);
    }
}
