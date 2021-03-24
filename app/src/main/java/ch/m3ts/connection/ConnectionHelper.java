package ch.m3ts.connection;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.PayloadCallback;

import cz.fmo.R;

public class ConnectionHelper {
    public static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
    public static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    public static final String SERVICE_ID = "M3TS_NEARBY";
    public static ConnectionsClient CONNECTIONS_CLIENT;

    private ConnectionHelper() { throw new IllegalStateException("Utility class");}

    public static AlertDialog makeAuthenticationDialog(final Context ctx, final String enpointId, String endpointName, String token, final PayloadCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(String.format(ctx.getString((R.string.connectTitle)), endpointName));
        builder.setMessage(ctx.getString(R.string.connectMessage) + token);
        builder.setPositiveButton(
                ctx.getString(R.string.connectConfirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Nearby.getConnectionsClient(ctx)
                                .acceptConnection(enpointId, callback);
                    }
                });
        builder.setNegativeButton(
                ctx.getString(R.string.connectReject),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Nearby.getConnectionsClient(ctx).rejectConnection(enpointId);
                    }
                });
        return builder.create();

    }

    public static AlertDialog makeDisconnectDialog(final Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(ctx.getString((R.string.disconnectTitle)));
        builder.setMessage(ctx.getString(R.string.disconnectMessage));
        builder.setPositiveButton(
                ctx.getString(R.string.connectConfirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO implement reconnect
                    }
                });
        return builder.create();
    }

    public static void createConnection(Context context) {
        CONNECTIONS_CLIENT = Nearby.getConnectionsClient(context.getApplicationContext());
    }
}