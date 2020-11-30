package cz.fmo.display;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONObject;

import cz.fmo.R;

public class MatchInitFragment extends Fragment implements DisplayConnectCallback, View.OnClickListener {
    private static final String TAG_MATCH_SCORE = "MATCH_SCORE";
    private FragmentReplaceCallback callback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_match_init, container, false);
        ImageView imageView = v.findViewById(R.id.qr_code);
        createQRCode(imageView);
        v.findViewById(R.id.miPictureBtn).setOnClickListener(this);
        ((MatchActivity)getActivity()).getPubNub().setDisplayConnectCallback(this);
        return v;
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
    public void onImageReceived(byte[] imageBytes) {
        Fragment fragment = new MatchScoreFragment();
        Bundle bundle = new Bundle();
        //bundle.putString("imageUrl", jsonObject.url oderso);
        fragment.setArguments(bundle);
        callback.replaceFragment(fragment, TAG_MATCH_SCORE);
    }

    @Override
    public void onConnected() {
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
    public void onClick(View v) {
        ((MatchActivity) getActivity()).getPubNub().onRequestTableFrame();
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
            e.printStackTrace();
        }
    }
}
