package com.example.dinindu.sales_data_sender;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

/**
 * Created by Dinindu on 7/24/2015.
 */
public class MainActivity extends Activity{

    private Button btnSendData;
    // Session Manager Class
    private SessionManager session;
    private TextView Name;
    private Button btnLogOut;
    private String image_URL;
    private String name;
    private String email;
    private Button btnChangePassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Buttons
        btnSendData = (Button) findViewById(R.id.btnSendData);
        btnLogOut = (Button) findViewById(R.id.btnLogOut);
        btnChangePassword = (Button) findViewById(R.id.btnChangePassword);

        // view products click event
        btnSendData.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching create new product activity
                Intent i = new Intent(getApplicationContext(), SendDataActivity.class);
                startActivity(i);

            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Logout();
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching create new product activity
                Intent i = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(i);

            }
        });

        viewUserDetails();
        // show The Image
        new DownloadImageTask((ImageView) findViewById(R.id.imageView))
                .execute(image_URL);
        //viewUserDetails();
    }

    public void viewUserDetails()
    {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.Shared_Preference), MODE_PRIVATE);
        name = prefs.getString("name", null);
        if (name != null) {
            name = prefs.getString("name", "No name defined");//"No name defined" is the default value.
            email = prefs.getString("email", null); //0 is the default value.
            image_URL = prefs.getString("image_source",null);
        }
        Log.d("Name : ", name);
        Log.d("Email : ", email);
        Log.d("Image URL : ", image_URL);
        Name = (TextView) findViewById(R.id.Name);
        Name.setText(name);

        TextView Email = (TextView) findViewById(R.id.Email);
        Email.setText(email);
    }

    public void Logout()
    {
        // Editor for Shared preferences
        SharedPreferences.Editor editor;
        SharedPreferences pre = getSharedPreferences(getString(R.string.Shared_Preference), MODE_PRIVATE);
        editor = pre.edit();
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        getApplicationContext().startActivity(i);
        finish();
    }
}


class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}

