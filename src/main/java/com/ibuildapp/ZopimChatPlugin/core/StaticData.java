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

import com.ibuildapp.ZopimChatPlugin.model.ColorSkin;

public class StaticData {

    private static ColorSkin colorSkin;
    private static String zopimKey;

    public static void setColorSkin(ColorSkin colorSkin) {
        StaticData.colorSkin = colorSkin;
    }

    public static ColorSkin getColorSkin() {
        return colorSkin;
    }

    public static void setZopimKey(String zopimKey) {
        StaticData.zopimKey = zopimKey;
    }

    public static String getZopimKey() {
        return zopimKey;
    }
}
