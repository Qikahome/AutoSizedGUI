package qikahome.autosizedgui;

import net.neoforged.neoforge.common.ModConfigSpec;
import qikahome.autosizedgui.api.OverflowMode;

public class ModConfig {

    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_SCROLL_PAGINATION;
    public static final ModConfigSpec.IntValue DEFAULT_MAX_HEIGHT;
    public static final ModConfigSpec.IntValue DEFAULT_MAX_WIDTH;
    public static final ModConfigSpec.IntValue DEFAULT_MIN_MARGIN;
    public static final ModConfigSpec.IntValue DEFAULT_MIN_COLUMNS;
    public static final ModConfigSpec.IntValue DEFAULT_MAX_COLUMNS;
    public static final ModConfigSpec.IntValue DEFAULT_MAX_ROWS;
    public static final ModConfigSpec.EnumValue<OverflowMode> DEFAULT_OVERFLOW_MODE;
    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("AutoSizedGUI client settings").push("general");

        ENABLE_SCROLL_PAGINATION = builder
                .comment("Allow scrolling the mouse wheel to switch pages")
                .define("enableScrollPagination", true);

        DEFAULT_MAX_HEIGHT = builder
                .comment("Max GUI height in pixels (0 for no limit)")
                .defineInRange("defaultMaxHeight", 0, 0, Integer.MAX_VALUE);

        DEFAULT_MAX_WIDTH = builder
                .comment("Max GUI width in pixels (0 for no limit)")
                .defineInRange("defaultMaxWidth", 332, 0, Integer.MAX_VALUE);

        DEFAULT_MIN_MARGIN = builder
                .comment("Minimum margin from screen edges in pixels")
                .defineInRange("defaultMinMargin", 15, 0, Integer.MAX_VALUE);

        DEFAULT_MIN_COLUMNS = builder
                .comment("Minimum number of columns in the GUI")
                .defineInRange("defaultMinColumns", 9, 1, Integer.MAX_VALUE);

        DEFAULT_MAX_COLUMNS = builder
                .comment("Maximum number of columns (-1 for auto)")
                .defineInRange("defaultMaxColumns", -1, -1, Integer.MAX_VALUE);

        DEFAULT_MAX_ROWS = builder
                .comment("Maximum number of rows (-1 for auto)")
                .defineInRange("defaultMaxRows", -1, -1, Integer.MAX_VALUE);

        DEFAULT_OVERFLOW_MODE = builder
                .comment("Default overflow mode for the GUI")
                .defineEnum("defaultOverflowMode", OverflowMode.SCROLL);

        builder.pop();
        CLIENT_SPEC = builder.build();
    }
}
