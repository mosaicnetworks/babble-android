package io.mosaicnetworks.babble.configure;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.BabbleService;

public abstract class BaseConfigActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    public static final String TAG = "SAMPLE-CHAT";
    private FragmentManager mFragmentManager;
    private HomeFragment mHomeFragment;
    private NewGroupFragment mNewGroupFragment;
    private JoinGroupFragment mJoinGroupFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_config);

        mFragmentManager = getSupportFragmentManager();
        mHomeFragment = HomeFragment.newInstance();

        addFragment(mHomeFragment);
    }

    public void addFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.constraint_layout, fragment);
        fragmentTransaction.commit();
    }

    public void removeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.constraint_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // called when the user presses the new button
    public void newGroup(View view) {
        mNewGroupFragment = NewGroupFragment.newInstance();
        replaceFragment(mNewGroupFragment);
    }

    // called when the user presses the join button
    public void joinGroup(View view) {
        mJoinGroupFragment = JoinGroupFragment.newInstance();
        replaceFragment(mJoinGroupFragment);
    }

    @Override
    public abstract BabbleService getBabbleService();

    @Override
    public abstract void onJoined(String moniker);

    @Override
    public abstract void onStartedNew(String moniker);
}
