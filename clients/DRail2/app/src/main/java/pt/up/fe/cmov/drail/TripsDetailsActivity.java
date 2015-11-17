package pt.up.fe.cmov.drail;


import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class TripsDetailsActivity extends AppCompatActivity {

    private ArrayList<ApiService.TripValidation> toBuy = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ArrayList<ApiService.HydratedTrip> trips = (ArrayList<ApiService.HydratedTrip>) getIntent().getSerializableExtra("trips");
        TableLayout stk = (TableLayout) findViewById(R.id.table_main);
        stk.addView(createItineraryTitle());

        Integer previousTrainId = null;
        ApiService.HydratedTrip previousTrip = null;
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        int currentIndex = 0;
        for (ApiService.HydratedTrip trip : trips) {

            toBuy.add(new ApiService.TripValidation(trip.id, trip.trainId));

            if (previousTrainId != null && !previousTrainId.equals(trip.trainId)) {
                //stk.addView(createSeparator());
                stk.addView(createTransferTitle());
                try {
                    Date earlierDate = format.parse(previousTrip.arrivalDate);
                    Date laterDate = format.parse(trip.departureDate);

                    int minutesElapsed = (int)((laterDate.getTime()/60000) - (earlierDate.getTime()/60000));
                    stk.addView(createTransferEntry(Integer.toString(trip.trainId), Integer.toString(minutesElapsed) + " Minutes"));
                    //stk.addView(createSeparator());
                    stk.addView(createItineraryTitle());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if (currentIndex != trips.size() - 1) { // not last
                stk.addView(createItineraryEntry(trip, false));
            } else { // last, show destination
                stk.addView(createItineraryEntry(trip, false));
                stk.addView(createItineraryEntry(trip, true));
            }

            previousTrainId = trip.trainId;
            previousTrip = trip;
            ++currentIndex;
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Call<String> tripsRequest = ApiService.service.buyTickets(MainActivity.mLoginUser.token,
                        MainActivity.mLoginUser.id,
                        new ApiService.TripsValidation(toBuy));
                tripsRequest.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Response<String> response, Retrofit retrofit) {
                        if (response.isSuccess()) { // successful request, build graph
                            finish();
                        } else {
                            Snackbar.make(findViewById(R.id.trip_details_table),
                                    "Error buying ticket: " + response.body(),
                                    Snackbar.LENGTH_LONG).show();

                            Log.d("ResponseError", response.raw().request().urlString());
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d("Error", t.getMessage());
                        finish();
                    }
                });
            }
        });
    }


    private View createSeparator() {
        View v = new View(getApplicationContext());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.height = 4;
        v.setLayoutParams(params);
        v.setBackgroundColor(Color.BLACK);

        return v;
    }

    private TableRow createItineraryTitle() {
        TableRow tr = new TableRow(getApplicationContext());

        TextView stationTextView = new TextView(getApplicationContext());
        stationTextView.setText("Station");
        stationTextView.setTextColor(Color.BLACK);

        TextView passageTimeTextView = new TextView(getApplicationContext());
        passageTimeTextView.setText("Passage time");
        passageTimeTextView.setTextColor(Color.BLACK);


        TextView trainTextView = new TextView(getApplicationContext());
        trainTextView.setGravity(Gravity.RIGHT);
        trainTextView.setText("Train");
        trainTextView.setTextColor(Color.BLACK);


        tr.addView(stationTextView);
        tr.addView(passageTimeTextView);
        tr.addView(trainTextView);

        return tr;
    }

    private TableRow createItineraryEntry(ApiService.HydratedTrip trip, boolean isLast) {
        TableRow tr = new TableRow(getApplicationContext());

        TextView stationTextView = new TextView(getApplicationContext());

        if (!isLast) {
            stationTextView.setText(trip.prevStationName);
        } else {
            stationTextView.setText(trip.nextStationName);
        }
        stationTextView.setTextColor(Color.BLACK);


        TextView passageTimeTextView = new TextView(getApplicationContext());

        if (!isLast) {
            passageTimeTextView.setText(trip.departureDate);
        } else {
            passageTimeTextView.setText(trip.arrivalDate);
        }
        passageTimeTextView.setTextColor(Color.BLACK);


        TextView trainTextView = new TextView(getApplicationContext());
        trainTextView.setGravity(Gravity.RIGHT);
        trainTextView.setText(Integer.toString(trip.trainId));
        trainTextView.setTextColor(Color.BLACK);


        tr.addView(stationTextView);
        tr.addView(passageTimeTextView);
        tr.addView(trainTextView);

        return tr;
    }

    private TableRow createTransferTitle() {
        TableRow tr = new TableRow(getApplicationContext());

        TextView dummyTextView = new TextView(getApplicationContext());
        dummyTextView.setText("Transfer");
        dummyTextView.setTextColor(Color.BLACK);



        TextView trainTextView = new TextView(getApplicationContext());
        trainTextView.setText("Target Train");
        trainTextView.setTextColor(Color.BLACK);


        TextView passageTimeTextView = new TextView(getApplicationContext());
        passageTimeTextView.setGravity(Gravity.RIGHT);
        passageTimeTextView.setText("Waiting Time");
        passageTimeTextView.setTextColor(Color.BLACK);


        tr.addView(dummyTextView);
        tr.addView(trainTextView);
        tr.addView(passageTimeTextView);

        return tr;
    }

    private TableRow createTransferEntry(String trainName, String waitingTime) {
        TableRow tr = new TableRow(getApplicationContext());

        TextView dummyTextView = new TextView(getApplicationContext());
        dummyTextView.setText("");


        TextView trainTextView = new TextView(getApplicationContext());
        trainTextView.setText(trainName);
        trainTextView.setTextColor(Color.BLACK);


        TextView passageTimeTextView = new TextView(getApplicationContext());
        passageTimeTextView.setGravity(Gravity.RIGHT);
        passageTimeTextView.setText(waitingTime);
        passageTimeTextView.setTextColor(Color.BLACK);


        tr.addView(dummyTextView);
        tr.addView(trainTextView);
        tr.addView(passageTimeTextView);

        return tr;
    }

    private TableRow createSummaryEntry(int TotalDistance) {
        TableRow tr = new TableRow(getApplicationContext());

        TextView dummyTextView1 = new TextView(getApplicationContext());
        dummyTextView1.setText("TotalCost:");


        TextView dummyTextView2 = new TextView(getApplicationContext());
        dummyTextView2.setText("");

        TextView passageTimeTextView = new TextView(getApplicationContext());
        passageTimeTextView.setGravity(Gravity.RIGHT);
        passageTimeTextView.setText(Integer.toString(TotalDistance));

        tr.addView(dummyTextView1);
        tr.addView(dummyTextView2);
        tr.addView(passageTimeTextView);

        return tr;
    }

}
