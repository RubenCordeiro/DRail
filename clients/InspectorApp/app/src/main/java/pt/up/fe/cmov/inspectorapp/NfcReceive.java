package pt.up.fe.cmov.inspectorapp;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;

public class NfcReceive extends Activity {
    TextView tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_receive);
        tv = (TextView) findViewById(R.id.textView1);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        int id = Integer.parseInt(new String(msg.getRecords()[0].getPayload()));
        tv.setText("Validated ticket #" + id);

        // TicketsActivity act = (TicketsActivity) getParent();
        for (int i = 0; i < TicketsActivity.mTicketList.size(); i++) {
            ApiService.Ticket t = TicketsActivity.mTicketList.get(i);
            if (t.id == id) {
                t.status = "validated";
                ((TicketsActivity.TicketListAdapter)TicketsActivity.mAdapter).notifyItemChanged(i);
            }
        }
    }
}
