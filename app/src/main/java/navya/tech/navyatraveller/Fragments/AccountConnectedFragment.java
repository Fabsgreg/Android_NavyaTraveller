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
public class AccountConnectedFragment extends Fragment {

    //
    ////////////////////////////////////////////////////  View Override /////////////////////////////////////////////////////////
    //

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FragmentTabHost mTabHostConnected = new FragmentTabHost(getActivity());
        mTabHostConnected.setup(getActivity(), getChildFragmentManager(), R.layout.account_connected_fragment);
        mTabHostConnected.addTab(mTabHostConnected.newTabSpec("Tab1").setIndicator("My Account"), MyAccountFragment.class, null);
        mTabHostConnected.addTab(mTabHostConnected.newTabSpec("Tab2").setIndicator("My History"), HistoryFragment.class, null);
        mTabHostConnected.setOnTabChangedListener(mTabHostConnected);
        mTabHostConnected.setOnTabChangedListener(new TabHost.OnTabChangeListener(){
            @Override
            public void onTabChanged(String tabId) {
                closeKeyboard(getActivity(), getActivity().getCurrentFocus().getWindowToken());
            }});
        return mTabHostConnected;
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
