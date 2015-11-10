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
package com.ibuildapp.ZopimChatPlugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appbuilder.sdk.android.Widget;
import com.ibuildapp.ZopimChatPlugin.core.Core;
import com.ibuildapp.ZopimChatPlugin.core.ParserXml;
import com.ibuildapp.ZopimChatPlugin.core.StaticData;

public class ZopimChatPlugin extends Activity {

    public static final int REQUEST_CHAT = 102310;

    public static final String EXTRA_NAME = "EXTRA_NAME";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_WIDGET = "Widget";

    private static final int COLOR_BLACK_40 = Color.parseColor("#66000000");
    private static final int COLOR_RED_40 = Color.parseColor("#66ff0000");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zopim_chat_plugin);

        final Widget widget = (Widget)getIntent().getSerializableExtra(EXTRA_WIDGET);

        if(widget == null) {
            handleErrorInit();

            return;
        }

        final String xml = widget.getPluginXmlData();

        if(TextUtils.isEmpty(xml)) {
            handleErrorInit();

            return;
        }

        Core.INSTANCE.addTask(new Runnable() {
            @Override
            public void run() {
                ParserXml.Result result = ParserXml.parse(xml);

                StaticData.setZopimKey(result.getZopimKey());
                StaticData.setColorSkin(result.getColorSkin());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            findViewById(R.id.content_view).setBackgroundColor(StaticData.getColorSkin().getColor1());
                            setNavbarBackgroundColor(StaticData.getColorSkin().getColor1(), findViewById(R.id.navbar));
                            findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    finish();
                                }
                            });

                            String name = getName();
                            final String title = TextUtils.isEmpty(widget.getTitle()) ? getString(R.string.zopim_chat_plugin_chat) : widget.getTitle();

                            final EditText enter_your_name_field = (EditText) findViewById(R.id.enter_your_name_field);
                            enter_your_name_field.setText(name);
                            enter_your_name_field.setSelection(name.length());
                            enter_your_name_field.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    enter_your_name_field.setHintTextColor(COLOR_BLACK_40);
                                }

                                @Override
                                public void afterTextChanged(Editable s) {

                                }
                            });

                            ((TextView) findViewById(R.id.title)).setText(title);
                            ((TextView) findViewById(R.id.lets_start)).setTextColor(StaticData.getColorSkin().getColor3());
                            ((TextView) findViewById(R.id.enter_your_name)).setTextColor(StaticData.getColorSkin().getColor3());

                            TextView start_chat = ((TextView) findViewById(R.id.start_chat));
                            start_chat.setTextColor(StaticData.getColorSkin().getColor1());
                            start_chat.getBackground().setColorFilter(StaticData.getColorSkin().getColor5(), PorterDuff.Mode.SRC);
                            start_chat.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (TextUtils.isEmpty(enter_your_name_field.getText()))
                                        enter_your_name_field.setHintTextColor(COLOR_RED_40);
                                    else {
                                        hideKeyboard(enter_your_name_field);
                                        startActivityForResult(
                                                new Intent(ZopimChatPlugin.this, ZopimChat.class)
                                                        .putExtra(EXTRA_NAME, enter_your_name_field.getText().toString())
                                                        .putExtra(EXTRA_TITLE, title)
                                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), REQUEST_CHAT
                                        );
                                    }
                                }
                            });

                            if(!TextUtils.isEmpty(name))
                                startActivityForResult(
                                        new Intent(ZopimChatPlugin.this, ZopimChat.class)
                                                .putExtra(EXTRA_NAME, name)
                                                .putExtra(EXTRA_TITLE, title)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), REQUEST_CHAT
                                );
                        } catch (Throwable error) {
                            error.printStackTrace();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    handleErrorInit();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public void setNavbarBackgroundColor(final int color, View view) {
        float density = getResources().getDisplayMetrics().density;

        Drawable background = new LayerDrawable(new Drawable[] {
                new ColorDrawable(color),
                new ColorDrawable(color == Color.WHITE ? Color.parseColor("#33000000") : Color.parseColor("#66FFFFFF"))
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            view.setBackground(background);
        else
            view.setBackgroundDrawable(background);

        view.setPadding((int) (density * 10), (int) (density * 10), (int) (density * 10), (int) (density * 10));
    }

    private void hideKeyboard(View view) {
        if(view != null) {
            view.clearFocus();
            ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void saveName() {
        getSharedPreferences(ZopimChatPlugin.class.getName(), MODE_PRIVATE).edit().putString(EXTRA_NAME, ((EditText)findViewById(R.id.enter_your_name_field)).getText().toString()).apply();
    }

    private String getName() {
        return getSharedPreferences(ZopimChatPlugin.class.getName(), MODE_PRIVATE).getString(EXTRA_NAME, "");
    }

    private void removeName() {
        getSharedPreferences(ZopimChatPlugin.class.getName(), MODE_PRIVATE).edit().remove(EXTRA_NAME).apply();
    }

    private void handleErrorInit() {
        Toast.makeText(this, getResources().getString(R.string.zopim_chat_plugin_error_init), Toast.LENGTH_LONG).show();

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CHAT) {
            if(resultCode == RESULT_OK) {
                saveName();
                finish();
            } else
                removeName();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
