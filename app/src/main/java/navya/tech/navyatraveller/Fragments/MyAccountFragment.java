package navya.tech.navyatraveller.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import navya.tech.navyatraveller.MainActivity;
import navya.tech.navyatraveller.R;

/**
 * Created by gregoire.frezet on 21/04/2016.
 */
public class MyAccountFragment extends Fragment implements View.OnClickListener {

    private TextView mFirstNameText;
    private TextView mLastNameText;
    private TextView mEmailText;
    private TextView mPassword;
    private TextView mConfirmPassword;
    private TextView mPhoneNumber;
    private Button mChange;
    private Button mLogOut;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.my_account_fragment, container, false);

        mFirstNameText = (TextView) v.findViewById(R.id.f_text);
        mLastNameText = (TextView) v.findViewById(R.id.l_text);
        mEmailText = (TextView) v.findViewById(R.id.e_text);
        mPassword = (TextView) v.findViewById(R.id.p_text);
        mConfirmPassword = (TextView) v.findViewById(R.id.c_text);
        mPhoneNumber = (TextView) v.findViewById(R.id.myphone_text);
        mChange = (Button) v.findViewById(R.id.change_button);
        mLogOut = (Button) v.findViewById(R.id.out_button);

        mChange.setOnClickListener(this);
        mLogOut.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.change_button) {
            String password = mPassword.getText().toString();
            String confirmPassword = mConfirmPassword.getText().toString();
            String email = mEmailText.getText().toString();
            String firstName = mFirstNameText.getText().toString();
            String lastName = mLastNameText.getText().toString();

            if (password.equalsIgnoreCase(MainActivity.savingAccount.getPassword())     &&
                email.equalsIgnoreCase(MainActivity.savingAccount.getEmail())           &&
                firstName.equalsIgnoreCase(MainActivity.savingAccount.getFirstName())   &&
                lastName.equalsIgnoreCase(MainActivity.savingAccount.getLastName()))
            {
                Toast.makeText(getActivity(),"Nothing has changed",Toast.LENGTH_LONG).show();
            }
            else if (!password.equalsIgnoreCase(confirmPassword)) {
                mPassword.setError("Passwords are different");
                mConfirmPassword.setError("Passwords are different");
            }
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEmailText.setError("Your email address is invalid");
            }
            else if (password.length() == 0) {
                mPassword.setError("Field cannot be empty");
            }
            else if (firstName.length() == 0) {
                mFirstNameText.setError("Field cannot be empty");
            }
            else if (lastName.length() == 0) {
                mLastNameText.setError("Field cannot be empty");
            }
            else if (!firstName.matches("[a-zA-Z]+")) {
                mFirstNameText.setError("Enter only alphabetical character");
            }
            else if (!lastName.matches("[a-zA-Z]+")) {
                mLastNameText.setError("Enter only alphabetical character");
            }
            else {
                JSONObject request = new JSONObject();
                try {
                    request.put("first_name", firstName);
                    request.put("last_name", lastName);
                    request.put("phone_number", MainActivity.savingAccount.getPhoneNumber());
                    request.put("email", email);
                    request.put("password", password);

                    MainActivity.mSocket.emit("changeRequest", request);
                    ShowMyDialog("Info","Information changed");
                    MainActivity.UpdateAccountData();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
        else if (v.getId() == R.id.out_button) {
            JSONObject request = new JSONObject();
            try {
                request.put("phone_number", MainActivity.savingAccount.getPhoneNumber());
                MainActivity.mSocket.emit("accountDisconnected", request);
                MainActivity.savingAccount.setConnected(false);
                ((MainActivity) getActivity()).onNavigationItemSelected(((MainActivity) getActivity()).navigationView.getMenu().getItem(3).setChecked(true));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirstNameText.setText(MainActivity.savingAccount.getFirstName());
        mLastNameText.setText(MainActivity.savingAccount.getLastName());
        mEmailText.setText(MainActivity.savingAccount.getEmail());
        mPassword.setText(MainActivity.savingAccount.getPassword());
        mConfirmPassword.setText("");
        mPhoneNumber.setText(MainActivity.savingAccount.getPhoneNumber());
    }

    private void ShowMyDialog (String title, String text) {
        Context context = getActivity();
        AlertDialog ad = new AlertDialog.Builder(context).create();
        ad.setCancelable(false);
        ad.setTitle(title);
        ad.setMessage(text);
        ad.setButton(-1, "OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }
}
