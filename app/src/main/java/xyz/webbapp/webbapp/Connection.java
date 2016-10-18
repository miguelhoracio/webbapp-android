package xyz.webbapp.webbapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Descripcion: Clase que se debe usar para realizar conexiones al servicio
 * Autor: Sergio Cruz
 * Fecha: 2016-10-17
 **/

public class Connection extends AsyncTask<Void, Void, String>
{
    private Context myContext;
    private final String Url = "http://www.webbapp.xyz/service.php";
    private String response;
    private byte[] postDataBytes;

    public Connection (Context context, Map<String, Object> parametros)
    {
        this.myContext = context;
        try
        {
            this.postDataBytes = postData(parametros);
        }
        catch (Exception ex)
        {
            this.postDataBytes = null;
        }
    }

    public Connection (Context context, byte[] postDataBytes)
    {
        this.myContext = context;
        this.postDataBytes = postDataBytes;
    }

    @Override
    protected String doInBackground(Void... Params)
    {
        String result;

        // Se valida que haya internet

        ConnectivityManager connMgr = (ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
        {
            try
            {
                result = goConnect(this.Url);
            }
            catch (IOException ex)
            {
                return "bad_url";
            }
        }
        else
        {
            return "no_internet";
        }

        return result;

    }

    protected byte[] postData(Map<String, Object> parametros) throws IOException
    {
        StringBuilder postData = new StringBuilder();

        for (Map.Entry<String,Object> param : parametros.entrySet())
        {
            if (postData.length() != 0)
            {
                postData.append('&');
            }
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }

        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        return  postDataBytes;
    }

    @Override
    protected void onPostExecute(final String data)
    {
        // Si doInBackground obtuvo informacion se pasa a la actividad home
        if (!data.isEmpty())
        {
            // Guardamos el reponse en la clase;
            this.response = data;
        }
        else
        {
            Toast.makeText(myContext, R.string.no_data, Toast.LENGTH_SHORT).show();
        }
    }

    protected  String goConnect(String myurl) throws IOException
    {
        InputStream is = null;

        try
        {
            URL url = new URL(myurl);
            byte[] postDataBytes = this.postDataBytes;
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "charset", "utf-8");
            conn.setRequestProperty( "Content-Length", String.valueOf(postDataBytes.length));
            conn.getOutputStream().write(postDataBytes);
            Log.d("TAG", conn.getOutputStream().toString());

            // Se inicia la conexion
            conn.connect();

            int response = conn.getResponseCode();
            Log.d("TAG", "The response is: " + response); // 200 es conexion correcta
            is = conn.getInputStream();

            // readIt convierte el input stream a string
            String contentAsString = readIt(is);
            return contentAsString;

            // Se cierra la conexion
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }

    //Lee el input stream y lo regresa como string.
    protected String readIt(InputStream stream) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String input = "";
        while((input = reader.readLine()) != null)
        {
            sb.append(input);
        }
        return sb.toString();
    }
}
