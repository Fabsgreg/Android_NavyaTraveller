package navya.tech.navyatraveller;


import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gregoire.frezet on 24/03/2016.
 */
public class GoFragment extends Fragment implements View.OnClickListener {

    private Button goBouton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.go_fragment, container, false);

        String [] values =
                {"Line 1","Line 2","Line 3","Line 4","Line 5","Line 6","Line 7","Line 8",};
        Spinner spinner = (Spinner) v.findViewById(R.id.spinner);
        ArrayAdapter<String> LTRadapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, values);
        LTRadapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(LTRadapter);


        goBouton = (Button) v.findViewById(R.id.go_button);
        goBouton.setOnClickListener(this);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void MyClick(View v) {
        switch(v.getId()) {
            case R.id.go_button:
                int a = 1;
                break;
            // Just like you were doing
        }
    }


    @Override
    public void onClick(View v) {
        //do what you want to do when mybutton is clicked

        if (v.getId() == R.id.go_button)
        {
            //int tt = 1;
            //FragmentTransaction t = this.getFragmentManager().beginTransaction();
            //Fragment mFrag = new ToolFragment();
            //t.replace(R.id.content_frame, mFrag, "ToolFragment");
            //t.commit();


            Context context = getActivity();
            AlertDialog ad = new AlertDialog.Builder(context)
                    .create();
            ad.setCancelable(false);
            ad.setTitle("Title");
            ad.setMessage("Message");
            ad.setButton(-1, "but", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            ad.show();
        }
    }

}
