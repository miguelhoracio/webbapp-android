package xyz.webbapp.webbapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Descripcion: Activity de log in de la aplicacion
 * Autor: Sergio Cruz
 * Fecha: 2016-10-08
 **/

public class Login extends AppCompatActivity implements LoaderCallbacks<Cursor>
{
    private UserLoginTask mAuthTask = null;
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Se liga la vista

        setContentView(R.layout.activity_login);

        // Se instancian los controles

        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        Button btnLogIn = (Button) findViewById(R.id.email_sign_in_button);
        btnLogIn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    // Intenta validar al usuario

    private void attemptLogin()
    {
        if (mAuthTask != null)
        {
            return;
        }

        // Resetea los controles
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Obtiene los valores de los controles
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Validala contraseña
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Valida el correo
        if (TextUtils.isEmpty(email))
        {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        else if (!isEmailValid(email))
        {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // Si hay un error en la validacion de la contraseña no se intenta logear
            focusView.requestFocus();
        }
        else
        {
            // Se muestra el spinner mientras se crea la asynctask para traer la informacion del server
            showProgress(true);
            mAuthTask = new UserLoginTask(this, email, password);
            mAuthTask.execute();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Logica del correo
        return email.contains("@");
    }

    private boolean isPasswordValid(String password)
    {
        //TODO: Logica de la contraseña
        return password.length() > 4;
    }

    // Animacion del proceso

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
        else
        {
            // Si la version de android no soporta la animacion solo se oculta y muestra lo que importa.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Descripcion: Valida a el usuario y se trae la informacion del webservice.
     * Autor: Sergio Cruz
     * Fecha: 2016-10-08
     **/

    // Las asynctask tienen preexecute, execute (doInBackground) y postexecute, se pasan sus returns
    public class UserLoginTask extends AsyncTask<Void, Void, String>
    {
        private Context myContext;
        private final String Url = "http://www.webbapp.xyz/service.php";
        private final String mEmail;
        private final String mPassword;

        //Constructor
        UserLoginTask(Context context, String email, String password)
        {
            myContext = context;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... Params)
        {

            String result = "";

            // Se valida que haya internet

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected())
            {
                try
                {
                    result = validateUser(this.Url);
                }
                catch (IOException e)
                {
                    // Asi se manda un manda un toast desde una AsyncTask a la Actividad
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(myContext, R.string.bad_url, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            else
            {
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(myContext, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            return result;
        }

        @Override
        protected void onPostExecute(final String data)
        {
            mAuthTask = null;
            showProgress(false);

            // Si doInBackground obtuvo informacion se pasa a la actividad home
            if (!data.isEmpty())
            {
                // Con putExtra pasamos info en el intent
                Intent intent = new Intent(myContext, Home.class);
                intent.putExtra("webData", data);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(myContext, R.string.no_data, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled()
        {
            mAuthTask = null;
            showProgress(false);
        }


        //Se conecta al webservice e intenta obtener la info del usuario

        protected String validateUser(String myurl) throws IOException
        {
            InputStream is = null;

            try
            {
                URL url = new URL(myurl);
                Map<String, Object> parametros = new LinkedHashMap<>();
                parametros.put("request", "login");
                parametros.put("email", this.mEmail);
                parametros.put("pwd", this.mPassword);
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

                // Despues de que se hace el request se convierte a bytes, y se manda su length

                byte[] postDataBytes = postData.toString().getBytes("UTF-8");
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
}

