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

import com.ibuildapp.ZopimChatPlugin.core.StaticData;
import com.zopim.android.sdk.api.Chat;
import com.zopim.android.sdk.chatlog.ZopimChatLogFragment;
import com.zopim.android.sdk.embeddable.ChatActions;
import com.zopim.android.sdk.model.Account;
import com.zopim.android.sdk.model.VisitorInfo;
import com.zopim.android.sdk.prechat.ChatListener;
import com.zopim.android.sdk.prechat.ZopimChatFragment;
import com.zopim.android.sdk.store.Storage;
import com.zopim.android.sdk.widget.ChatWidgetService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ZopimChat extends AppCompatActivity implements ChatListener {

    private static String ACTION_TIMEOUT = "chat.action.TIMEOUT";
    private static String ACTION_INITIALIZATION_TIMEOUT = "chat.action.INITIALIZATION_TIMEOUT";

    private boolean forceResultCancelled;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(ACTION_TIMEOUT.equals(action) || ACTION_INITIALIZATION_TIMEOUT.equals(action))
                forceResultCancelled = true;
        }
    };

    private Toolbar toolbar;
    private TextView right_btn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(StaticData.getColorSkin().isLight() ? R.style.ZopimChatThemeLight : R.style.ZopimChatThemeDark);
        setContentView(R.layout.zopim_chat);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ((TextView)findViewById(R.id.title)).setText(getIntent().getStringExtra(ZopimChatPlugin.EXTRA_TITLE));

        right_btn = (TextView)findViewById(R.id.right_btn);

        setNavbarBackgroundColor(StaticData.getColorSkin().getColor1(), findViewById(R.id.navbar));
        (findViewById(R.id.chat_fragment_container)).setBackgroundColor(StaticData.getColorSkin().getColor1());

        setSupportActionBar(toolbar);

        String name = getIntent().getStringExtra(ZopimChatPlugin.EXTRA_NAME);

        Storage.init(this);

        if(Storage.visitorInfo().getVisitorInfo() != null && name.equals(Storage.visitorInfo().getVisitorInfo().getName()) &&
                (stopService(new Intent(this, ChatWidgetService.class)) || ChatActions.ACTION_RESUME_CHAT.equals(getIntent().getAction()))) {
            resumeChat();

            return;
        }

        try {
            newChat(name);
        } catch(Throwable error) {
            error.printStackTrace();

            handleErrorInit();
        }
    }

    private void resumeChat() {
        FragmentManager manager = getSupportFragmentManager();

        if (manager.findFragmentByTag(ZopimChatLogFragment.class.getName()) == null) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.chat_fragment_container, new ZopimChatLogFragment(), ZopimChatLogFragment.class.getName());
            transaction.commit();
        }

        overDrawRightButton();
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

    private Fragment fragmentZopimChatInit(String name) {
        Storage.INSTANCE.clearAll();
        com.zopim.android.sdk.api.ZopimChat.init(StaticData.getZopimKey());
        com.zopim.android.sdk.api.ZopimChat.setVisitorInfo(new VisitorInfo.Builder().name(name).build());

        return ZopimChatFragment.newInstance(new com.zopim.android.sdk.api.ZopimChat.SessionConfig());
    }

    private void newChat(String name) throws Throwable {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.chat_fragment_container, fragmentZopimChatInit(name), ZopimChatFragment.class.getName());
        transaction.commit();
    }

    private void overDrawRightButton() {
        Account account = com.zopim.android.sdk.api.ZopimChat.getDataSource().getAccount();

        if(account != null) {
            if(Account.Status.UNKNOWN == account.getStatus()) {
                right_btn.setVisibility(View.GONE);
                right_btn.setOnClickListener(null);
            } else if(Account.Status.ONLINE == account.getStatus()) {
                right_btn.setVisibility(View.VISIBLE);
                right_btn.setText(R.string.zopim_chat_plugin_exit);
                right_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view = ((ActionMenuView) toolbar.getChildAt(1)).getChildAt(0);

                        if (view != null)
                            view.performClick();
                    }
                });
            } else if(Account.Status.OFFLINE == account.getStatus()) {
                View no_agents_button = findViewById(R.id.no_agents_button);

                if(no_agents_button != null) {
                    no_agents_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendOrderedBroadcast(new Intent().setAction("zopim.action.CREATE_REQUEST"), null);

                            right_btn.setVisibility(View.VISIBLE);
                            right_btn.setText(R.string.zopim_chat_plugin_send);
                            right_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    View view = ((ActionMenuView) toolbar.getChildAt(1)).getChildAt(0);

                                    if (view != null)
                                        view.performClick();
                                }
                            });
                        }
                    });
                }
            } else {
                right_btn.setVisibility(View.GONE);
                right_btn.setOnClickListener(null);
            }
        } else {
            right_btn.setVisibility(View.GONE);
            right_btn.setOnClickListener(null);
        }
    }

    private void handleErrorInit() {
        Toast.makeText(this, getResources().getString(R.string.zopim_chat_plugin_error_init), Toast.LENGTH_LONG).show();

        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onResume() {
        registerReceiver(receiver, new IntentFilter() {{
            addAction(ACTION_TIMEOUT);
            addAction(ACTION_INITIALIZATION_TIMEOUT);
        }});

        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Account account = com.zopim.android.sdk.api.ZopimChat.getDataSource().getAccount();

        setResult(forceResultCancelled | account == null || Account.Status.ONLINE != account.getStatus() ? RESULT_CANCELED : RESULT_OK);
        finish();
    }

    @Override
    public void onChatLoaded(Chat chat) {}

    @Override
    public void onChatInitialized() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                overDrawRightButton();
            }
        });
    }

    @Override
    public void onChatEnded() {
        setResult(RESULT_CANCELED);
        finish();
    }

}

