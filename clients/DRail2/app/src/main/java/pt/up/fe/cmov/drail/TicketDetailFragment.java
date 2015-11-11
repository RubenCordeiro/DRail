package pt.up.fe.cmov.drail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.glxn.qrgen.android.QRCode;

import java.nio.charset.Charset;

import pt.up.fe.cmov.drail.dummy.DummyContent;

/**
 * A fragment representing a single Ticket detail screen.
 * This fragment is either contained in a {@link TicketListActivity}
 * in two-pane mode (on tablets) or a {@link TicketDetailActivity}
 * on handsets.
 */
public class TicketDetailFragment extends Fragment
        implements NfcAdapter.OnNdefPushCompleteCallback {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private ApiService.Ticket mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TicketDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = TicketListFragment.mTicketList.get(getArguments().getInt(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle("Ticket #" + mItem.id);
            }

            NfcAdapter mNfcAdapter;
            String tag;
            byte[] message;

            // Check for available NFC Adapter
            mNfcAdapter = NfcAdapter.getDefaultAdapter(getContext());
            if (mNfcAdapter == null) {
                Toast.makeText(getContext(), "NFC is not available on this device.", Toast.LENGTH_LONG).show();
                return;
            }

            NdefMessage msg = new NdefMessage(new NdefRecord[] {
                createMimeRecord("application/pt.up.fe.cmov.type1",
                Integer.toString(mItem.id).getBytes())
            });

            // Register a NDEF message to be sent in a beam operation (P2P)
            mNfcAdapter.setNdefPushMessage(msg, getActivity());
            mNfcAdapter.setOnNdefPushCompleteCallback(this, getActivity());
        }
    }

    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("ISO-8859-1"));
        return new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ticket_detail, container, false);

        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.ticket_detail)).setText(
                    Html.fromHtml("<b>Start station</b>: " + mItem.startStation + "<br>" +
                    "<b>End station</b>: " + mItem.endStation + "<br>" +
                    "<b>Bought date</b>: " + mItem.creationDate.replace('T', ' ').substring(0, 19))
            );

            Bitmap myBitmap = QRCode.from(String.format("%d|%s|%s", mItem.id, mItem.creationDate,
                    mItem.signature)).bitmap();
            ImageView myImage = (ImageView) rootView.findViewById(R.id.imageView);
            myImage.setImageBitmap(myBitmap);
        }

        return rootView;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity(), "Message sent.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
