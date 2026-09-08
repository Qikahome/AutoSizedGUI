package qikahome.autosizedgui;

import qikahome.autosizedgui.api.OverflowMode;

/**
 * 布局引擎在运行时读取的配置值（纯 vanilla，无 loader 依赖）。
 * <p>
 * 各 loader 的 config（NeoForge ModConfigSpec / Fabric properties）在加载与重载时
 * 把当前值同步到本类字段，common 代码只与本类交互。默认值与各 loader config 默认一致。
 */
public final class AutoSizedGUISettings {

    /** 是否允许滚轮翻页 */
    public static volatile boolean enableScrollPagination = true;
    public static volatile int defaultMaxHeight = 0;
    public static volatile int defaultMaxWidth = 332;
    public static volatile int defaultMinMargin = 15;
    public static volatile int defaultMinColumns = 9;
    public static volatile int defaultMaxColumns = -1;
    public static volatile int defaultMaxRows = -1;
    public static volatile OverflowMode defaultOverflowMode = OverflowMode.SCROLL;
    /** 滚动条轨道按住后开始自动重复的初始延迟（ms） */
    public static volatile int scrollbarTrackRepeatDelay = 400;
    /** 按住滚动条轨道时的重复间隔（ms） */
    public static volatile int scrollbarTrackRepeatInterval = 80;

    private AutoSizedGUISettings() {
    }
}
