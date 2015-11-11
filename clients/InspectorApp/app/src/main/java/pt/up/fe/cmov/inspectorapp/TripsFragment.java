package pt.up.fe.cmov.inspectorapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class TripsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TripsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_trips, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyAdapter(new TripsItem[] { });
        mRecyclerView.setAdapter(mAdapter);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if (trips != null) {
                    Intent intent = new Intent(view.getContext(), TicketsActivity.class);
                    //intent.putExtra(EXTRA_TOKEN, trips.get(position));
                    startActivity(intent);
                }
            }
        });

        return view;
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

    public ArrayList<ArrayList<ApiService.Trip>> trips = null;

    public void updateTripsView(ArrayList<ArrayList<ApiService.Trip>> al) {
        trips = al;

        ArrayList<TripsItem> items = new ArrayList<>(al.size());

        for (ArrayList<ApiService.Trip> l : al) {
            Set<Integer> trains = new TreeSet<>();
            for (ApiService.Trip trip : l) {
                trains.add(trip.trainId);
            }

            Joiner joiner = Joiner.on(',');
            String trainsString = joiner.join(trains);

            TripsItem item = new TripsItem(l.get(0).departureDate, l.get(l.size() - 1).arrivalDate, trainsString);
            items.add(item);
        }

        TripsItem[] arr = new TripsItem[items.size()];
        ((MyAdapter) mAdapter).updateList(items.toArray(arr));
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
        public void onFragmentInteraction(String id);
    }

    public static class TripsItem {
        public final String departureDate;
        public final String arrivalDate;
        public final String trains;

        public TripsItem(String departureDate, String arrivalDate, String trains) {
            this.departureDate = departureDate;
            this.arrivalDate = arrivalDate;
            this.trains = trains;
        }
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private TripsItem[] mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mArrivalDateTextView;
            public TextView mDepartureDateTextView;
            public TextView mTrainsTextView;

            public ViewHolder(View v) {
                super(v);
                mArrivalDateTextView = (TextView) v.findViewById(R.id.arrival_date_text_view);
                mDepartureDateTextView = (TextView) v.findViewById(R.id.departure_date_text_view);
                mTrainsTextView = (TextView) v.findViewById(R.id.trains_text_view);
            }
        }

        public void updateList(TripsItem[] data) {
            mDataset = data;
            notifyDataSetChanged();
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(TripsItem[] myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.trips_list_item, parent, false);
            // set the view's size, margins, paddings and layout parameters
            //...
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mArrivalDateTextView.setText(Html.fromHtml("<b>Arrival date:</b> " + mDataset[position].arrivalDate));
            holder.mDepartureDateTextView.setText(Html.fromHtml("<b>Departure date:</b> " + mDataset[position].departureDate));
            holder.mTrainsTextView.setText(Html.fromHtml("<b>Train(s):</b> " + mDataset[position].trains));
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }

}
