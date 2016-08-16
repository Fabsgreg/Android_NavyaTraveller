package navya.tech.navyatraveller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import navya.tech.navyatraveller.Databases.MyDBHandler;
import navya.tech.navyatraveller.Fragments.AccountConnectedFragment;
import navya.tech.navyatraveller.Fragments.AccountDisconnectedFragment;
import navya.tech.navyatraveller.Fragments.GmapFragment;
import navya.tech.navyatraveller.Fragments.GoFragment;
import navya.tech.navyatraveller.Fragments.QRcodeFragment;



/**
 * Created by gregoire.frezet on 24/03/2016.
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    // Global variable
    private static final String ipAddress = "10.0.5.56";
    private static final Integer port = 3010;
    private static final Integer timeout = 10000;            // Timeout for database loading in millisecond
    private static final Float criticalLevel = 20.0F;

    // Data saving
    private static SavingResult mSavingResult;
    private static SavingAccount mSavingAccount;
    private static Socket mSocket;

    // Layout
    public NavigationView mNavigationView;       // Declared as public because other fragments may invoke to change the current view
    private DrawerLayout mDrawer;

    // Fragments
    private Fragment[] mFragmentList;
    private String[] mFragmentTAGS;
    private GmapFragment mFragGMap;
    private QRcodeFragment mFragQRCode;
    private GoFragment mFragGo;
    private AccountConnectedFragment mFragAccountConnected;
    private AccountDisconnectedFragment mFragAccountDisconnected;

    private Handler mTimeoutHandler;
    private Runnable mTimeoutProcess;
    private boolean isFirstConnection;

    // Database
    private boolean[] mDBloaded;
    private MyDBHandler mDBHandler;

    private boolean isNavViewBlocked;

    //
    ////////////////////////////////////////////////////  View Override /////////////////////////////////////////////////////////
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
                // Hide Keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            public void onDrawerClosed(View drawerView){
                super.onDrawerClosed(drawerView);
            }
        };

        if (mDrawer != null) {
            mDrawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
        }
        isNavViewBlocked = false;
        View header = mNavigationView.getHeaderView(0);
        TextView mPhoneNumber = (TextView) header.findViewById(R.id.phone_number);

        // Init DB & Data saving
        mDBHandler = new MyDBHandler(this);
        mDBloaded = new boolean[]{false, false};
        mSavingResult = new SavingResult();
        mSavingAccount = new SavingAccount();

        // Retrieve phone number
        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String myPhoneNumber = tMgr.getLine1Number();
/*        if (myPhoneNumber != "") {
            myPhoneNumber = "0123456789";
        }*/
        myPhoneNumber = "0123456789";
        mSavingAccount.setPhoneNumber(myPhoneNumber);
        mPhoneNumber.setText("Login : "+myPhoneNumber+"");

        // Internet connection checking
        mTimeoutProcess = new Runnable() {
            @Override
            public void run() {
                if (!mDBloaded[0] || !mDBloaded[1]) {
                    ShowDialogAndExit("Your internet connection is not available", "Please, check your network before to launch the app");
                }
                else if (getBatteryLevel() < criticalLevel) {
                    ShowDialogAndExit("Low level battery", "Please, reload your smartphone over " + criticalLevel + "% to use this app");
                }
                else {
                    onNavigationItemSelected(mNavigationView.getMenu().getItem(0).setChecked(true));
                }
                mTimeoutHandler.removeCallbacks(mTimeoutProcess);
            }
        };
        mTimeoutHandler = new Handler();
        mTimeoutHandler.postDelayed(mTimeoutProcess, timeout);

        // Socket.IO init
        isFirstConnection = true;
        try {

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            IO.setDefaultSSLContext(sc);
            HttpsURLConnection.setDefaultHostnameVerifier(new RelaxedHostNameVerifier());

            // socket options
            IO.Options opts = new IO.Options();
           // opts.forceNew = true;
            //opts.reconnection = true;
            opts.secure = true;
            opts.sslContext = sc;
            mSocket = IO.socket("https://"+MainActivity.ipAddress+":"+port+"",opts);

            mSocket.connect();
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            mSocket.on("stationReceived", onStationReceived);
            mSocket.on("lineReceived", onlineReceived);
            mSocket.on("accountDataUpdated", onAccountDataUpdated);
            UpdateAccountData();

        }
        catch (URISyntaxException ignored) {}
        catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        // Fragment init
        mFragGMap = new GmapFragment();
        mFragQRCode = new QRcodeFragment();
        mFragGo = new GoFragment();
        mFragAccountConnected = new AccountConnectedFragment();
        mFragAccountDisconnected = new AccountDisconnectedFragment();
        mFragmentList = new Fragment[]{mFragGMap, mFragGo, mFragQRCode, mFragAccountConnected, mFragAccountDisconnected};
        mFragmentTAGS = new String[]{"Map", "Go","QR code", "Account Connected", "Account Disconnected"};
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up mybutton, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int position = 0;

        if (mSavingResult.getTravelling() && !isNavViewBlocked) {
            isNavViewBlocked = true;
            onNavigationItemSelected(mNavigationView.getMenu().getItem(0).setChecked(true));
            return false;
        }
        else if (isNavViewBlocked) {
            isNavViewBlocked = false;
            mFragGMap.ShowMyDialog("Warning","End your travel before to switch between Tabs");
        }
        else {
            switch (item.toString())
            {
                case "Map" :
                    position = 0;
                    break;
                case  "Go" :
                    position = 1;
                    break;
                case  "QR code" :
                    position = 2;
                    break;
                case  "Account" :
                    if (mSavingAccount.getConnected()) {
                        position = 3;
                    }
                    else {
                        position = 4;
                    }
                    break;
            }
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        // Add the mFragmentList only once if array haven't fragment
        if (getSupportFragmentManager().findFragmentByTag(mFragmentTAGS[position]) == null) {
            fragmentTransaction.add(R.id.content_frame, mFragmentList[position], mFragmentTAGS[position]);
        }

        // Hiding & Showing mFragmentList
        for(int catx = 0; catx< mFragmentList.length; catx++)
        {
            if(catx == position) {

                fragmentTransaction.show(mFragmentList[catx]);
                if ((mFragmentList[catx] == mFragGMap) && (!mSavingResult.getGmap())) {
                    mFragGMap.Update();
                }
                else if (mFragmentList[catx] == mFragQRCode) {
                    // Reset fragment
                    fragmentTransaction.detach(mFragmentList[catx]);
                    fragmentTransaction.attach(mFragmentList[catx]);
                }
            }
            else {
                fragmentTransaction.hide(mFragmentList[catx]);
            }
        }
        fragmentTransaction.commit();

        mDrawer.closeDrawer(GravityCompat.START);

        return true;
    }

    //
    ////////////////////////////////////////////////////  Miscellaneous functions   /////////////////////////////////////////////////////////
    //

    private void ShowDialogAndExit (String title, String text) {
        Context context = this;
        AlertDialog ad = new AlertDialog.Builder(context).create();
        ad.setCancelable(false);
        ad.setTitle(title);
        ad.setMessage(text);
        ad.setButton(-1, "OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                System.exit(0);
            }
        });
        ad.show();
    }

    public static void UpdateAccountData () {
        JSONObject request = new JSONObject();
        try {
            request.put("phone_number", mSavingAccount.getPhoneNumber());
            mSocket.emit("updateAccountData",request);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }

    //
    ////////////////////////////////////////////////////  Getter   /////////////////////////////////////////////////////////
    //

    public static Socket getSocket () { return mSocket; }

    public static SavingAccount getSavingAccount () { return mSavingAccount; }

    public static SavingResult getSavingResult () { return mSavingResult; }

    //
    ////////////////////////////////////////////////////  Socket.IO events   /////////////////////////////////////////////////////////
    //

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("state",""+isFirstConnection+"");
                    if (isFirstConnection){
                        mDBHandler.Reset();
                        mSocket.emit("lineRequest");
                        isFirstConnection = false;
                    }

                    JSONObject request = new JSONObject();
                    try {
                        request.put("phone_number", mSavingAccount.getPhoneNumber());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("nameUpdate",request);
                    mSavingAccount.setInternetAvailable(true);
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSavingAccount.setInternetAvailable(false);
                }
            });
        }
    };

    private Emitter.Listener onStationReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray stations = (JSONArray) args[0];
                        for (int i=0; i < stations.length(); i++) {
                            JSONObject station = stations.getJSONObject(i);

                            String stationName = station.getString("name");
                            Double latitude = station.getDouble("latitude");
                            Double longitude = station.getDouble("longitude");
                            String lineName = station.getString("line_name");

                            mDBHandler.createStation(stationName, latitude.floatValue(), longitude.floatValue(), lineName);
                        }
                        mDBloaded[1] = true;
                        mTimeoutHandler.post(mTimeoutProcess);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onlineReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray lines = (JSONArray) args[0];
                        for (int i=0; i < lines.length(); i++) {
                            JSONObject line = lines.getJSONObject(i);

                            mDBHandler.createLine(line.getString("name"));
                        }
                        mDBloaded[0] = true;
                        mSocket.emit("stationRequest");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };

    private Emitter.Listener onAccountDataUpdated = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray dataArray = (JSONArray) args[0];
                        JSONObject data = dataArray.getJSONObject(0);
                        mSavingAccount.setFirstName(data.getString("first_name"));
                        mSavingAccount.setLastName(data.getString("last_name"));
                        mSavingAccount.setEmail(data.getString("email"));
                        mSavingAccount.setPassword(data.getString("password"));
                        mSavingAccount.setDuration(data.getDouble("duration"));
                        mSavingAccount.setDistance(data.getDouble("distance"));
                        mSavingAccount.setNbrTravel(data.getInt("nbr_travel"));
                        mSavingAccount.setJourneyAborted(data.getInt("penalization"));
                        mSavingAccount.setConnected(data.getInt("state") != 0);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //
    ////////////////////////////////////////////////////  SSL communication   /////////////////////////////////////////////////////////
    //

    private TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    } };

    public static class RelaxedHostNameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

}


