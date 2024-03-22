package cs20a.doublezerotwo.dablist.Operations;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import cs20a.doublezerotwo.dablist.Adapter.taskAdapter;
import cs20a.doublezerotwo.dablist.R;

import java.util.Objects;

public class SwipeControls  extends ItemTouchHelper.SimpleCallback {
    private final taskAdapter adapter;
    public SwipeControls(taskAdapter adapter){
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target){
        return false;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction){
        final int position = viewHolder.getAdapterPosition();
        if(direction == ItemTouchHelper.LEFT){
            adapter.getContext();
            adapter.deleteItem(position);
        }
        else{
            adapter.editItem(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isActive){
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isActive);

        Drawable icon;
        ColorDrawable bg;
        View itemView = viewHolder.itemView;
        int bgCornerOffset = 100;

        if(dX>0){
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.icons8_pencil);
            bg = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.shadow));
        }
        else{
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.icons8_multiply_2);
            bg = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.shadow));
        }

        int iconMargin = (itemView.getHeight() - Objects.requireNonNull(icon).getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if(dX>0){
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicHeight();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            bg.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + bgCornerOffset, itemView.getBottom());
        } else if (dX < 0) {
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            bg.setBounds(itemView.getRight() + ((int) dX) - bgCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else {
            bg.setBounds(0, 0, 0, 0);
        }

        bg.draw(c);
        icon.draw(c);
        }
    }
