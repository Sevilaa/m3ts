package ch.m3ts.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import ch.m3ts.MainActivity;
import cz.fmo.R;

public class QuitAlertDialogHelper {

    private QuitAlertDialogHelper() {
    }

    public static AlertDialog makeDialog(final Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(ctx.getString(R.string.quitMatchTitle));
        builder.setMessage(ctx.getString(R.string.quitMatchMessage));
        builder.setPositiveButton(R.string.quitMatchProceed, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                ctx.startActivity(new Intent(ctx, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        builder.setNegativeButton(R.string.quitMatchCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        return builder.create();
    }
}
