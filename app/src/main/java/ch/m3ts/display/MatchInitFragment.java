package ch.m3ts.display;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import ch.m3ts.Log;
import ch.m3ts.connection.ConnectionCallback;
import ch.m3ts.connection.NearbyDisplayConnection;
import cz.fmo.R;
import cz.fmo.util.Config;

/**
 * Fragment which generates and displays a QR-Code based on the selected table tennis
 * match settings and a randomly generated PubNub room id. The QR-Code is displayed for the
 * tracking device.
 */
public class MatchInitFragment extends Fragment implements DisplayConnectCallback, View.OnClickListener, ConnectionCallback {
    private static final String TAG_MATCH_SELECT_CORNERS = "MATCH_SELECT_CORNERS";
    private FragmentReplaceCallback callback;
    private NearbyDisplayConnection nearbyDisplayConnection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_match_init, container, false);
        initConnection(v);
        v.findViewById(R.id.miPictureBtn).setOnClickListener(this);
        return v;
    }

    private void initConnection(View v) {
        Config mConfig = new Config(getContext());
        if(mConfig.isUsingPubnub()) {
            ((MatchActivity)getActivity()).getConnection().setDisplayConnectCallback(this);
            ImageView imageView = v.findViewById(R.id.qr_code);
            createQRCode(imageView);
        } else {
            ((TextView) v.findViewById(R.id.miSubTitle)).setText(getString(R.string.connectDisplaySearching));
            this.nearbyDisplayConnection = ((MatchActivity)getActivity()).getNearbyDisplayConnection();
            this.nearbyDisplayConnection.setDisplayConnectCallback(this);
            this.nearbyDisplayConnection.setConnectCallback(this);
            this.nearbyDisplayConnection.startAdvertising();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(this.nearbyDisplayConnection != null) {
            this.nearbyDisplayConnection.setDisplayConnectCallback(null);
            this.nearbyDisplayConnection.setConnectCallback(null);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = (FragmentReplaceCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FragmentReplaceListener");
        }
    }

    @Override
    public void onImageReceived(byte[] imageBytes, int imageWidth, int imageHeight) {
        Fragment fragment = new MatchSelectCornerFragment();
        Bundle bundle = new Bundle();
        bundle.putByteArray("tableFrame", imageBytes);
        bundle.putInt("width", imageWidth);
        bundle.putInt("height", imageHeight);
        fragment.setArguments(bundle);
        callback.replaceFragment(fragment, TAG_MATCH_SELECT_CORNERS);
    }

    @Override
    public void onConnected() {
        initTakePicture();
    }

    private void initTakePicture() {
        final Activity activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)activity.findViewById(R.id.miTitle)).setText(R.string.miConnectedTitle);
                ((TextView)activity.findViewById(R.id.miSubTitle)).setText(R.string.miConnectedSubTitle);
                activity.findViewById(R.id.qr_code).setVisibility(View.GONE);
                activity.findViewById(R.id.miPictureBtn).setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onImageTransmissionStarted(int parts) {
        ProgressBar bar = getActivity().findViewById(R.id.loading_bar);
        bar.setMax(parts);
        bar.setProgress(0);
    }

    @Override
    public void onImagePartReceived(int partNumber) {
        ProgressBar bar = getActivity().findViewById(R.id.loading_bar);
        bar.setProgress(partNumber);
    }

    @Override
    public void onClick(View v) {
        Activity activity = getActivity();
        ((TextView)activity.findViewById(R.id.miSubTitle)).setText(R.string.miPictureLoadingSubTitle);
        activity.findViewById(R.id.display_loading).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.miPictureBtn).setVisibility(View.GONE);
        ((MatchActivity) getActivity()).getConnection().onRequestTableFrame();
        v.setOnClickListener(null);
    }

    private void createQRCode(ImageView imageView) {
        String qr = ((MatchActivity)getActivity()).getPubNub().getRoomID() + ";" + getArguments().getString("type") + ";" + getArguments().getString("server");
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(qr, BarcodeFormat.QR_CODE, 700, 700);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.setHasAlpha(true);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.TRANSPARENT);
                }
            }
            imageView.setImageBitmap(bmp);
        } catch (WriterException e) {
            Log.d(e.getMessage());
        }
    }

    @Override
    public void onDiscoverFailure() {
        setConnectInfoText(R.string.connectDiscoverFailure);
    }

    @Override
    public void onRejection() {
        setConnectInfoText(R.string.connectRejection);
    }

    @Override
    public void onDisconnection(String endpoint) {
        setConnectInfoText(R.string.connectDisconnect);
    }

    @Override
    public void onConnection(String endpoint) {
        initTakePicture();
    }

    @Override
    public void onConnecting(String endpoint) {

    }

    private void setConnectInfoText(int stringId) {
        ((TextView) getActivity().findViewById(R.id.miSubTitle)).setText(getString(stringId));
    }
}
