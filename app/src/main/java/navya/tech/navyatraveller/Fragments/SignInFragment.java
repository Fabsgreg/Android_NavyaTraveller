package navya.tech.navyatraveller.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;
import navya.tech.navyatraveller.MainActivity;
import navya.tech.navyatraveller.R;

/**
 * Created by gregoire.frezet on 21/04/2016.
 */
public class SignInFragment extends Fragment implements View.OnClickListener {

    private TextView mPassForgot;
    private TextView mPassText;
    private TextView mEmailText;
    private Button mLogButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.mSocket.on("emailNotFound", onEmailNotFound);
        MainActivity.mSocket.on("emailSent", onEmailSent);
        MainActivity.mSocket.on("recipientRejected", onRecipientRejected);
        MainActivity.mSocket.on("signInFailed", onSignInFailed);
        MainActivity.mSocket.on("signInSuccessful", onSignInSuccessful);
        MainActivity.mSocket.on("phoneNumberError", onPhoneNumberError);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.sign_in_fragment, container, false);

        mPassForgot = (TextView) v.findViewById(R.id.pass_forgot);
        mPassText = (TextView) v.findViewById(R.id.pass_text);
        mEmailText = (TextView) v.findViewById(R.id.email_text);
        mLogButton = (Button) v.findViewById(R.id.log_button);

        mPassForgot.setOnClickListener(this);
        mLogButton.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.pass_forgot) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Password recovery");
            builder.setMessage("Please, type your email address, you will receive a mail containing your password");

            // Set up the input
            final EditText input = new EditText(getContext());
            // Specify the type of input expected
            input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String email = input.getText().toString();
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(getActivity(),"Your email address is invalid",Toast.LENGTH_LONG).show();
                    }
                    else {
                        JSONObject request = new JSONObject();
                        try {
                            request.put("email", email);
                            MainActivity.mSocket.emit("passForgotRequest", request);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
        else if (v.getId() == R.id.log_button) {
            String password = mPassText.getText().toString();
            String email = mEmailText.getText().toString();

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEmailText.setError("Your email address is invalid");
            }
            else {
                JSONObject request = new JSONObject();
                try {
                    request.put("email", email);
                    request.put("password", password);
                    request.put("phone_number", MainActivity.savingAccount.getPhoneNumber());
                    MainActivity.mSocket.emit("signInRequest", request);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mEmailText.setText("");
        mPassText.setText("");
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

    private Emitter.Listener onEmailNotFound = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(),"Your email address hasn't been found",Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onPhoneNumberError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowMyDialog("Error", "Your phone number doesn't match with the one filled in your account, please contact us at developer.navya@gmail.com");
                }
            });
        }
    };

    private Emitter.Listener onRecipientRejected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowMyDialog("Error", "The recipient was rejected, please contact us at developer.navya@gmail.com");
                }
            });
        }
    };

    private Emitter.Listener onEmailSent = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(),"Your password has been sent",Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onSignInFailed = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(),"Connection identifier wrong",Toast.LENGTH_LONG).show();
                    try {
                        this.finalize();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onSignInSuccessful = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.savingAccount.setConnected(true);
                    MainActivity.UpdateAccountData();
                    Toast.makeText(getActivity(),"Congratulations ! You're now logged in",Toast.LENGTH_LONG).show();
                    ((MainActivity) getActivity()).onNavigationItemSelected(((MainActivity) getActivity()).navigationView.getMenu().getItem(0).setChecked(true));
                }
            });
        }
    };
}
