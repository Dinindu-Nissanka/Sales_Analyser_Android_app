package com.example.dinindu.sales_data_sender;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Dinindu on 9/7/2015.
 */
public class ChangePasswordActivity extends Activity {

    EditText inputCurrentPassword;
    EditText inputNewPassword_1;
    EditText inputNewPassword_2;
    Button btnSubmit;

    String currentPassword;
    String newPassword_1;
    String newPassword_2;
    String url_change_password;// = "http://10.0.2.2/se_project/change_password.php";

    JSONParser jsonParser = new JSONParser();
    private static final String TAG_SUCCESS = "success";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);

        url_change_password = getString(R.string.ChangePassword_URL);
        inputCurrentPassword = (EditText) findViewById(R.id.inputCurrentPassword);
        inputNewPassword_1 = (EditText) findViewById(R.id.inputNewPassword_1);
        inputNewPassword_2 = (EditText) findViewById(R.id.inputNewPassword_2);
        btnSubmit = (Button) findViewById(R.id.btnConfirm);

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // creating new product in background thread
                new ChangeDealerPassword().execute();
            }
        });
    }

    class ChangeDealerPassword extends AsyncTask<String, Integer, Integer>
    {
        @Override
        protected Integer doInBackground(String... args) {
            currentPassword = inputCurrentPassword.getText().toString();
            newPassword_1 = inputNewPassword_1.getText().toString();
            newPassword_2 = inputNewPassword_2.getText().toString();

            PasswordEncrypter pswEnrypt = new PasswordEncrypter();
            try {
                currentPassword = pswEnrypt.encrypt(currentPassword,getString(R.string.Encryption_Key));
                newPassword_1 = pswEnrypt.encrypt(newPassword_1, getString(R.string.Encryption_Key));
                newPassword_2 = pswEnrypt.encrypt(newPassword_2, getString(R.string.Encryption_Key));
                currentPassword = currentPassword.trim();
                newPassword_1 = newPassword_1.trim();
                newPassword_2 = newPassword_2.trim();
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

            SharedPreferences prefs = getSharedPreferences(getString(R.string.Shared_Preference), MODE_PRIVATE);
            String username = prefs.getString("username", null);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username",username));
            params.add(new BasicNameValuePair("current_password", currentPassword));
            params.add(new BasicNameValuePair("new_password_1", newPassword_1));
            params.add(new BasicNameValuePair("new_password_2", newPassword_2));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json_send = jsonParser.makeHttpRequest(url_change_password,
                    "POST", params);

            // check log cat for response
            Log.d("Send Response", json_send.toString());

            // check for success tag
            int success = 0;
            try {
                success = json_send.getInt(TAG_SUCCESS);
                Log.d("Success Status : ", String.valueOf(success));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return success;
        }

        protected void onPostExecute(Integer status)
        {
            //if successfully sent, show the relevant message
            if(status==1){
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(ChangePasswordActivity.this);
                dlgAlert.setMessage("Password was successfully chnaged");
                dlgAlert.setTitle("Success");
                dlgAlert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss the dialog
                                finish();
                            }
                        });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }

            //if fields are not filled,show the relevant message
            else if(status==2){
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(ChangePasswordActivity.this);
                dlgAlert.setMessage("Required fields are missing");
                dlgAlert.setTitle("Error");
                dlgAlert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss the dialog
                            }
                        });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }

            //if an error occured in the system, show the relevant message
            else if(status==3){
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(ChangePasswordActivity.this);
                dlgAlert.setMessage("Two passwords do not match with each other");
                dlgAlert.setTitle("Error");
                dlgAlert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss the dialog
                                inputCurrentPassword.setText("");
                                inputNewPassword_1.setText("");
                                inputNewPassword_2.setText("");
                            }
                        });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }

            else if(status==4){
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(ChangePasswordActivity.this);
                dlgAlert.setMessage("Oops an error occured while changing the password");
                dlgAlert.setTitle("Error");
                dlgAlert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss the dialog
                                finish();
                            }
                        });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }
            else{
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(ChangePasswordActivity.this);
                dlgAlert.setMessage("Password is incorrect");
                dlgAlert.setTitle("Error");
                dlgAlert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss the dialog
                                inputCurrentPassword.setText("");
                                inputNewPassword_1.setText("");
                                inputNewPassword_2.setText("");
                            }
                        });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }
        }
        }
    }


