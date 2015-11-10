/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.ZopimChatPlugin.model;

import android.graphics.Color;

public class ColorSkin {

    private static final String IS_LIGHT_VALUE_LIGHT = "1";
    private static final String RGBA = "rgba(";

    private final int color1;
    private final int color2;
    private final int color3;
    private final int color4;
    private final int color5;
    private final int color6;
    private final int color7;
    private final int color8;

    private final boolean light;

    private ColorSkin(Builder builder) {
        color1 = builder.color1;
        color2 = builder.color2;
        color3 = builder.color3;
        color4 = builder.color4;
        color5 = builder.color5;
        color6 = builder.color6;
        color7 = builder.color7;
        color8 = builder.color8;

        light = builder.light;
    }

    public int getColor1() {
        return color1;
    }

    public int getColor2() {
        return color2;
    }

    public int getColor3() {
        return color3;
    }

    public int getColor4() {
        return color4;
    }

    public int getColor5() {
        return color5;
    }

    public int getColor6() {
        return color6;
    }

    public int getColor7() {
        return color7;
    }

    public int getColor8() {
        return color8;
    }

    public boolean isLight() {
        return light;
    }

    public static class Builder {

        private int color1;
        private int color2;
        private int color3;
        private int color4;
        private int color5;
        private int color6;
        private int color7;
        private int color8;

        private boolean light;

        public Builder setColor1(String color1) {
            this.color1 = normalizeColor(color1);

            return this;
        }

        public Builder setColor2(String color2) {
            this.color2 = normalizeColor(color2);

            return this;
        }

        public Builder setColor3(String color3) {
            this.color3 = normalizeColor(color3);

            return this;
        }

        public Builder setColor4(String color4) {
            this.color4 = normalizeColor(color4);

            return this;
        }

        public Builder setColor5(String color5) {
            this.color5 = normalizeColor(color5);

            return this;
        }

        public Builder setColor6(String color6) {
            this.color6 = normalizeColor(color6);

            return this;
        }

        public Builder setColor7(String color7) {
            this.color7 = normalizeColor(color7);

            return this;
        }

        public Builder setColor8(String color8) {
            this.color8 = normalizeColor(color8);

            return this;
        }

        public Builder setLight(String light) {
            this.light = IS_LIGHT_VALUE_LIGHT.equals(light);

            return this;
        }

        public ColorSkin build() {
            return new ColorSkin(this);
        }

        private static int normalizeColor(String value) {
            int result;

            try {
                if (value.contains(RGBA)) {
                    String[] params = value.replaceAll("\\s", "").substring(value.indexOf("(") + 1, value.indexOf(")")).split(",");
                    result = Color.argb(
                            Float.valueOf(Float.valueOf(params[3]) * 100).intValue(),
                            Integer.valueOf(params[0]),
                            Integer.valueOf(params[1]),
                            Integer.valueOf(params[2])
                    );
                } else
                    result = Color.parseColor(value);
            } catch(Exception exception) {
                exception.printStackTrace();
                result = Color.BLACK;
            }

            return result;
        }

    }

}
