package cs20a.doublezerotwo.dablist.Operations;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import cs20a.doublezerotwo.dablist.Model.DialogCloseListener;
import cs20a.doublezerotwo.dablist.Model.taskModel;
import cs20a.doublezerotwo.dablist.R;
import cs20a.doublezerotwo.dablist.Utils.DatabaseHandler;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";
    private EditText newTaskText;
    private Button saveButton;
    private DatabaseHandler db;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS_KEY = "CS20A.DoubleZeroTwo.dablist.sharedPrefs";
    private static final String IS_ANE_KEY = "isAutoNewlineEnabled";
    private boolean getIsAutoNewlineEnabled(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(IS_ANE_KEY, true); // Default value true if key not found
    }
    private String AutoNewline(String text) {
        boolean isAutoNewlineEnabled = getIsAutoNewlineEnabled(requireContext());
        if(isAutoNewlineEnabled) {
            text = text.replaceAll(",\\s+", ",");
            text = text.replace(",", "\n");
        }
        return text;
    }
    public static AddNewTask newInstance(){
        return new AddNewTask();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.new_task, container, false);
        Objects.requireNonNull(getDialog()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        newTaskText = requireView().findViewById(R.id.newTaskText);
        saveButton = requireView().findViewById(R.id.newTaskButton);

        newTaskText.requestFocus();

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(newTaskText, InputMethodManager.SHOW_IMPLICIT);

        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if(bundle !=null){
            isUpdate = true;
            String task = bundle.getString("task");
            newTaskText.setText(task);
            newTaskText.setSelection(task.length());
        }

        db = new DatabaseHandler(getActivity());

        db.openDatabase();

        newTaskText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveButton.setEnabled(!s.toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                String filteredText = text.replace("\r", "");

                if (!text.equals(filteredText)) {
                    newTaskText.setText(filteredText);
                    newTaskText.setSelection(filteredText.length());
                }

            }
        });

        boolean finalIsUpdate = isUpdate;

        saveButton.setOnClickListener(v -> {
            String text = newTaskText.getText().toString().trim();
            if(text.isEmpty())
                saveButton.setEnabled(false);
            else{
                text = AutoNewline(text);
                if(finalIsUpdate) {
                    db.updateTask(bundle.getInt("id"), text);
                    db.insertSuggestedTask(text);
                }
                    else{
                    taskModel task = new taskModel();
                    task.setTask(text);
                    task.setStatus(0);
                    db.insertTask(task);
                    db.insertSuggestedTask(text);
                }
                dismiss();
            }

        });

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog){
        Activity activity = getActivity();
        if(activity instanceof DialogCloseListener){
            ((DialogCloseListener)activity).handleDialogClose(dialog);
        }
    }
}
