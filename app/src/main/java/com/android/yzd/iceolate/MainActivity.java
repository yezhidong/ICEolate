package com.android.yzd.iceolate;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.clj.fastble.BleManager;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StatusFragment.OnDelete {

    private FrameLayout mContainer;
    private BUtils mInstance;
    private FloatingActionButton mFab;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_main);

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setMaxConnectCount(7)
                .setOperateTimeout(5000);
        mInstance = BUtils.getInstance();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BluetoothConnectActivity.class);
                startActivityForResult(intent, 100);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mContainer = (FrameLayout) findViewById(R.id.container);
    }


    @Override
    protected void onResume() {
        super.onResume();
        changFragment();
    }

    private void changFragment() {
        //获取到FragmentManager，在V4包中通过getSupportFragmentManager，
        //在系统中原生的Fragment是通过getFragmentManager获得的。
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (mInstance.isConnect()) {
            //2.开启一个事务，通过调用beginTransaction方法开启。
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            //把自己创建好的fragment创建一个对象
            StatusFragment statusFragment = StatusFragment.newInstance();
            //向容器内加入Fragment，一般使用add或者replace方法实现，需要传入容器的id和Fragment的实例。
            fragmentTransaction.replace(R.id.container, statusFragment);
            //提交事务，调用commit方法提交。
            fragmentTransaction.commit();
            mFab.setVisibility(View.GONE);
        } else {
            //2.开启一个事务，通过调用beginTransaction方法开启。
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            //把自己创建好的fragment创建一个对象
            BlankFragment blankFragment = BlankFragment.newInstance();
            //向容器内加入Fragment，一般使用add或者replace方法实现，需要传入容器的id和Fragment的实例。
            fragmentTransaction.replace(R.id.container, blankFragment);
            //提交事务，调用commit方法提交。
            fragmentTransaction.commit();
            mFab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        changFragment();
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
        getMenuInflater().inflate(R.menu.main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            startActivity(new Intent(this, HistoryActivity.class));
        } else if (id == R.id.nav_setting) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDelete() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        //2.开启一个事务，通过调用beginTransaction方法开启。
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //把自己创建好的fragment创建一个对象
        BlankFragment blankFragment = BlankFragment.newInstance();
        //向容器内加入Fragment，一般使用add或者replace方法实现，需要传入容器的id和Fragment的实例。
        fragmentTransaction.replace(R.id.container, blankFragment);
        //提交事务，调用commit方法提交。
        fragmentTransaction.commit();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
        mFab.setVisibility(View.VISIBLE);
    }
}
