package pt.up.fe.cmov.inspectorapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TimePicker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    ClickToSelectEditText editTextDepartureStation;
    ClickToSelectEditText editTextArrivalStation;
    TimePickerEditText editTextTimePicker;

    String selectedDepartureStation;
    String selectedArrivalStation;
    int HourOfDay;
    int Minute;

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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiService.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService.DRail service = retrofit.create(ApiService.DRail.class);

        Call<List<ApiService.Station>> stationsRequest = service.listStations("token");
        String[] stations;
        try {
            Response<List<ApiService.Station>> stationsResponse = stationsRequest.execute();
            List<ApiService.Station> stationList = stationsResponse.body();
            List<String> stationNames = new ArrayList<>();

            for (ApiService.Station s : stationList) {
                stationNames.add(s.name);
            }

            stations = (String[]) stationNames.toArray();

        } catch (IOException e) {
            e.printStackTrace();
            stations = new String[]{};
        }

        editTextDepartureStation = (ClickToSelectEditText) findViewById(R.id.text_input_departure_station);
        editTextArrivalStation = (ClickToSelectEditText) findViewById(R.id.text_input_arrival_station);
        editTextDepartureStation.setItems(stations);
        editTextArrivalStation.setItems(stations);
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

        editTextTimePicker = (TimePickerEditText) findViewById(R.id.text_input_time);
        editTextTimePicker.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                HourOfDay = hourOfDay;
                Minute = minute;
            }
        });
        editTextTimePicker.configureOnClickListener(getFragmentManager());
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