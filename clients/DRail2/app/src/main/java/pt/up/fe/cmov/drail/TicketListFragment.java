package pt.up.fe.cmov.drail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static pt.up.fe.cmov.drail.ApiService.*;

/**
 * A list fragment representing a list of Tickets. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link TicketDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class TicketListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected(int id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(int id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TicketListFragment() {
    }

    public static ArrayList<ApiService.Ticket> mTicketList = new ArrayList<>();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            loadTickets();
        }
    }

    public void loadTicketsCache() {
        try {
            FileInputStream in = getContext().openFileInput("ticketdata");
            if (in.available() == 0)
                return;

            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String ticketList = bufferedReader.readLine();

            Gson gson = new Gson();
            mTicketList = gson.fromJson(ticketList, new TypeToken<ArrayList<Ticket>>(){}.getType());
            setListAdapter(new TicketListAdapter(getActivity(), mTicketList));
            Log.d("Error", "loaded data!! " + mTicketList.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTickets() {
        if (MainActivity.mLoginUser != null) {
            Call<ArrayList<ApiService.Ticket>> ticketsRequest = service.getTickets(MainActivity.mLoginUser.token, MainActivity.mLoginUser.id);
            ticketsRequest.enqueue(new Callback<ArrayList<ApiService.Ticket>>() {
                @Override
                public void onResponse(Response<ArrayList<ApiService.Ticket>> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        mTicketList = response.body();

                        ArrayList<ApiService.Ticket> toRemove = new ArrayList<>();
                        for (Ticket t : mTicketList) {
                            if (!t.status.equalsIgnoreCase("pending")) {
                                toRemove.add(t);
                            }
                        }

                        mTicketList.removeAll(toRemove);

                        setListAdapter(new TicketListAdapter(getActivity(), mTicketList));

                        Toast.makeText(getActivity(), "Loaded " + mTicketList.size() +
                                " tickets", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Error", response.raw().request().urlString());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    //Log.d("Error", t.getMessage());
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTicketsCache();
        loadTickets();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setEmptyText("No tickets");

        loadTickets();

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(position);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
