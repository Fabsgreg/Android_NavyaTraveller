package navya.tech.navyatraveller;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import navya.tech.navyatraveller.Databases.MyDBHandler;
import navya.tech.navyatraveller.Fragments.GmapFragment;
import navya.tech.navyatraveller.Fragments.GoFragment;
import navya.tech.navyatraveller.Fragments.QRcodeFragment;
import navya.tech.navyatraveller.Fragments.ToolFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private MyDBHandler mDBHandler;

    private QRcodeFragment fragQRCode;
    private GmapFragment fragGMap;
    private GoFragment fragGo;
    private ToolFragment fragTool;

    private Fragment[] fragments;
    private String[] fragmentTAGS;

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        mDBHandler = new MyDBHandler(this);
        mDBHandler.Reset();
        mDBHandler.createLine("Line1");
        mDBHandler.createStation("Station1", (float) 48.890354, (float) 2.353866, "Line1");
        mDBHandler.createStation("Station2", (float) 48.888657, (float) 2.355598, "Line1");
        mDBHandler.createStation("Station3", (float) 48.889298, (float) 2.351637, "Line1");

        mDBHandler.createLine("Line2");
        mDBHandler.createStation("StationA", (float) 48.893595, (float) 2.354017, "Line2");
        mDBHandler.createStation("StationB", (float) 48.894778, (float) 2.352902, "Line2");
        mDBHandler.createStation("StationC", (float)48.894535, (float)2.351229, "Line2");
        mDBHandler.createStation("StationD", (float)48.893781, (float)2.351301, "Line2");
        mDBHandler.createStation("StationE", (float)48.893425, (float)2.352704, "Line2");

        fragGMap = new GmapFragment();
        fragQRCode = new QRcodeFragment();
        fragGo = new GoFragment();
        fragTool = new ToolFragment();

        fragments = new Fragment[]{fragQRCode,fragGMap,fragGo,fragTool};
        fragmentTAGS = new String[]{"QR code","Map","Go","Tools"};

        onNavigationItemSelected(navigationView.getMenu().getItem(0));

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
