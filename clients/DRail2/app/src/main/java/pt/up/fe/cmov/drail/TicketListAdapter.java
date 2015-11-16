package pt.up.fe.cmov.drail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class TicketListAdapter extends ArrayAdapter<ApiService.Ticket> {

    public TicketListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public TicketListAdapter(Context context, int resource, List<ApiService.Ticket> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.trips_list_item, null);
        }

        ApiService.Ticket ticket = getItem(position);

        if (ticket != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.ticket_title);
            TextView tt2 = (TextView) v.findViewById(R.id.ticket_date);

            if (tt1 != null) {
                tt1.setText(ticket.startStation + " to " + ticket.endStation);
            }

            if (tt2 != null) {
                tt2.setText(ticket.creationDate);
            }
        }

        return v;
    }

}