package example.harsh.com.stockticker.UI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import example.harsh.com.stockticker.R;

public class SettingsActivity extends AppCompatActivity {

    private EditText code_edit;
    private Button btn_done;

    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        code_edit = (EditText) findViewById(R.id.edit_code);
        btn_done = (Button)findViewById(R.id.button);

        prefs = getSharedPreferences("stock1", MODE_PRIVATE);
        btn_done.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(prefs.contains("code")){
                    prefs.edit().remove("code").apply();

                }
                prefs.edit().putString("code", code_edit.getText().toString().toUpperCase()).apply();

                finish();
            }
        });

    }
}
