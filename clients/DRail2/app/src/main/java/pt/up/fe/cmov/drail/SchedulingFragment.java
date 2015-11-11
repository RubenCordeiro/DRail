package pt.up.fe.cmov.drail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.BubbleIconFactory;
import com.google.maps.android.ui.IconGenerator;

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
public class SchedulingFragment extends Fragment implements GoogleMap.OnMyLocationChangeListener, GoogleMap.OnMarkerClickListener {

    public enum StationSelectionState {
        FIRST_CLICK, SECOND_CLICK, NONE
    }

    private GoogleMap map;
    private Location currentUserLocation;
    private HashMap<Marker, ApiService.Station> indexedMarkers = new HashMap<>();
    private HashMap<ApiService.Station, Circle> indexedCircles = new HashMap<>();
    private ApiService.Station firstClickedStation = null;
    private ApiService.Station secondClickedStation = null;
    private StationSelectionState currentSelectionState = StationSelectionState.NONE;

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

            map.setOnMyLocationChangeListener(this);
            map.setOnMarkerClickListener(this);

        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }

        final FloatingActionButton actionQr = (FloatingActionButton) view.findViewById(R.id.fab);
        actionQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (firstClickedStation == null || secondClickedStation == null) {
                    return;
                }

                Intent intent = new Intent(view.getContext(), TripsActivity.class);
                intent.putExtra("from", SchedulingFragment.this.firstClickedStation.id);
                intent.putExtra("to", SchedulingFragment.this.secondClickedStation.id);
                startActivity(intent);
            }
        });

        Call<ApiService.Graph> graphRequest = ApiService.service.getGraph("token");
        graphRequest.enqueue(new Callback<ApiService.Graph>() {
            @Override
            public void onResponse(Response<ApiService.Graph> response, Retrofit retrofit) {
                if (response.isSuccess()) { // successful request, build graph
                    ApiService.Graph graph = response.body();

                    IconGenerator iconGenerator = new IconGenerator(getActivity().getApplicationContext());
                    iconGenerator.setContentPadding(0, 0, 0, 0);
                    HashMap<Integer, ApiService.Station> indexedStations = new HashMap<>();
                    for (ApiService.Station s : graph.stations) {

                        indexedStations.put(s.id, s);
                        LatLng position = new LatLng(s.latitude, s.longitude);


                        Bitmap iconBitmap = iconGenerator.makeIcon(s.name);
                        Marker stationMarker = map.addMarker(new MarkerOptions().
                                        icon(BitmapDescriptorFactory.fromBitmap(iconBitmap))
                                        .position(position)
                        );

                        indexedMarkers.put(stationMarker, s);

                        Circle stationCircle = map.addCircle(new CircleOptions()
                                .center(position)
                                .radius(70)
                                .fillColor(Color.BLUE)
                                .strokeWidth(1));

                        indexedCircles.put(s, stationCircle);

                        Location stationLocation = new Location("none");
                        stationLocation.setLongitude(s.longitude);
                        stationLocation.setLatitude(s.latitude);
                        if (s.name.equals("Central")) {
                            Log.d("Location", "Camera set");
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(s.latitude, s.longitude))
                                    .zoom(15)
                                    .bearing(0)
                                    .build();

                            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    }

                    ApiService.Station source, target;
                    for (ApiService.GraphEdge e : graph.trips) {

                        source = indexedStations.get(e.start);
                        target = indexedStations.get(e.end);

                        map.addPolyline(new PolylineOptions()
                                .add(new LatLng(source.latitude,
                                        source.longitude), new LatLng(target.latitude, target.longitude))
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

    @Override
    public void onMyLocationChange(Location location) {
        Log.d("Location", location.toString());
        currentUserLocation = location;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        switch(currentSelectionState) {
            case NONE: {
                firstClickedStation = indexedMarkers.get(marker);
                indexedCircles.get(firstClickedStation).setFillColor(Color.RED);
                currentSelectionState = StationSelectionState.FIRST_CLICK;
                break;
            }
            case FIRST_CLICK: {
                secondClickedStation = indexedMarkers.get(marker);
                indexedCircles.get(secondClickedStation).setFillColor(Color.RED);
                currentSelectionState = StationSelectionState.SECOND_CLICK;
                break;
            }
            case SECOND_CLICK: {
                indexedCircles.get(firstClickedStation).setFillColor(Color.BLUE);
                indexedCircles.get(secondClickedStation).setFillColor(Color.BLUE);

                firstClickedStation = null;
                secondClickedStation = null;
                currentSelectionState = StationSelectionState.NONE;
                break;
            }
            default:
                Log.d("Extreme anomaly", "We're talking java here, anything is possible");
        }

        return true;
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
