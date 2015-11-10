package pt.up.fe.cmov.drail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TripsActivity extends AppCompatActivity implements TripListFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);
    }

    @Override
    public void onFragmentInteraction(String uri) {

    }
}
