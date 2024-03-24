package cs20a.doublezerotwo.dablist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cs20a.doublezerotwo.dablist.Adapter.taskAdapter;
import cs20a.doublezerotwo.dablist.Model.DialogCloseListener;
import cs20a.doublezerotwo.dablist.Model.taskModel;
import cs20a.doublezerotwo.dablist.Operations.AddNewTask;
import cs20a.doublezerotwo.dablist.Operations.SwipeControls;
import cs20a.doublezerotwo.dablist.Utils.DatabaseHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DialogCloseListener {

    public RecyclerView tasks;
    private taskAdapter tasksAdapter;
    private TextView header;
    private Button addButton;
    private Button delButton;
    private List<taskModel> tasksList;
    private DatabaseHandler db;
    public boolean snackbarVisible = false;
    private static final String SHARED_PREFS_KEY = "CS20A.DoubleZeroTwo.dablist.sharedPrefs";
    private static final String SHOULD_PLAY_SOUND_KEY = "shouldPlaySound";
    private static final String IS_ANE_KEY = "isAutoNewlineEnabled";
    public boolean shouldPlaySound = true;
    public boolean isAutoNewlineEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHandler(this );

        db.openDatabase();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        shouldPlaySound = sharedPreferences.getBoolean(SHOULD_PLAY_SOUND_KEY, true);
        isAutoNewlineEnabled = sharedPreferences.getBoolean(IS_ANE_KEY, true);

        tasksList = new ArrayList<>();

        tasks = findViewById(R.id.tasks);
        tasks.setLayoutManager(new LinearLayoutManager(this));

        tasksAdapter = new taskAdapter(db, MainActivity.this);
        tasks.setAdapter(tasksAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeControls(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasks);

        addButton = findViewById(R.id.addButton);

        delButton = findViewById(R.id.delButton);

        updateList();

        header = findViewById(R.id.header);

        header.setOnClickListener(view -> {
            Animation scaleUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_up_long);
            header.startAnimation(scaleUp);
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, header);
            popupMenu.getMenu().add(0, 1, 0, "Show Tasks History");
            popupMenu.getMenu().add(0, 2, 0, "Clear Tasks History");
            popupMenu.getMenu().add(0, 3, 0, "Turn Sound On/Off");
            popupMenu.getMenu().add(0, 4, 0, isAutoNewlineEnabled ? "Disable AutoNewline" : "Enable AutoNewline");

            popupMenu.setOnMenuItemClickListener(item -> {
                if(item.getItemId() == 1){
                    showSuggestionsDialog();
                    return true;
                }
                else if(item.getItemId() == 2){
                    ClearTasks();
                    return true;
                }
                else if(item.getItemId() == 3){
                    soundSwitch();
                    return true;
                }
                else if(item.getItemId() == 4){
                    AutoNewlineSwitch();
                    String message = isAutoNewlineEnabled ? "AutoNewline Feature Enabled" : "AutoNewline Feature Disabled";
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    return true;
                }
                else
                    return false;
            });
            popupMenu.show();
        });

        header.setOnLongClickListener(view -> {
            Animation scaleUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_up_long);
            header.startAnimation(scaleUp);
            return true;
        });

        addButton.setOnClickListener(view -> {
            Animation scaleUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_up);
            addButton.startAnimation(scaleUp);

            AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
        });

        addButton.setOnLongClickListener(view -> {
            Animation scaleUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_up);
            addButton.startAnimation(scaleUp);

            showSuggestionsPopup(addButton);
            return true;
        });

        delButton.setOnClickListener(view -> {

            Animation scaleUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_up);
            delButton.startAnimation(scaleUp);

            if (!db.noUnchecked() && !snackbarVisible){
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogTheme));
                builder.setTitle("Delete All Checked Tasks");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("Yes", (dialog, which) -> tasksAdapter.deleteChecked());

                builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();

            }
            else{
                Toast.makeText(MainActivity.this, "No Checked Tasks!", Toast.LENGTH_SHORT).show();
               }

            });

        delButton.setOnLongClickListener(view -> {
            Animation scaleUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_up);
            delButton.startAnimation(scaleUp);

            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(tasksAdapter.getContext(), R.style.AlertDialogTheme));
            builder.setTitle("Delete All Tasks");
            builder.setMessage("Are you sure ?");
            builder.setPositiveButton("Yes",

                    (dialog, which) -> tasksAdapter.deleteAll());

            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

            return true; });

    }

