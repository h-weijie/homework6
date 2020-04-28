package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.Prority;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract.TodoEntry;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TodoDbHelper todoDbHelper;
    private final SimpleDateFormat simpleDateFormat=new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        todoDbHelper=new TodoDbHelper(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        todoDbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        ArrayList<Note> todoList=new ArrayList<Note>();
        SQLiteDatabase tododb=todoDbHelper.getReadableDatabase();
        String[] projection={
                TodoEntry.COLUNM_NAME_ID,
                TodoEntry.COLUMN_NAME_DATE,
                TodoEntry.COLUMN_NAME_STATE,
                TodoEntry.COLUMN_NAME_PRORITY,
                TodoEntry.COLUMN_NAME_CONTENT
        };
        String sortOder=TodoEntry.COLUMN_NAME_PRORITY+" DESC";
        Cursor cursor=tododb.query(
            TodoEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            sortOder
        );
        while(cursor.moveToNext()){
            Note note=new Note(cursor.getLong(cursor.getColumnIndex(TodoEntry.COLUNM_NAME_ID)));
            Date date=null;
            try {
                 date= simpleDateFormat.parse(cursor.getString(cursor.getColumnIndex(TodoEntry.COLUMN_NAME_DATE)));
            }catch (Exception e){
                Log.d("ERROR",e.getMessage());
            }
            note.setDate(date);
            note.setState(State.from(cursor.getInt(cursor.getColumnIndex(TodoEntry.COLUMN_NAME_STATE))));
            note.setPrority(Prority.from(cursor.getInt(cursor.getColumnIndex(TodoEntry.COLUMN_NAME_PRORITY))));
            note.setContent(cursor.getString(cursor.getColumnIndex(TodoEntry.COLUMN_NAME_CONTENT)));
            todoList.add(note);
        }
        return todoList;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        SQLiteDatabase tododb=todoDbHelper.getWritableDatabase();
        String selection=TodoEntry.COLUNM_NAME_ID+" = ?";
        String[] selectionArgs={String.valueOf(note.id)};
        int deleteRows=tododb.delete(TodoEntry.TABLE_NAME,selection,selectionArgs);
        Log.d("database","delete row "+deleteRows+"\n");
        notesAdapter.refresh(loadNotesFromDatabase());
    }

    private void updateNode(Note note) {
        // 更新数据
        SQLiteDatabase tododb=todoDbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(TodoEntry.COLUMN_NAME_STATE,note.getState().intValue);
        String selection =TodoEntry.COLUNM_NAME_ID+" = ?";
        String[] selectionArgs={String.valueOf(note.id)};
        int count=tododb.update(
                TodoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        Log.d("database","update "+count+" rows\n");
        notesAdapter.refresh(loadNotesFromDatabase());
    }

}
