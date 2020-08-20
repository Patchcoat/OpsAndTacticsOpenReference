package com.metallicim.oatsopenref;

public class ParseTheme {
    public int parseThemeColor(String string) {
        switch (string) {
            case "dark":
                return R.style.AppTheme_Dark;
            case "low_contrast":
                return R.style.AppTheme_Low_contrast;
            case "low_contrast_dark":
                return R.style.AppTheme_Low_contrast_dark;
            case "solarized_light":
                return R.style.AppTheme_Solarized_light;
            case "solarized_dark":
                return R.style.AppTheme_Solarized_dark;
            case "terminal":
                return R.style.AppTheme_Terminal;
            default:
                return R.style.AppTheme_Standard;
        }
    }
}
