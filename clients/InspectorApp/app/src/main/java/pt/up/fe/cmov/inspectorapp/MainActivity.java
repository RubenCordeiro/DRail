package pt.up.fe.cmov.inspectorapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TimePicker;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    ClickToSelectEditText editTextDepartureStation;
    ClickToSelectEditText editTextArrivalStation;
    TimePickerEditText editTextTimePicker;

    String selectedDepartureStation;
    String selectedArrivalStation;
    int hourOfDay;
    int minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Call<List<ApiService.Station>> stationsRequest = ApiService.service.listStations("token");
        stationsRequest.enqueue(new Callback<List<ApiService.Station>>() {
            @Override
            public void onResponse(Response<List<ApiService.Station>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<ApiService.Station> stationList = response.body();
                    final List<String> stationNames = new ArrayList<>(stationList.size());

                    for (ApiService.Station s : stationList) {
                        stationNames.add(s.name);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            editTextDepartureStation.setItems(stationNames);
                            editTextArrivalStation.setItems(stationNames);
                        }
                    });
                } else {
                    Log.d("Error", response.raw().request().urlString());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });

        editTextDepartureStation = (ClickToSelectEditText) findViewById(R.id.text_input_departure_station);
        editTextArrivalStation = (ClickToSelectEditText) findViewById(R.id.text_input_arrival_station);

        editTextTimePicker = (TimePickerEditText) findViewById(R.id.text_input_time);
        editTextTimePicker.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                MainActivity.this.hourOfDay = hourOfDay;
                MainActivity.this.minute = minute;
            }
        });
        editTextTimePicker.configureOnClickListener(getFragmentManager());

        editTextDepartureStation.setOnItemSelectedListener(new ClickToSelectEditText.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelectedListener(String item, int selectedIndex) {
                selectedDepartureStation = item;
            }
        });

        editTextArrivalStation.setOnItemSelectedListener(new ClickToSelectEditText.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelectedListener(String item, int selectedIndex) {
                selectedArrivalStation = item;
            }
        });

        Button listTrainsButton = (Button) findViewById(R.id.list_trains_button);
        listTrainsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateFormat dateFormat = new SimpleDateFormat();
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);

                Call<List<ApiService.Train>> trainsRequest = ApiService.service.listTrains("token",
                        selectedDepartureStation, selectedArrivalStation, dateFormat.format(cal.getTime()));
                trainsRequest.enqueue(new Callback<List<ApiService.Train>>() {
                    @Override
                    public void onResponse(Response<List<ApiService.Train>> response, Retrofit retrofit) {
                        if (response.isSuccess()) {
                            List<ApiService.Train> trainList = response.body();



                        } else {
                            try {
                                Log.d("Error", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d("Error", t.getMessage());
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
