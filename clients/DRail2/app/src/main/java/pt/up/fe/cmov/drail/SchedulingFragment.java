package pt.up.fe.cmov.drail;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SchedulingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SchedulingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SchedulingFragment extends Fragment {

    private GoogleMap map;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SchedulingFragment newInstance(int sectionNumber) {
        SchedulingFragment fragment = new SchedulingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SchedulingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    private static View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_scheduling, container, false);
            map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                    .getMap();

        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }

        Call<ApiService.Graph> graphRequest = ApiService.service.getGraph("token");
        graphRequest.enqueue(new Callback<ApiService.Graph>() {
            @Override
            public void onResponse(Response<ApiService.Graph> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ApiService.Graph graph = response.body();

                    HashMap<Integer, ApiService.Station> indexedStations = new HashMap<>();
                    for (ApiService.Station s: graph.stations) {
                        indexedStations.put(s.id, s);
                        map.addCircle(new CircleOptions()
                                .center(new LatLng(s.latitude, s.longitude))
                                .radius(70)
                                .fillColor(Color.BLUE)
                                .strokeWidth(1));
                    }

                    ApiService.Station source, target;
                    for (ApiService.GraphEdge e: graph.trips) {
                        source = indexedStations.get(e.start);
                        target = indexedStations.get(e.end);
                        map.addPolyline(new PolylineOptions()
                        .add(new LatLng(source.latitude, source.longitude), new LatLng(target.latitude, target.longitude))
                                .width(3).color(Color.RED));
                    }
                } else {
                    Log.d("Error", response.raw().request().urlString());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
