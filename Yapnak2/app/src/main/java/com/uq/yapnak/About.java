package com.uq.yapnak;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class About extends AppCompatActivity {

    TextView aboutText;
    String about;
    DrawerLayout lMenu;
    ActionBarDrawerToggle lMenuToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        aboutText = (TextView) findViewById(R.id.about_text);
        SharedPreferences data = getSharedPreferences("Yapnak", 0);
        about = data.getString("about", null);
        aboutText.setText(about);
        drawerSetup();
    }

    public void drawerSetup() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lMenu = (DrawerLayout) findViewById(R.id.drawer_layout);
        lMenuToggle = new ActionBarDrawerToggle(this, lMenu, R.string.menu_closed, R.string.menu_open) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        lMenu.setDrawerListener(lMenuToggle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (lMenuToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        lMenuToggle.syncState();
    }
}
