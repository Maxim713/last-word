package com.maxim.last_word;

/**
 * Created by Максим on 22.04.2018.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.Date;


/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends FragmentActivity {

    private boolean isResumed = false;
    private static SharedPreferences bd;

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

        bd = getPreferences(MODE_PRIVATE);
        SharedPreferences sp = getSharedPreferences("savedStrings",
                Context.MODE_PRIVATE);

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
        public String temp;
        String getName(){
            String token = VKSdk.getAccessToken().accessToken;
            VKParameters parameters = VKParameters.from(VKApiConst.ACCESS_TOKEN, token);

            VKRequest request = new VKRequest("account.getProfileInfo", parameters);

            request.executeWithListener(new VKRequest.VKRequestListener()
            {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);

                    String status = "";

                    try {

                        JSONObject jsonObject = response.json.getJSONObject("response");

                        String first_name = jsonObject.getString("first_name");
                        String last_name = jsonObject.getString("last_name");
                        //String screen_name = jsonObject.getString("screen_name");
                        status = jsonObject.getString("status");
                        temp = first_name + " " + last_name;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
            return temp;
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
            date_view = v.findViewById(R.id.textView);

            try {
                //text.setText((bd.getString("textlastword", "")).replace("name", getName()));
                text.setText(bd.getString("textlastword", ""));
                long lastd = bd.getLong("lastdate", 0);
                Date last = new Date(lastd);
                date_view.setText(last.toString());


                if (Calendar.getInstance().getTimeInMillis()-86400000>lastd){

                    VKAttachments vka = new VKAttachments();
                    String myIDasString = VKSdk.getAccessToken().userId;
                    int myID = Integer.parseInt(myIDasString);
                    makePost(vka, bd.getString("textlastword", ""), myID);

                }else{
                    Toast toast = Toast.makeText(getContext(),
                            "Поздравляю, вы еще живы! Подтвердите этот факт нажатием на кнопку. Если хотите, можете изменить текст.", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
            catch(Exception e){

                Toast toast = Toast.makeText(getContext(),
                        "Добро пожаловать. Здесь вы можете написать завещание на случай, если не будете заходить в это приложение более суток.", Toast.LENGTH_SHORT);
                toast.show();

            }


            date_view = v.findViewById(R.id.textView);
            v.findViewById(R.id.continue_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    long currentMillis = Calendar.getInstance().getTimeInMillis();

                    SharedPreferences.Editor ed = bd.edit();
                    ed.putString("textlastword", text.getText().toString());
                    ed.commit();
                    ed.putLong("lastdate", currentMillis);
                    ed.commit();

                    Toast toast = Toast.makeText(getContext(),
                            "Вы подтвердили, что это вы а не ваш кот, поздравляю. Если вы изменили текст, он сохранен.", Toast.LENGTH_SHORT);
                    toast.show();

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