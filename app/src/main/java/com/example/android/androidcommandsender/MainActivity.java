package com.example.android.androidcommandsender;

import android.Manifest.permission;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Created by jesus on 04/09/2018.
 */

public class MainActivity extends AppCompatActivity {

    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private TextView textView;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Comprobamos que el dispositivo tenga Android Marshmallow o superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Obtenemos un ejemplo de KeyguardManager y FingerprintManager
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            textView = (TextView) findViewById(R.id.textview);

            // Comprobar que el dispositivo tenga sensor de huellas
            if (!fingerprintManager.isHardwareDetected()){
                // Si no hay sensor de huellas se lo hacemos saber al usuario
                textView.setText("Su dispositivo no posee sensor de huella digital");

            }
            //Comprobamos que el usuario nos haya concedido el permiso USE_FINGERPRINT
            if (ActivityCompat.checkSelfPermission(this, permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
                // Si no han concedido el permiso que aparezca el siguiente texto
                textView.setText("Por favor, conceda el permiso de uso de huella digital");
            }
            // Comprobamos que el usuario tenga al menos una huella digital registrada en su dispositivo
            if (!fingerprintManager.hasEnrolledFingerprints()){
                // En caso de no tener ninguna huella registrada se lo hacemos saber al usuario
                textView.setText("Por favor, registre al menos una huella en su dispositivo");
            }
            // Comprobamos que el bloqueo de seguridad está activado en el dispositivo
            if (!keyguardManager.isKeyguardSecure()){
                //Si el usuario no posee ningún bloqueo de pantalla se lo hacemos saber
                textView.setText("Por favor, habilite el bloqueo de pantalla de seguridad en su dispositivo");
            } else {
                try {
                    generateKey();
                } catch (FingerprintException e){
                    e.printStackTrace();
                }

                if (initCipher()){
                    // Si el cifrado se ha realizado correctamente, creamos un CryptoObject
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    // Aquí hago uso de FingerprintManager, lo usaremos para el proceso de autenticación
                    FingerprintHandler helper = new FingerprintHandler(this);
                    helper.startAuth(fingerprintManager, cryptoObject);
                }
            }
        }
    }

    // Aquí generaremos la key
    private void generateKey() throws FingerprintException{
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            // Generamos la key
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            // Inicializamos una key vacía
            keyStore.load(null);
            // Inicializamos el key generator
            keyGenerator.init(new

                    // Especificamos los usos de esta key
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                    KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    // Configuramos la key para que el usuario tenga que utilizar su huella dactilar cada vez que quiera acceder
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            // Generamos la key
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | CertificateException
                | InvalidAlgorithmParameterException
                | IOException e){
            e.printStackTrace();
            throw new FingerprintException(e);
        }
    }

    // Creamos un nuevo método para inicializar nuestro Cipher
    public Boolean initCipher(){
        try {
            // obtenemos un ejemplo de Cipher y lo configuramos para que se adapte al bloqueo mediante huella digital
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchPaddingException
                | NoSuchAlgorithmException e) {
            throw new RuntimeException("Fallo al obtener el Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // return true si el Cipher se ha inicializado correctamente
            return true;
        } catch (KeyPermanentlyInvalidatedException e){

            // return false si el Cipher no se ha inicializado correctamente
            return false;


        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e){
            throw  new RuntimeException("Fallo al inicializar el Cipher", e);
             }
        }

        private class FingerprintException extends  Exception {
        public FingerprintException(Exception e){
            super(e);
        }
        }



    }

