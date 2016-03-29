package navya.tech.navyatraveller;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import navya.tech.navyatraveller.Databases.Line;
import navya.tech.navyatraveller.Databases.LineDAO;
import navya.tech.navyatraveller.Databases.MyDBHandler;
import navya.tech.navyatraveller.Databases.MyDBHandler2;
import navya.tech.navyatraveller.Databases.Product;
import navya.tech.navyatraveller.Databases.Station;
import navya.tech.navyatraveller.Databases.StationDAO;
import navya.tech.navyatraveller.Fragments.GmapFragment;
import navya.tech.navyatraveller.Fragments.GoFragment;
import navya.tech.navyatraveller.Fragments.QRcodeFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    LineDAO _lineDAO;
    StationDAO _stationDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Test DB

/*        MyDBHandler2 dbHandler = new MyDBHandler2(this, null, null, 1);
        int quantity = 10;
        Product product = new Product("Product1", quantity);
        dbHandler.addProduct(product);

        int test = 0;
        int test2 = 0;
        boolean test3 = false;

        product = dbHandler.findProduct("Product1");
        if (product != null) {
            test = product.getID();
            test2 = product.getQuantity();
        }


        test3 = dbHandler.deleteProduct("Product1");
        test3 = dbHandler.deleteProduct("Product1");
        test3 = dbHandler.deleteProduct("Product1");
        test3 = dbHandler.deleteProduct("Product1");

        product = dbHandler.findProduct("Product1");
        if (product != null) {
            test = product.getID();
            test2 = product.getQuantity();
        }
        else{
            int a = 1;
        }*/


        // Trst DB advanced

        //MyDBHandler dbHandler2 = new MyDBHandler(this, null, null, 1);

        this._lineDAO = new LineDAO(this);
        //this._stationDAO = new StationDAO(this);


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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            fm.beginTransaction().replace(R.id.content_frame, new QRcodeFragment()).commit();
        } else if (id == R.id.nav_map) {
            fm.beginTransaction().replace(R.id.content_frame, new GmapFragment()).commit();

        } else if (id == R.id.nav_go) {
            fm.beginTransaction().replace(R.id.content_frame, new GoFragment()).commit();

        } else if (id == R.id.nav_manage) {

            Line createdLine = _lineDAO.createLine("line1");


            Station createdStation1 = _stationDAO.createStation("station1", "line1");
            Station createdStation2 = _stationDAO.createStation("station2", "line1");

            List<Station> myList = new ArrayList<Station>();

            myList =  _stationDAO.getStationsOfLine("line1");

            int toto = 1;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
