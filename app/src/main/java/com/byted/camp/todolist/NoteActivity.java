package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.byted.camp.todolist.beans.Prority;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract.TodoEntry;
import com.byted.camp.todolist.db.TodoDbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.byted.camp.todolist.beans.State.*;

public class NoteActivity extends AppCompatActivity {
    private TodoDbHelper todoDbHelper;
    private EditText editText;
    private RadioGroup radioGroup;
    private Button addBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);

        todoDbHelper=new TodoDbHelper(this);
        editText = findViewById(R.id.edit_text);
        radioGroup=findViewById(R.id.prority);

        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }

        addBtn = findViewById(R.id.btn_add);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                Prority prority;
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.hight:
                        prority=Prority.HIGHT;break;
                    case R.id.mid:
                        prority=Prority.MID;break;
                    default:
                        prority=Prority.LOW;break;
                }
                boolean succeed = saveNote2Database(content.toString().trim(),prority);
                if (succeed) {
                    Toast.makeText(NoteActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        todoDbHelper.close();
        super.onDestroy();
    }

    private boolean saveNote2Database(String content, Prority prority) {
        // TODO 插入一条新数据，返回是否插入成功
        SQLiteDatabase tododb=todoDbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(TodoEntry.COLUMN_NAME_DATE, new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH).format(new Date(System.currentTimeMillis())));
        values.put(TodoEntry.COLUMN_NAME_STATE, TODO.intValue);
        values.put(TodoEntry.COLUMN_NAME_PRORITY,prority.intValue);
        values.put(TodoEntry.COLUMN_NAME_CONTENT,content);
        long newRowId= tododb.insert(TodoEntry.TABLE_NAME,null,values);
        Log.d("database", "insert newRow " + newRowId + "\n");
        return newRowId==-1?false:true;
    }
}
