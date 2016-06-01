package de.volzo.miscreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private Spinner spRole;
    private Button btOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spRole = (Spinner) findViewById(R.id.spRole);
        btOk = (Button) findViewById(R.id.btOk);

        spRole.setOnItemSelectedListener(this);
        btOk.setOnClickListener(this);
    }


    // as taken from https://developer.android.com/guide/topics/ui/controls/spinner.html
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        saveRole(pos);
    }

    private void saveRole(int pos) {
        SharedPreferences sharedPref = getSharedPreferences("minPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("role", pos);
        editor.commit();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        resetView();
    }

    private void resetView() {
        saveRole(0);
        this.spRole.setSelection(0);
    }

    private void refreshView() {
        int role = getRole();
        this.spRole.setSelection(role);
    }

    private int getRole() {
        SharedPreferences sharedPref = getSharedPreferences("minPrefs", MODE_PRIVATE);
        return sharedPref.getInt("role", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshView();
    }


    @Override
    public void onClick(View v) {
        int role = getRole();
        Intent intent = new Intent(this, tileActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
    }
}
