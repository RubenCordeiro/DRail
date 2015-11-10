package pt.up.fe.cmov.inspectorapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

public class TicketsActivity extends AppCompatActivity {

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
    }
}
