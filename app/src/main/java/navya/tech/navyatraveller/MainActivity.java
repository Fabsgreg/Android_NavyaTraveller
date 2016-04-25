package navya.tech.navyatraveller;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import navya.tech.navyatraveller.Databases.MyDBHandler;
import navya.tech.navyatraveller.Fragments.AccountConnectedFragment;
import navya.tech.navyatraveller.Fragments.AccountDisconnectedFragment;
import navya.tech.navyatraveller.Fragments.GmapFragment;
import navya.tech.navyatraveller.Fragments.GoFragment;
import navya.tech.navyatraveller.Fragments.QRcodeFragment;
import navya.tech.navyatraveller.Fragments.SignInFragment;
import navya.tech.navyatraveller.Fragments.SignUpFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Global variable
    public static String ipAddress = "10.0.20.72";
    public static Integer timeout = 10000;              // Timeout for database loading in millisecond
    public static SaveResult savingData;
    public static SaveAccount savingAccount;
    public static Socket mSocket;

    private Fragment[] fragments;
    private String[] fragmentTAGS;
    public NavigationView navigationView;

    private GmapFragment fragGMap;
    private QRcodeFragment fragQRCode;
    private AccountConnectedFragment fragAccountConnected;
    private AccountDisconnectedFragment fragAccountDisconnected;

    private Handler handler;
    private Runnable timeoutProcess;

    private boolean[] DBloaded;
    private MyDBHandler mDBHandler;

    private boolean isNavViewBlocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
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

        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        mDBHandler = new MyDBHandler(this);

        DBloaded = new boolean[]{false, false};

        savingData = new SaveResult();

        savingAccount = new SaveAccount();

        fragGMap = new GmapFragment();
        fragQRCode = new QRcodeFragment();
        GoFragment fragGo = new GoFragment();
        fragAccountConnected = new AccountConnectedFragment();
        fragAccountDisconnected = new AccountDisconnectedFragment();


        fragments = new Fragment[]{fragGMap, fragGo, fragQRCode, fragAccountConnected, fragAccountDisconnected};
        fragmentTAGS = new String[]{"Map", "Go","QR code", "Account Connected", "Account Disconnected"};

        timeoutProcess = new Runnable() {
            @Override
            public void run() {
                if (DBloaded[0] && DBloaded[1]) {
                    onNavigationItemSelected(navigationView.getMenu().getItem(0).setChecked(true));
                }
                else {
                    ShowMyDialog("Your internet connection is not available", "Please, check your network before to launch the app");
                }
                handler.removeCallbacks(timeoutProcess);
            }
        };

        View header=navigationView.getHeaderView(0);
        TextView mPhoneNumber = (TextView) header.findViewById(R.id.phone_number);

        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String myPhoneNumber = tMgr.getLine1Number();

        if (myPhoneNumber == null) {
            myPhoneNumber = "0123456789";
        }

        mPhoneNumber.setText("Login : "+myPhoneNumber+"");

        savingAccount.setPhoneNumber(myPhoneNumber);

        isNavViewBlocked = false;

        handler = new Handler();
        handler.postDelayed(timeoutProcess, timeout);

        try {
            mSocket = IO.socket("http://"+MainActivity.ipAddress+":3001");
            mSocket.connect();
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on("stationReceived", onStationReceived);
            mSocket.on("lineReceived", onlineReceived);
            mSocket.on("accountDataUpdated", onAccountDataUpdated);
            UpdateAccountData();

        } catch (URISyntaxException e) {}
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

        if (savingData.getIsTravelling() && !isNavViewBlocked) {
            isNavViewBlocked = true;
            onNavigationItemSelected(navigationView.getMenu().getItem(0).setChecked(true));
            return false;
        }
        else if (isNavViewBlocked) {
            isNavViewBlocked = false;
            fragGMap.ShowMyDialog("Warning","End your travel before to switch between Tabs");
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
                    if (savingAccount.getConnected()) {
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

        // Add the fragments only once if array haven't fragment
        if (getSupportFragmentManager().findFragmentByTag(fragmentTAGS[position]) == null) {
            fragmentTransaction.add(R.id.content_frame, fragments[position], fragmentTAGS[position]);
        }

        // Hiding & Showing fragments
        for(int catx=0;catx<fragments.length;catx++)
        {
            if(catx == position) {

                fragmentTransaction.show(fragments[catx]);
                if ((fragments[catx] == fragGMap) && (!savingData.getWasGmap())) {
                    fragGMap.Update();
                }
                else if (fragments[catx] == fragQRCode) {
                    // Reset fragment
                    fragmentTransaction.detach(fragments[catx]);
                    fragmentTransaction.attach(fragments[catx]);
                }
            }
            else {
                fragmentTransaction.hide(fragments[catx]);
            }
        }
        fragmentTransaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    //
    ////////////////////////////////////////////////////  Miscellaneous functions   /////////////////////////////////////////////////////////
    //

    private void ShowMyDialog (String title, String text) {
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
            request.put("phone_number", savingAccount.getPhoneNumber());
            mSocket.emit("updateAccountData",request);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //
    ////////////////////////////////////////////////////  Socket.IO events   /////////////////////////////////////////////////////////
    //

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDBHandler.Reset();
                    JSONObject request = new JSONObject();
                    try {
                        request.put("phone_number", savingAccount.getPhoneNumber());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("nameUpdate",request);
                    mSocket.emit("lineRequest");
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
                        DBloaded[1] = true;
                        handler.post(timeoutProcess);

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
                        DBloaded[0] = true;
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
                        savingAccount.setFirstName(data.getString("first_name"));
                        savingAccount.setLastName(data.getString("last_name"));
                        savingAccount.setEmail(data.getString("email"));
                        savingAccount.setPassword(data.getString("password"));
                        savingAccount.setDuration(data.getDouble("duration"));
                        savingAccount.setDistance(data.getDouble("distance"));
                        savingAccount.setNbrTravel(data.getInt("nbr_travel"));
                        savingAccount.setTripAborted(data.getInt("penalization"));
                        savingAccount.setConnected(data.getInt("state") != 0);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

}
