package com.example.android.androidcommandsender;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by jesus on 04/09/2018.
 */
@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    // Usamos CancellationSignal para que una vez fuera de la App otras apps puedan usar el sensor de huellas sin problemas

    private CancellationSignal cancellationSignal;
    private Context context;

    public FingerprintHandler(Context mContext) {
        context = mContext;
    }

    // Implementamos startAuth ya que es el encargado de realizar la autenticación de la huella digital
    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    // onAuthtenticationError es llamado cuando ocurre un error fatal, y nos proporciona información sobre este
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Toast.makeText(context, "Error de autenticación\n" + errString, Toast.LENGTH_SHORT).show();
    }

    @Override
    // onAuthenticationHelp es llamado cuando ocurre un error no fatal, y nos proporciona información sobre este
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        Toast.makeText(context, "Ayuda de autenticación:\n " + helpString, Toast.LENGTH_SHORT).show();
    }

    @Override
    // onAuthenticationSucceeded es llamado cuando la huella utilizada coincide con alguna de la sregistradas en el dispositivo
    public void onAuthenticationSucceeded(AuthenticationResult result) {
        Toast.makeText(context, "Autenticación realizada con éxito", Toast.LENGTH_SHORT).show();

        context.startActivity(new Intent(context, CommandSenderActivity.class));

    }

    @Override
    // onAuthenticationFailed es llamado cuando la huela dactilar utilizada no coincide con ninguna de las registradas en el dispositivo
    public void onAuthenticationFailed() {
        Toast.makeText(context, "La huella no coincide con ninguna de las registradas", Toast.LENGTH_SHORT).show();
    }
}
