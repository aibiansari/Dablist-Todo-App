package cs20a.doublezerotwo.dablist.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.media.Ringtone;
import android.media.RingtoneManager;

import cs20a.doublezerotwo.dablist.Operations.AddNewTask;
import cs20a.doublezerotwo.dablist.MainActivity;
import cs20a.doublezerotwo.dablist.Model.taskModel;
import cs20a.doublezerotwo.dablist.R;
import cs20a.doublezerotwo.dablist.Utils.DatabaseHandler;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class taskAdapter  extends RecyclerView.Adapter<taskAdapter.ViewHolder> {

    private List<taskModel> taskList;
    private final MainActivity activity;
    private final DatabaseHandler db;

    public taskAdapter(DatabaseHandler db, MainActivity activity){
        this.db = db;
        this.activity = activity;
    }

    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    public void onBindViewHolder(ViewHolder holder, int position){
        db.openDatabase();
        taskModel item = taskList.get(position);
        holder.task.setText(item.getTask());
        holder.task.setOnCheckedChangeListener(null);
        holder.task.setChecked(toBool(item.getStatus()));

        if (item.getStatus() != 0) {
            holder.task.setPaintFlags(holder.task.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.task.setTextColor(ContextCompat.getColor(activity, R.color.off));
        } else {
            holder.task.setPaintFlags(holder.task.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.task.setTextColor(ContextCompat.getColor(activity, R.color.white));
        }

        holder.task.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if(!activity.snackbarVisible) {
                if (isChecked) {
                    db.updateStatus(item.getId(), 1);
                    holder.task.setPaintFlags(holder.task.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.task.setTextColor(ContextCompat.getColor(activity, R.color.off));
                    playSound(1);
                } else {
                    db.updateStatus(item.getId(), 0);
                    holder.task.setPaintFlags(holder.task.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    holder.task.setTextColor(ContextCompat.getColor(activity, R.color.white));
                    playSound(2);
                }
                new Handler().postDelayed(activity::updateList, 300);
            }
        });
    }

    public int getItemCount(){
        return taskList.size();
    }

    public Context getContext(){ return activity; }

    private boolean toBool(int n){
        return n!=0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<taskModel> taskList){
        this.taskList = taskList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position){
        taskModel item = taskList.get(position);
        String taskText = item.getTask();
        taskList.remove(position);
        notifyItemRemoved(position);
        activity.checkDel();
        playSound(3);

        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "'" + taskText + "' Deleted", Snackbar.LENGTH_SHORT).setDuration(1200);
        snackbar.setAction("Undo", view -> {
            taskList.add(position, item);
            notifyItemInserted(position);
            activity.checkDel();
        });

        snackbar.setActionTextColor(ContextCompat.getColor(activity, R.color.mainColor));

        snackbar.show();

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event != DISMISS_EVENT_ACTION) {
                    db.deleteTask(item.getId());
                }
            }
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    public void deleteChecked() {
        List<taskModel> deletedTasks = new ArrayList<>();
        Animation fadeOutAnim = AnimationUtils.loadAnimation(activity, R.anim.fade_out);
        activity.tasks.startAnimation(fadeOutAnim);
        playSound(3);

        new Handler().postDelayed(() -> {
        for (Iterator<taskModel> iterator = taskList.iterator(); iterator.hasNext(); ) {
            taskModel task = iterator.next();
            if (task.getStatus() != 0) {
                deletedTasks.add(task);
                iterator.remove();
            }
        }
        notifyDataSetChanged();
        activity.checkDel();

        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "Checked Tasks Deleted", Snackbar.LENGTH_SHORT).setDuration(1200);
        activity.snackbarVisible = true;

        snackbar.setAction("Undo", view -> {
            taskList.addAll(deletedTasks);
            notifyDataSetChanged();
            activity.checkDel();
            activity.snackbarVisible = false;

            Animation fadeInAnim = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
            activity.tasks.startAnimation(fadeInAnim);

        });

        snackbar.setActionTextColor(ContextCompat.getColor(activity, R.color.mainColor));

        snackbar.show();

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event != DISMISS_EVENT_ACTION) {
                    activity.snackbarVisible = false;
                    db.deleteChecked();
                    activity.updateList();
                }
            }
        });
    }, 300);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void deleteAll() {
        List<taskModel> deletedTasks = new ArrayList<>(taskList);

        Animation slideOutAnim = AnimationUtils.loadAnimation(activity, R.anim.slide_out);
        activity.tasks.startAnimation(slideOutAnim);
        playSound(3);

        new Handler().postDelayed(() -> {
            taskList.clear();
            notifyDataSetChanged();
            activity.checkDel();

            Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "List Deleted", Snackbar.LENGTH_SHORT).setDuration(1200);

            snackbar.setAction("Undo", view -> {
                taskList.addAll(deletedTasks);
                notifyDataSetChanged();
                activity.checkDel();

                Animation slideInAnim = AnimationUtils.loadAnimation(activity, R.anim.slide_in);
                activity.tasks.startAnimation(slideInAnim);
            });

            snackbar.setActionTextColor(ContextCompat.getColor(activity, R.color.mainColor));

            snackbar.show();

            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if (event != DISMISS_EVENT_ACTION) {
                        db.deleteAll();
                    }
                }
            });
        }, 350);
    }


    public void editItem(int position){
        taskModel item = taskList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task", item.getTask());
        AddNewTask fragment = new AddNewTask();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AddNewTask.TAG);
    }

    private void playSound(int wich) {
        if(activity.shouldPlaySound){
        try {
            int resourceId;
            if (wich == 1)
                resourceId = R.raw.check;
            else if(wich == 2)
                resourceId = R.raw.uncheck;
            else if(wich == 3)
                resourceId = R.raw.delete;
            else{
                Uri defaultNotificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone notificationSound = RingtoneManager.getRingtone(activity, defaultNotificationSoundUri);
                notificationSound.play();
                return;
            }

            Uri notificationSoundUri = Uri.parse("android.resource://" + activity.getPackageName() + "/" + resourceId);
            Ringtone notificationSound = RingtoneManager.getRingtone(activity, notificationSoundUri);
            notificationSound.play();
        }
        catch (Exception e) {
            e.printStackTrace();
        } }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox task;
        ViewHolder(View view){
            super(view);
            task = view.findViewById(R.id.taskCheckBox);
        }
    }
}

