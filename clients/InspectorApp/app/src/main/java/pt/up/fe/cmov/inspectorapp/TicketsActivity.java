package pt.up.fe.cmov.inspectorapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.common.base.Joiner;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
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
    static public RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static String string(ByteBuffer buf)
    {
        return new String(lenval(buf), Charset.forName("US-ASCII"));
    }

    private static BigInteger sshint(ByteBuffer buf)
    {
        return new BigInteger(+1, lenval(buf));
    }

    private static byte[] lenval(ByteBuffer buf)
    {
        int len = buf.getInt();
        byte[] copy = new byte[len];
        buf.get(copy);
        return copy;
    }

    static RSAPublicKeySpec decodeRSAPublicSSH(byte[] encoded)
    {
        ByteBuffer input = ByteBuffer.wrap(encoded);
        String type = string(input);
        if (!"ssh-rsa".equals(type))
            throw new IllegalArgumentException("Unsupported type");
        BigInteger exp = sshint(input);
        BigInteger mod = sshint(input);

        Log.d("Error", exp.toString());
        Log.d("Error", mod.toString());

        //if (input.hasRemaining())
        //    throw new IllegalArgumentException("Excess data");
        return new RSAPublicKeySpec(mod, exp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");

                String[] split = contents.split("\\|");
                int id = Integer.parseInt(split[0]);
                String date = split[1];
                String sign = split[2];

                try {
                    // String pubKey = "AAAAB3NzaC1yc2EAAAADAQABAAAAYQDMZpxocEcKPOPr9TRlA18vrqDdeHJHfYir5hQuUm+ledCSkGfa16KLL91AZJymIdCnaLWHAK214zI5qTKwZYdLfKK5IK/YenoDyokmn/SDroq1fprz1loeMVbDTHXjz9k=";
                    // pubKey = pubKey.replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)", "");

                    // byte[] keyBytes = android.util.Base64.decode(pubKey, Base64.NO_PADDING);
                    byte[] keyBytes = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x07, (byte)0x73, (byte)0x73, (byte)0x68, (byte)0x2D, (byte)0x72, (byte)0x73, (byte)0x61, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x01, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x61, (byte)0x00, (byte)0xCC, (byte)0x66, (byte)0x9C, (byte)0x68, (byte)0x70, (byte)0x47, (byte)0x0A, (byte)0x3C, (byte)0xE3, (byte)0xEB, (byte)0xF5, (byte)0x34, (byte)0x65, (byte)0x03, (byte)0x5F, (byte)0x2F, (byte)0xAE, (byte)0xA0, (byte)0xDD, (byte)0x78, (byte)0x72, (byte)0x47, (byte)0x7D, (byte)0x88, (byte)0xAB, (byte)0xE6, (byte)0x14, (byte)0x2E, (byte)0x52, (byte)0x6F, (byte)0xA5, (byte)0x79, (byte)0xD0, (byte)0x92, (byte)0x90, (byte)0x67, (byte)0xDA, (byte)0xD7, (byte)0xA2, (byte)0x8B, (byte)0x2F, (byte)0xDD, (byte)0x40, (byte)0x64, (byte)0x9C, (byte)0xA6, (byte)0x21, (byte)0xD0, (byte)0xA7, (byte)0x68, (byte)0xB5, (byte)0x87, (byte)0x00, (byte)0xAD, (byte)0xB5, (byte)0xE3, (byte)0x32, (byte)0x39, (byte)0xA9, (byte)0x32, (byte)0xB0, (byte)0x65, (byte)0x87, (byte)0x4B, (byte)0x7C, (byte)0xA2, (byte)0xB9, (byte)0x20, (byte)0xAF, (byte)0xD8, (byte)0x7A, (byte)0x7A, (byte)0x03, (byte)0xCA, (byte)0x89, (byte)0x26, (byte)0x9F, (byte)0xF4, (byte)0x83, (byte)0xAE, (byte)0x8A, (byte)0xB5, (byte)0x7E, (byte)0x9A, (byte)0xF3, (byte)0xD6, (byte)0x5A, (byte)0x1E, (byte)0x31, (byte)0x56, (byte)0xC3, (byte)0x4C, (byte)0x75, (byte)0xE3, (byte)0xCF, (byte)0xD9};

                    // generate public key
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    PublicKey publicKey = keyFactory.generatePublic(decodeRSAPublicSSH(keyBytes));

                    Signature sg = Signature.getInstance("SHA1WithRSA");
                    sg.initVerify(publicKey);
                    sg.update(Integer.toString(id).getBytes());
                    sg.verify(sign.getBytes());

                    Toast.makeText(this, "Ticked verified successfully", Toast.LENGTH_SHORT).show();

                    for (int i = 0; i < mTicketList.size(); i++) {
                        ApiService.Ticket t = mTicketList.get(i);
                        if (t.id == id) {
                            t.status = "validated";
                            ((TicketListAdapter)mAdapter).notifyItemChanged(i);
                        }
                    }

                } catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
            else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "QR Code scanning cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    static ArrayList<ApiService.Ticket> mTicketList = new ArrayList<>();

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

                try {
                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

                    startActivityForResult(intent, 0);
                } catch (Exception e) {
                    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
                    startActivity(marketIntent);
                }

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
                    final List<ApiService.Ticket> tickets = response.body();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mTicketList = new ArrayList<>(tickets);

                            ((TicketListAdapter)mAdapter).updateList(mTicketList);
                            Toast.makeText(TicketsActivity.this, "Loaded " + mTicketList.size() +
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
            holder.mTripsTextView.setText(Html.fromHtml("<b>Trips(s):</b> " +
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

        public void forceNotifyItemChanged(int position) {
            notifyItemChanged(position);
        }
    }
}
