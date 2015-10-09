package com.example.rodrigobresan.awesomeplayer.activity;

/**
 * Created by Rodrigo Bresan on 08/10/2015.
 */

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.rodrigobresan.awesomeplayer.R;
import com.example.rodrigobresan.awesomeplayer.fragment.LyricsFragment;
import com.example.rodrigobresan.awesomeplayer.fragment.VideoFragment;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends AppCompatActivity implements LyricsFragment.ButtonClicked {

    private static final String TAG_VIDEO_FRAGMENT = "VideoFragment";
    private static final String TAG_LYRICS_FRAGMENT = "LyricsFragment";

    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    private NavigationView mNavView;
    private ActionBarDrawerToggle mDrawerToggle;

    private LyricsFragment mLyricsFragment;
    private VideoFragment mVideoFragment;

    private FragmentTransaction mFragmentTransaction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setupToolbar();
        setupDrawerNavigation();

        checkPreviousInstanceState(savedInstanceState);

        displayVideoFragment();
    }

    private void setupToolbar() {
        // replace the default action bar with the toolbar (from Material Design)
        // set the Toolbar (Material Design) and the Drawer Navigation
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void setupDrawerNavigation() {
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = setupDrawerToggle();

        mDrawer.setDrawerListener(mDrawerToggle);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        mNavView = (NavigationView) findViewById(R.id.nvView);

        setupDrawerContent(mNavView);
    }

    private void checkPreviousInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mVideoFragment = VideoFragment.newInstance(1);
            mLyricsFragment = LyricsFragment.newInstance(2);
        }
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.drawer_open, R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    /*
     * This method is used to check which item was selected on the Drawer Menu
     * and then display the item's fragment
     */
    private void selectDrawerItem(MenuItem menuItem) {
        // check which item was selected on the drawer menu
        switch(menuItem.getItemId()) {
            case R.id.nav_video_fragment:
                displayVideoFragment();
                break;
            case R.id.nav_lyrics_fragment:
                displayLyricsFragment();
                break;
        }

        // mark the item as checked (current one)
        menuItem.setChecked(true);

        // change the activity title to match the fragment one
        setTitle(menuItem.getTitle());

        // and finally close the drawer
        mDrawer.closeDrawers();
    }

    /*
     * This method is used to display the video fragment on the current
     * content frame
     */
    private void displayVideoFragment() {

        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        // must check if the fragment is already added, otherwise we must add it into
        // our frame container
        if (mVideoFragment.isAdded()) {
            mFragmentTransaction.show(mVideoFragment);
        } else {  // hasn't been added yet, so we add to the frame container
            mFragmentTransaction.add(R.id.flContent, mVideoFragment, TAG_VIDEO_FRAGMENT);
        }

        // hide the lyrics fragment
        if (mLyricsFragment.isAdded()) {
            mFragmentTransaction.hide(mLyricsFragment);
        }

        mFragmentTransaction.commit();
    }

    // Same as the displayVideoFragment(), but with the lyrics fragment
    private void displayLyricsFragment() {

        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        // must check if the fragment is already added, otherwise we must add it into
        // our frame container
        if (mLyricsFragment.isAdded()) {
            mFragmentTransaction.show(mLyricsFragment);
        } else {  // hasn't been added yet, so we add to the frame container
            mFragmentTransaction.add(R.id.flContent, mLyricsFragment, TAG_LYRICS_FRAGMENT);
        }

        // hide the lyrics fragment
        if (mVideoFragment.isAdded()) {
            mFragmentTransaction.hide(mVideoFragment);
        }

        mFragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void pauseVideo() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        VideoFragment videoFragment = (VideoFragment) fragmentManager.findFragmentByTag(TAG_VIDEO_FRAGMENT);
        videoFragment.pauseVideo();
    }
    @Override
    public void continueVideo() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        VideoFragment videoFragment = (VideoFragment) fragmentManager.findFragmentByTag(TAG_VIDEO_FRAGMENT);
        videoFragment.continueVideo();
    }
}
