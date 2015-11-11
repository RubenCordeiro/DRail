package pt.up.fe.cmov.drail;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class TripsDetailsActivity extends AppCompatActivity {

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
        for (ApiService.HydratedTrip trip : trips) {

            if (previousTrainId != null && !previousTrainId.equals(trip.trainId)) {
                //stk.addView(createSeparator());
                stk.addView(createTransferTitle());
                stk.addView(createTransferEntry(Integer.toString(trip.trainId), "00:05:00"));
                //stk.addView(createSeparator());
                stk.addView(createItineraryTitle());
            }

            stk.addView(createItineraryEntry(trip));
            previousTrainId = trip.trainId;
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

    private TableRow createItineraryEntry(ApiService.HydratedTrip trip) {
        TableRow tr = new TableRow(getApplicationContext());

        TextView stationTextView = new TextView(getApplicationContext());
        stationTextView.setText(trip.prevStationName);
        stationTextView.setTextColor(Color.BLACK);


        TextView passageTimeTextView = new TextView(getApplicationContext());
        passageTimeTextView.setText(trip.departureDate);
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
