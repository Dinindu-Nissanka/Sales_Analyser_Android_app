package com.example.dinindu.sales_data_sender;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Dinindu on 7/24/2015.
 */

public class LoginActivity extends Activity {

    private EditText user, pass;
    private Button btnLogin;
    // Progress Dialog
    private ProgressDialog pDialog;

    // Session Manager Class
    public SessionManager session;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    private String LOGIN_URL ;//= getString(R.string.Login_URL);
    //"http://dinindu.pe.hu/login.php";//http://file-manager.000webhost.com/file-manager/index.php

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override protected void onCreate(Bundle savedInstanceState) {

        LOGIN_URL = getString(R.string.Login_URL);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        user = (EditText)findViewById(R.id.username);
        pass = (EditText)findViewById(R.id.password);
        btnLogin = (Button)findViewById(R.id.login);
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // creating new product in background thread
                new AttemptLogin().execute();
            }
        });
        SharedPreferences prefs = getSharedPreferences(getString(R.string.Shared_Preference), MODE_PRIVATE);
        String username = prefs.getString("username", null);
        if(username!=null)
        {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            finish();
            startActivity(i);
        }
    }

    class AttemptLogin extends AsyncTask<String, String, String> {
        /** * Before starting background thread Show Progress Dialog * */
        boolean failure = false;
        @Override protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Attempting for login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // here Check for success tag
            int success;
            String username = user.getText().toString();
            String tempPassword = pass.getText().toString();
            PasswordEncrypter pswEnrypt = new PasswordEncrypter();
            String password = null;
            try {
                password = pswEnrypt.encrypt(tempPassword,getString(R.string.Encryption_Key));
                password = password.trim();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
            Log.d("Encrypted Password : ",password);

            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));

                Log.d("request!", "starting");

                JSONObject json = jsonParser.makeHttpRequest( LOGIN_URL, "POST", params);

                // success tag for json
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    JSONArray dealer = json.getJSONArray("dealer");
                    JSONObject tempObject = dealer.getJSONObject(0);
                    String tempUsername= tempObject.getString("username");
                    String user_id = tempObject.getString("user_id");
                    String name = tempObject.getString("name");
                    String email = tempObject.getString("email");
                    String image_source = tempObject.getString("image_source");

                    SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.Shared_Preference), MODE_PRIVATE).edit();
                    editor.putString("username", tempUsername);
                    editor.putString("user_id", user_id);
                    editor.putString("name", name);
                    editor.putString("email", email);
                    editor.putString("image_source", image_source);
                    editor.commit();

                    Log.d("Successfully Login!", json.toString());
                    Intent ii = new Intent(LoginActivity.this,MainActivity.class);
                    finish();
                    // this finish() method is used to tell android os that we are done with current
                    // activity now! Moving to other activity

                    startActivity(ii);
                    return json.getString(TAG_MESSAGE);
                }
                else
                {
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /** * Once the background process is done we need to Dismiss the progress dialog asap * **/
        protected void onPostExecute(String message) {
            pDialog.dismiss();
            if (message != null){
                pass.setText("");
                user.setText("");
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
