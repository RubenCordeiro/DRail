package pt.up.fe.cmov.drail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class TripsActivity extends AppCompatActivity implements TripsListFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        int from = getIntent().getIntExtra("from", 0);
        int to = getIntent().getIntExtra("to", 0);

        Call<ArrayList<ArrayList<ApiService.Trip>>> tripsRequest = ApiService.service.getTrips("token", from, to);
        tripsRequest.enqueue(new Callback<ArrayList<ArrayList<ApiService.Trip>>>() {
            @Override
            public void onResponse(Response<ArrayList<ArrayList<ApiService.Trip>>> response, Retrofit retrofit) {
                if (response.isSuccess()) { // successful request, build graph

                    ArrayList<ArrayList<ApiService.Trip>> tripsList = response.body();

                    ArrayList<ArrayList<ApiService.Trip>> al = new ArrayList<>(tripsList.size());
                    for (ArrayList<ApiService.Trip> trips : tripsList) {
                        al.add(trips);
                    }

                    TripsListFragment tripsFragment = (TripsListFragment)
                            getSupportFragmentManager().findFragmentById(R.id.trips_fragment);

                    if (tripsFragment != null) {
                        tripsFragment.updateTripsView(al);
                    }

                } else {
                    Log.d("ResponseError", response.raw().request().urlString());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });

    }

    @Override
    public void onFragmentInteraction(String uri) {

    }
}
