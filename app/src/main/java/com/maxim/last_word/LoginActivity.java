package com.maxim.last_word;

/**
 * Created by Максим on 22.04.2018.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKWallPostResult;

import java.util.Calendar;
import java.util.Date;


/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends FragmentActivity {

    private boolean isResumed = false;
    private static SharedPreferences bd;
    public static boolean hasVisited;
    /**
     * Scope is set of required permissions for your application
     *
     * @see <a href="https://vk.com/dev/permissions">vk.com api permissions documentation</a>
     */
    private static final String[] sMyScope = new String[]{
            //VKScope.FRIENDS,
            VKScope.WALL
            //,
            //VKScope.PHOTOS,
            //VKScope.NOHTTPS,
            //VKScope.MESSAGES,
            //VKScope.DOCS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bd = getSharedPreferences("savedStrings", Context.MODE_PRIVATE);
        SharedPreferences sp = getSharedPreferences("savedStrings",
                Context.MODE_PRIVATE);
        // проверяем, первый ли раз открывается программа
        hasVisited = sp.getBoolean("hasVisited", false);

        setContentView(R.layout.activity_start);
        VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                if (isResumed) {
                    switch (res) {
                        case LoggedOut:
                            showLogin();
                            break;
                        case LoggedIn:
                            showLogout();
                            break;
                        case Pending:
                            break;
                        case Unknown:
                            break;
                    }
                }
            }

            @Override
            public void onError(VKError error) {

            }
        });

//        String[] fingerprint = VKUtil.getCertificateFingerprint(this, this.getPackageName());
//        Log.d("Fingerprint", fingerprint[0]);
    }

    private void showLogout() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LogoutFragment())
                .commitAllowingStateLoss();
    }

    private void showLogin() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commitAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        if (VKSdk.isLoggedIn()) {
            showLogout();
        } else {
            showLogin();
        }
    }

    @Override
    protected void onPause() {
        isResumed = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // User passed Authorization
                //startMainActivity();
                showLogout();
            }

            @Override
            public void onError(VKError error) {
                //startMainActivity();
                // User didn't pass Authorization
            }
        };

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

//    private void startMainActivity() {
//        startActivity(new Intent(this, MainActivity.class));
//    }

    public static class LoginFragment extends android.support.v4.app.Fragment {
        public LoginFragment() {
            super();
        }
        //bd = getSharedPreferences("savedStrings", Context.MODE_PRIVATE);
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_login, container, false);
            v.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKSdk.login(getActivity(), sMyScope);
                }
            });
            return v;
        }

    }
    public static EditText text;
    public static TextView date_view;
    public static class LogoutFragment extends android.support.v4.app.Fragment {
        public LogoutFragment() {
            super();

        }

        void makePost(VKAttachments att, String msg, final int ownerId) {
            VKParameters parameters = new VKParameters();
            parameters.put(VKApiConst.OWNER_ID, String.valueOf(ownerId));
            parameters.put(VKApiConst.ATTACHMENTS, att);
            parameters.put(VKApiConst.MESSAGE, msg);
            VKRequest post = VKApi.wall().post(parameters);
            post.setModelClass(VKWallPostResult.class);
            post.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    Toast toast = Toast.makeText(getContext(),
                            "Вы не заходили в это приложение более суток! Похоже вам " +
                                    "черезвыйчайно плохо! Ваше завещание уже опубликовано.", Toast.LENGTH_SHORT);
                    toast.show();
                }
                @Override
                public void onError(VKError error) {
                    // error
                }
            });
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_logout, container, false);
            text = v.findViewById(R.id.text_lastword);



            if (hasVisited) {
                text.setText(bd.getString("textlastword", ""));
                date_view.setText(bd.getString("lastdate", ""));

               // if (bd.getString("lastdate", "")){

               // }
            }

            date_view = v.findViewById(R.id.textView);
            v.findViewById(R.id.continue_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Date date = new Date();
                    date_view.setText(date.toString());
                    SharedPreferences.Editor editor = bd.edit();
                    editor.putString("textlastword", text.getText().toString());
                    editor.putString("lastdate", date_view.getText().toString());
                    editor.apply();
                    editor.commit();


                    // currentTime = checkDate
                    Date currentTime = Calendar.getInstance().getTime();
                    long currentMillis = Calendar.getInstance().getTimeInMillis();
                    Date checkDate = new Date (currentMillis);


                    String t = text.getText().toString();

                    VKAttachments vka = new VKAttachments();
                    String myIDasString = VKSdk.getAccessToken().userId;
                    int myID = Integer.parseInt(myIDasString);


                    makePost(vka, "testing is 300 bucks", myID);
                }});

            v.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKSdk.logout();
                    if (!VKSdk.isLoggedIn()) {
                        ((LoginActivity) getActivity()).showLogin();
                    }
                }
            });
            return v;
        }
    }
}