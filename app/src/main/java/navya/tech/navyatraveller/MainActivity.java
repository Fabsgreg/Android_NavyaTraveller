package navya.tech.navyatraveller;

import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import navya.tech.navyatraveller.Databases.MyDBHandler;
import navya.tech.navyatraveller.Fragments.GmapFragment;
import navya.tech.navyatraveller.Fragments.GoFragment;
import navya.tech.navyatraveller.Fragments.QRcodeFragment;
import navya.tech.navyatraveller.Fragments.ToolFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment[] fragments;
    private String[] fragmentTAGS;
    private NavigationView navigationView;

    private Handler handler;
    private Runnable toto;

    private boolean[] DBloaded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final MyDBHandler mDBHandler = new MyDBHandler(this);
        mDBHandler.Reset();

        DBloaded = new boolean[]{false, false};


        GmapFragment fragGMap = new GmapFragment();
        QRcodeFragment fragQRCode = new QRcodeFragment();
        GoFragment fragGo = new GoFragment();
        ToolFragment fragTool = new ToolFragment();

        fragments = new Fragment[]{fragQRCode, fragGMap, fragGo, fragTool};
        fragmentTAGS = new String[]{"QR code","Map","Go","Tools"};

        phpLineRequest(mDBHandler);

        toto = new Runnable() {
            @Override
            public void run() {
                if (DBloaded[0] && DBloaded[1]) {
                    navigationView.getMenu().getItem(0).setChecked(true);
                    onNavigationItemSelected(navigationView.getMenu().getItem(0));
                }
                else {
                    ShowMyDialog("Your internet connection is not available", "Please, check your network before to launch the app");
                }
            }
        };

        handler = new Handler();
        handler.postDelayed(toto, 6000);
    }


    public void phpLineRequest (final MyDBHandler myDB) {
        /// PHP request
        String showLine = "http://10.0.20.34/navyaTraveller/showLine.php";
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, showLine
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray lines = response.getJSONArray("lines");
                    for (int i=0; i < lines.length(); i++) {
                        JSONObject line = lines.getJSONObject(i);

                        myDB.createLine(line.getString("name"));
                    }
                    DBloaded[0] = true;
                    phpStationRequest(myDB);
                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        }       , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }

        });
        requestQueue.add(jsonObjectRequest);
    }

    public void phpStationRequest (final MyDBHandler myDB) {
        /// PHP request
        String showStation = "http://10.0.20.34/navyaTraveller/showStation.php";
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, showStation
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray stations = response.getJSONArray("stations");
                    for (int i=0; i < stations.length(); i++) {
                        JSONObject station = stations.getJSONObject(i);

                        String stationName = station.getString("name");
                        Double latitude = station.getDouble("latitude");
                        Double longitude = station.getDouble("longitude");
                        String lineName = station.getString("line_name");

                        myDB.createStation(stationName, latitude.floatValue(), longitude.floatValue(), lineName);
                    }
                    DBloaded[1] = true;
                    handler.post(toto);

                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        }       , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }

        });
        requestQueue.add(jsonObjectRequest);
    }


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


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        int position = 0;

        switch (item.toString())
        {
            case "QR Code" :
                position = 0;
                break;
            case  "Map" :
                position = 1;
                break;
            case  "Go" :
                position = 2;
                break;
            case  "Tools" :
                position = 3;
                break;
        }

        // Add the fragments only once if array haven't fragment
        if (getSupportFragmentManager().findFragmentByTag(fragmentTAGS[position]) == null) {
            fragmentTransaction.add(R.id.content_frame, fragments[position], fragmentTAGS[position]);
        }

        // Hiding & Showing fragments
        for(int catx=0;catx<fragments.length;catx++)
        {
            if(catx == position) {
                fragmentTransaction.show(fragments[catx]);
            }
            else {
                fragmentTransaction.hide(fragments[catx]);
            };
        };
        fragmentTransaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


}
