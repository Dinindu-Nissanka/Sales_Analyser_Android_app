package com.example.dinindu.sales_data_sender;

/**
 * Created by Dinindu on 7/24/2015.
 */
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

public class SendDataActivity extends Activity implements AdapterView.OnItemSelectedListener{

    // Progress Dialog
    private ProgressDialog pDialog;
    private Spinner countrySpinner,regionSpinner;
    private Calendar cal = Calendar.getInstance();

    JSONParser jsonParser = new JSONParser();
    EditText inputPrice;
    EditText inputQuantity;
    EditText inputDate;
    private String selectedDate;

    // url to send sales data
    private static String url_send_data ;//= "http://10.0.2.2/se_project/send_data.php";//"http://dinindu.pe.hu/send_data.php";

    //url to get countries
    private static String url_countries ;//= "http://10.0.2.2/se_project/get_countries.php";//"http://dinindu.pe.hu/get_countries.php";
    //private static String url_countries = "http://file-manager.000webhost.com/file-manager/index.php";

    //url to get regions
    private static String url_regions ;//= "http://10.0.2.2/se_project/get_regions.php";//"http://dinindu.pe.hu/get_regions.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    private JSONArray countries;
    private JSONArray regions;
    private List<String> countryList = new ArrayList<String>();
    private List<Region> regionList = new ArrayList<Region>();
    private String country;
    private String region;
    private int region_id;

    //Date picker widget to show
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, monthOfYear);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    //show picked date in the text field
    private void updateLabel() {

        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        inputDate.setText(sdf.format(cal.getTime()));
        this.selectedDate = sdf.format(cal.getTime());
        Log.d("Date :", selectedDate);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        url_send_data = getString(R.string.Send_URL);
        url_countries = getString(R.string.Country_URL);
        url_regions = getString(R.string.Region_URL);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senddata);

        // Spinner element
        Spinner country_spinner = (Spinner) findViewById(R.id.dropListCountries);
        // Spinner click listener
        country_spinner.setOnItemSelectedListener(this);

        // Spinner element
        Spinner region_spinner = (Spinner) findViewById(R.id.dropListRegions);
        // Spinner click listener
        region_spinner.setOnItemSelectedListener(this);

        // Edit Text
        inputPrice = (EditText) findViewById(R.id.inputPrice);
        inputQuantity = (EditText) findViewById(R.id.inputQuantity);
        inputDate = (EditText) findViewById(R.id.inputDate);

        // Create button
        Button btnCreateProduct = (Button) findViewById(R.id.btnSendData);

        //drop down list
        new LoadCountries().execute();

        // button click event
        btnCreateProduct.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // creating new product in background thread
                new SendSalesData().execute();
            }
        });

        //date text field click event
        inputDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(SendDataActivity.this, date, cal
                        .get(Calendar.YEAR), cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    //event listeners for the drop lists
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner parentSpinner = (Spinner) parent;
        if(parentSpinner.getId() == R.id.dropListCountries){
            this.country = parentSpinner.getItemAtPosition(position).toString();
            new LoadRegions().execute();
        }
        else{
            Region rw = (Region) parentSpinner.getItemAtPosition(position);
            this.region_id = rw.region_id;
            Log.d("region_id :",String.valueOf(this.region_id));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Background Async Task to Load regions to the drop list
     **/
    class LoadRegions extends AsyncTask<String, Integer, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SendDataActivity.this);
            pDialog.setMessage("Loading Regions..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Sending sales data
         * */
        protected String doInBackground(String... args) {
            regionList.clear();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("country", country));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_regions,
                    "POST", params);

            // check log cat for response
            Log.d("Create Response", json.toString());

            // check for success tag
            int success = 0;
            try {
                success = json.getInt(TAG_SUCCESS);
                regions = json.getJSONArray("regions");
                for (int i = 0; i < regions.length(); i++) {
                    JSONObject tempObject = regions.getJSONObject(i);
                    String tempRegion= tempObject.getString("region");
                    int tempID = Integer.valueOf(tempObject.getString("region_id"));
                    regionList.add(new Region(tempRegion,tempID));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return  null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    regionSpinner = (Spinner) findViewById(R.id.dropListRegions);
                    ArrayAdapter<Region> dataAdapter = new ArrayAdapter<Region>(SendDataActivity.this, android.R.layout.simple_spinner_item, regionList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    regionSpinner.setAdapter(dataAdapter);
                }
            });
        }
    }

    /**
     * Background Async Task to Load countries to the drop list
     * */
    class LoadCountries extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_countries,
                    "GET", params);

            // check log cat for response
            Log.d("Get Countries", json.toString());
            int success = 0;
            try {
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    countries = json.getJSONArray("countries");
                    for (int i = 0; i < countries.length(); i++) {
                        JSONObject temp = countries.getJSONObject(i);
                        String tempCountry = temp.getString("country");
                        countryList.add(tempCountry);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            runOnUiThread(new Runnable() {
                public void run() {
                    countrySpinner = (Spinner) findViewById(R.id.dropListCountries);
                    ArrayAdapter dataAdapter = new ArrayAdapter(SendDataActivity.this, android.R.layout.simple_spinner_item, countryList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    countrySpinner.setAdapter(dataAdapter);
                }
            });
        }
    }

    /**
     * Background Async Task to Send sales data
     * */
    class SendSalesData extends AsyncTask<String, Integer, Integer> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SendDataActivity.this);
            pDialog.setMessage("Creating Product..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Sending sales data
         * */
        protected Integer doInBackground(String... args) {
            String price = inputPrice.getText().toString();
            String quantity = inputQuantity.getText().toString();
            double total_income = Integer.parseInt(quantity) * Double.parseDouble(price);

            SharedPreferences prefs = getSharedPreferences(getString(R.string.Shared_Preference), MODE_PRIVATE);
            String user_id = prefs.getString("user_id", null);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_id",user_id));
            params.add(new BasicNameValuePair("price", price));
            params.add(new BasicNameValuePair("quantity", quantity));
            params.add(new BasicNameValuePair("date", selectedDate));
            params.add(new BasicNameValuePair("total_income",String.valueOf(total_income)));
            params.add(new BasicNameValuePair("region_id",String.valueOf(region_id)));

            Log.d("Username : ", "Dinindu");
            Log.d("Price : ", price);
            Log.d("Quantity : ", quantity);
            Log.d("Date : ", selectedDate);
            Log.d("Income : ", String.valueOf(total_income));
            Log.d("Region : ", String.valueOf(region_id));


            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json_send = jsonParser.makeHttpRequest(url_send_data,
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

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(Integer status) {
            // dismiss the dialog once done
            pDialog.dismiss();

            //if successfully sent, show the relevant message
            if(status==1){
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(SendDataActivity.this);
                dlgAlert.setMessage("Sales data was successfully sent");
                dlgAlert.setTitle("Information");
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
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(SendDataActivity.this);
                dlgAlert.setMessage("Required fields are missing");
                dlgAlert.setTitle("Information");
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
            else{
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(SendDataActivity.this);
                dlgAlert.setMessage("Oops! An error occured while sending data");
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
        }
    }
}
