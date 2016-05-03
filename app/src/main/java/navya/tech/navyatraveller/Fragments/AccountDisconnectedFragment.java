package navya.tech.navyatraveller.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;

import navya.tech.navyatraveller.R;

/**
 * Created by gregoire.frezet on 21/04/2016.
 */
public class AccountDisconnectedFragment extends Fragment {

    //
    ////////////////////////////////////////////////////  View Override /////////////////////////////////////////////////////////
    //

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FragmentTabHost mTabHostDisconnected = new FragmentTabHost(getActivity());
        mTabHostDisconnected.setup(getActivity(), getChildFragmentManager(), R.layout.account_disconnected_fragment);

        mTabHostDisconnected.addTab(mTabHostDisconnected.newTabSpec("Tab1").setIndicator("Sign In"), SignInFragment.class, null);
        mTabHostDisconnected.addTab(mTabHostDisconnected.newTabSpec("Tab2").setIndicator("Sign Up"), SignUpFragment.class, null);

        mTabHostDisconnected.setOnTabChangedListener(mTabHostDisconnected);
        mTabHostDisconnected.setOnTabChangedListener(new TabHost.OnTabChangeListener(){
                @Override
                public void onTabChanged(String tabId) {
                    closeKeyboard(getActivity(), getActivity().getCurrentFocus().getWindowToken());}});

        return mTabHostDisconnected;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    //
    ////////////////////////////////////////////////////  Miscellaneous functions   /////////////////////////////////////////////////////////
    //

    public static void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }

}
