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
package com.ibuildapp.ZopimChatPlugin.core;

import android.graphics.Color;
import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;

import com.ibuildapp.ZopimChatPlugin.model.ColorSkin;

public class ParserXml {

    public static class Result {

        private final String moduleId;
        private final String zopimKey;
        private final ColorSkin colorSkin;

        private Result(Builder builder) {
            moduleId = builder.moduleId;
            zopimKey = builder.zopimKey;
            colorSkin = builder.colorSkin;
        }

        public String getModuleId() {
            return moduleId;
        }

        public String getZopimKey() {
            return zopimKey;
        }

        public ColorSkin getColorSkin() {
            return colorSkin;
        }

        public static class Builder {

            private String moduleId;
            private String zopimKey;
            private ColorSkin colorSkin;

            public Builder setModuleId(String moduleId) {
                this.moduleId = moduleId;

                return this;
            }

            public Builder setZopimKey(String zopimKey) {
                this.zopimKey = zopimKey;

                return this;
            }

            public Builder setColorSkin(ColorSkin colorSkin) {
                this.colorSkin = colorSkin;

                return this;
            }

            public Result build() {
                return new Result(this);
            }

        }

    }

    private static final String TAG = ParserXml.class.getCanonicalName();
    private static final String ERROR_XML_PARSING = "Xml parsing error";

    private static final String XML_DATA = "data";
    private static final String XML_MODULE_ID = "module_id";
    private static final String XML_ZOPIM_KEY = "zopim_key";
    private static final String XML_COLORSKIN = "colorskin";
    private static final String XML_COLORSKIN_COLOR_1 = "color1";
    private static final String XML_COLORSKIN_COLOR_2 = "color2";
    private static final String XML_COLORSKIN_COLOR_3 = "color3";
    private static final String XML_COLORSKIN_COLOR_4 = "color4";
    private static final String XML_COLORSKIN_COLOR_5 = "color5";
    private static final String XML_COLORSKIN_COLOR_6 = "color6";
    private static final String XML_COLORSKIN_COLOR_7 = "color7";
    private static final String XML_COLORSKIN_COLOR_8 = "color8";
    private static final String XML_COLORSKIN_IS_LIGHT = "isLight";

    public static Result parse(String xml) {
        final ColorSkin.Builder colorSkinBuilder = new ColorSkin.Builder();
        final Result.Builder resultBuilder = new Result.Builder();

        RootElement data = new RootElement(XML_DATA);
        Element colorskin = data.getChild(XML_COLORSKIN);

        data.getChild(XML_MODULE_ID).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                resultBuilder.setModuleId(body.trim());
            }
        });

        data.getChild(XML_ZOPIM_KEY).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                resultBuilder.setZopimKey(body.trim());
            }
        });

        colorskin.getChild(XML_COLORSKIN_COLOR_1).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                colorSkinBuilder.setColor1(body.trim());
            }
        });

        colorskin.getChild(XML_COLORSKIN_COLOR_2).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                colorSkinBuilder.setColor2(body.trim());
            }
        });

        colorskin.getChild(XML_COLORSKIN_COLOR_3).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                colorSkinBuilder.setColor3(body.trim());
            }
        });

        colorskin.getChild(XML_COLORSKIN_COLOR_4).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                colorSkinBuilder.setColor4(body.trim());
            }
        });

        colorskin.getChild(XML_COLORSKIN_COLOR_5).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                colorSkinBuilder.setColor5(body.trim());
            }
        });

        colorskin.getChild(XML_COLORSKIN_COLOR_6).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                colorSkinBuilder.setColor6(body.trim());
            }
        });

        colorskin.getChild(XML_COLORSKIN_COLOR_7).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                colorSkinBuilder.setColor7(body.trim());
            }
        });

        colorskin.getChild(XML_COLORSKIN_COLOR_8).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                colorSkinBuilder.setColor8(body.trim());
            }
        });

        colorskin.getChild(XML_COLORSKIN_IS_LIGHT).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                colorSkinBuilder.setLight(body.trim());
                resultBuilder.setColorSkin(colorSkinBuilder.build());
            }
        });

        try {
            Xml.parse(xml, data.getContentHandler());
        } catch(Exception exception) {
            Log.e(TAG, ERROR_XML_PARSING, exception);
        }

        return resultBuilder.build();
    }

}
