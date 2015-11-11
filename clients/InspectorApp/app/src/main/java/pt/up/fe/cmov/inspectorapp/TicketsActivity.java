package pt.up.fe.cmov.inspectorapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.common.base.Joiner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class TicketsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton actionQr = (FloatingActionButton) findViewById(R.id.action_qr);
        actionQr.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Scan the QR code", Toast.LENGTH_SHORT).show();
            }
        });

        final FloatingActionButton actionUpload = (FloatingActionButton) findViewById(R.id.action_upload);
        actionUpload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Upload scanned ticket data", Toast.LENGTH_SHORT).show();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.tickets_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new TicketListAdapter(new ArrayList<ApiService.Ticket>());
        mRecyclerView.setAdapter(mAdapter);

        ArrayList<Integer> trips = getIntent().getIntegerArrayListExtra("trips");

        Call<List<ApiService.Ticket>> ticketsRequest = ApiService.service.listTickets("token", trips);
        ticketsRequest.enqueue(new Callback<List<ApiService.Ticket>>() {
            @Override
            public void onResponse(Response<List<ApiService.Ticket>> response, Retrofit retrofit) {


                if (response.isSuccess()) {
                    final List<ApiService.Ticket> stationList = response.body();

                    Log.d("Error", Integer.toString(stationList.size()));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TicketListAdapter)mAdapter).updateList(new ArrayList<>(stationList));
                            Toast.makeText(TicketsActivity.this, "Loaded " + stationList.size() +
                                    " tickets", Toast.LENGTH_SHORT).show();
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
    }

    public static class TicketListAdapter extends RecyclerView.Adapter<TicketListAdapter.ViewHolder> {
        private ArrayList<ApiService.Ticket> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mDateTextView;
            public TextView mStatusTextView;
            public TextView mTripsTextView;
            public TextView mIdTextView;

            public ViewHolder(View v) {
                super(v);
                mDateTextView = (TextView) v.findViewById(R.id.date_text_view);
                mStatusTextView = (TextView) v.findViewById(R.id.status_text_view);
                mTripsTextView = (TextView) v.findViewById(R.id.trips_text_view);
                mIdTextView = (TextView) v.findViewById(R.id.id_text_view);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public TicketListAdapter(ArrayList<ApiService.Ticket> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public TicketListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.tickets_list_item, parent, false);
            // set the view's size, margins, paddings and layout parameters
            //...
            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mDateTextView.setText(Html.fromHtml("<b>Date:</b> " +
                    mDataset.get(position).creationDate.replace('T', ' ').substring(0, 19)));
            holder.mStatusTextView.setText(Html.fromHtml("<b>Status:</b> " +
                    mDataset.get(position).status));
            holder.mTripsTextView.setText(Html.fromHtml("<b>Train(s):</b> " +
                    Joiner.on(',').join(mDataset.get(position).trips)));
            holder.mIdTextView.setText(Html.fromHtml("<b>#</b>" +
                    mDataset.get(position).id));
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public void updateList(ArrayList<ApiService.Ticket> data) {
            mDataset = data;
            notifyItemRangeInserted(0, mDataset.size());
            notifyDataSetChanged();
        }
    }
}
