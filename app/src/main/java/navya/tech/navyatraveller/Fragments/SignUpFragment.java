package navya.tech.navyatraveller.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;
import navya.tech.navyatraveller.MainActivity;
import navya.tech.navyatraveller.R;
import navya.tech.navyatraveller.SaveResult;

/**
 * Created by gregoire.frezet on 21/04/2016.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener {

    private TextView mFirstNameText;
    private TextView mLastNameText;
    private TextView mEmailText;
    private TextView mPassword;
    private TextView mConfirmPassword;
    private TextView mPhoneNumber;
    private Button  mSignUp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.mSocket.on("signUpSuccessful", onSigningUpResultReceived);
        MainActivity.mSocket.on("emailError", onEmailError);
        MainActivity.mSocket.on("phoneNumberError", onSignUpPhoneNumberError);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.sign_up_fragment, container, false);

        mFirstNameText = (TextView) v.findViewById(R.id.first_name_text);
        mLastNameText = (TextView) v.findViewById(R.id.last_name_text);
        mEmailText = (TextView) v.findViewById(R.id.email_text);
        mPassword = (TextView) v.findViewById(R.id.password_text);
        mConfirmPassword = (TextView) v.findViewById(R.id.confirm_passwaord_text);
        mPhoneNumber = (TextView) v.findViewById(R.id.phone_number_text);
        mSignUp = (Button) v.findViewById(R.id.signUp_button);

        mPhoneNumber.setText(MainActivity.savingAccount.getPhoneNumber());

        mSignUp.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {

        String password = mPassword.getText().toString();
        String confirmPassword = mConfirmPassword.getText().toString();
        String email = mEmailText.getText().toString();
        String firstName = mFirstNameText.getText().toString();
        String lastName = mLastNameText.getText().toString();

        if (!password.equalsIgnoreCase(confirmPassword)) {
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

                MainActivity.mSocket.emit("signUpRequest", request);
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
        mFirstNameText.setText("");
        mLastNameText.setText("");
        mEmailText.setText("");
        mPassword.setText("");
        mConfirmPassword.setText("");
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

    private SaveResult MySaving() {return MainActivity.savingData;}


    private Emitter.Listener onSigningUpResultReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.savingAccount.setConnected(true);
                    MainActivity.UpdateAccountData();
                    Toast.makeText(getActivity(),"Congratulations ! You're now registered",Toast.LENGTH_LONG).show();
                    ((MainActivity) getActivity()).onNavigationItemSelected(((MainActivity) getActivity()).navigationView.getMenu().getItem(0).setChecked(true));
                }
            });
        }
    };

    private Emitter.Listener onEmailError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowMyDialog("Error","Your email address is already registered");
                }
            });
        }
    };

    private Emitter.Listener onSignUpPhoneNumberError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowMyDialog("Error","Your phone number is already registered");
                }
            });
        }
    };
}