public void checkDel() {
        if (tasksList.isEmpty()) {
            delButton.setEnabled(false);
            delButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.shadow));
        } else {
            delButton.setEnabled(true);
            delButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red));
        }
    }

    private void showSuggestionsPopup(View anchorView) {
        if (!db.isTableEmpty()) {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, anchorView);
            List<String> suggestedTasks = db.getSuggestedTasks();
            Collections.reverse(suggestedTasks);
            for (String task : suggestedTasks) {
                popupMenu.getMenu().add(task);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                String suggestedTask = Objects.requireNonNull(item.getTitle()).toString();
                addToList(suggestedTask);
                return true;
            });
            popupMenu.show();
        }
        else
        {
            Toast.makeText(MainActivity.this, "No Suggestions to show!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuggestionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogTheme2));
        builder.setTitle("Tasks History");

        if (!db.isTableEmpty()) {
            List<String> suggestedTasks = db.getSuggestedTasks();
            Collections.reverse(suggestedTasks);

            // Convert list of suggestions to array for dialog
            final CharSequence[] suggestionsArray = suggestedTasks.toArray(new CharSequence[0]);

            builder.setItems(suggestionsArray, (dialog, which) -> {
                String suggestedTask = suggestionsArray[which].toString();
                addToList(suggestedTask);
            });

            builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            Toast.makeText(MainActivity.this, "No Tasks History!", Toast.LENGTH_SHORT).show();
        }
    }


    private void ClearTasks(){
        if (!db.isTableEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogTheme));
            builder.setTitle("Clear All Tasks History?");
            builder.setMessage("Are you sure?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                db.deleteAllSuggestions();
                Toast.makeText(MainActivity.this, "Tasks History Cleared!", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else
        {
            Toast.makeText(MainActivity.this, "No Tasks History!", Toast.LENGTH_SHORT).show();
        }
    }
    private void addToList(String suggestedTask) {
        taskModel task = new taskModel();
        task.setTask(suggestedTask);
        task.setStatus(0);

        db.insertTask(task);

        updateList();

        Toast.makeText(this, "'" + suggestedTask + "' added to List.", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(){
        tasksList = db.getTasks();
        Collections.reverse(tasksList);
        tasksAdapter.setTasks(tasksList);
        tasksAdapter.notifyDataSetChanged();
        checkDel();
    }

    private void saveShouldPlaySoundState() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHOULD_PLAY_SOUND_KEY, shouldPlaySound);
        editor.apply();
    }

    private void isAutoNewlineEnabledState() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_ANE_KEY, isAutoNewlineEnabled);
        editor.apply();
    }

    private void soundSwitch(){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogTheme));
        if(shouldPlaySound) {
            builder.setMessage("Notifications Sound : ON");
            builder.setPositiveButton("Turn Off", (dialog, which) -> {
                shouldPlaySound = false;
                saveShouldPlaySoundState();
                Toast.makeText(MainActivity.this, "Notifications Sound Turned OFF", Toast.LENGTH_SHORT).show();
            });
        }
        else {
            builder.setMessage("Notifications Sound : OFF");
            builder.setPositiveButton("Turn On", (dialog, which) -> {
                shouldPlaySound = true;
                saveShouldPlaySoundState();
                Toast.makeText(MainActivity.this, "Notifications Sound Turned ON", Toast.LENGTH_SHORT).show();
            });
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void AutoNewlineSwitch(){
        isAutoNewlineEnabled = !isAutoNewlineEnabled;
        isAutoNewlineEnabledState();
    }
    @Override
    public void handleDialogClose(DialogInterface dialog){
        updateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveShouldPlaySoundState();
        isAutoNewlineEnabledState();
        db.closeDB();
    }
}