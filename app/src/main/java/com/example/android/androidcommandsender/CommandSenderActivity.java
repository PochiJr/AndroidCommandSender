package com.example.android.androidcommandsender;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class CommandSenderActivity extends AppCompatActivity {

    public String host = "192.168.1.46";
    public String user = "idiota";
    public String password = "1234";

    private class connectAsyncTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground (String... cmd){
            // Se conecta al servidor SSH.
            StringBuilder output = new StringBuilder();
            try {
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                JSch jSch = new JSch();
                Session session = jSch.getSession(user, host, 22);
                session.setPassword(password);
                session.setConfig(config);
                session.connect();
                System.out.println("Connected");

                Channel channel = session.openChannel("exec");
                ((ChannelExec)channel).setCommand(cmd[0]);
                channel.setInputStream(null);
                ((ChannelExec)channel).setErrStream(System.err);

                InputStream in = channel.getInputStream();
                channel.connect();


                // Obtenemos la respuesta del servidor, más que nada  por debug, si te sobra lo quitas
                if (in != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(in, Charset.forName("UTF-8"));
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line = reader.readLine();
                    while (line != null){
                        output.append(line);
                        line = reader.readLine();
                    }
                }
                channel.disconnect();
                session.disconnect();
                System.out.println("DONE");

            } catch (Exception e){
                e.printStackTrace();
            }
            return output.toString();
        }

    }

    public void comando1(View v) {
        connectAsyncTask task = new connectAsyncTask();
        task.execute("ipconfig");
    }
    public void comando2(View v) {
        connectAsyncTask task = new connectAsyncTask();
        task.execute("cmd /c echo Soy magi");
    }
    public void comando3(View v) {
        connectAsyncTask task = new connectAsyncTask();
        task.execute("help");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.security_activity);

    }
}
