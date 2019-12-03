package io.mosaicnetworks.babble.configure;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import io.mosaicnetworks.babble.R;
import io.mosaicnetworks.babble.node.BabbleService;

/**
 * This activity complements the {@link BabbleService}. It consists of a set of fragments which
 * allow the {@link BabbleService} to be configured. Extend class and override the
 * {@link BaseConfigActivity#getBabbleService()}, {@link BaseConfigActivity#onJoined(String)} and
 * {@link BaseConfigActivity#onStartedNew(String)} methods.
 */
public abstract class BaseConfigActivity extends AppCompatActivity implements OnFragmentInteractionListener {

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

    private void addFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.constraint_layout, fragment);
        fragmentTransaction.commit();
    }

    private void removeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
    }

    private void replaceFragment(Fragment fragment) {
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
