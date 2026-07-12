// client/skin/SkinState.java
package com.xiaoshi2022.kamenriderbossyouandme.client.skin;

/**
 * 皮肤加载状态枚举
 */
public enum SkinState {
    NOT_LOADED(0, "未加载"),
    LOADING(1, "加载中"),
    LOADED(2, "已加载"),
    FAILED(3, "加载失败");

    private final int code;
    private final String description;

    SkinState(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SkinState fromCode(int code) {
        for (SkinState state : values()) {
            if (state.code == code) return state;
        }
        return NOT_LOADED;
    }
}