package de.everytap.broteinheiten_datenbank.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.everytap.broteinheiten_datenbank.R;
import de.everytap.broteinheiten_datenbank.interfaces.OnFoodItemClickListener;
import de.everytap.broteinheiten_datenbank.model.Food;

/**
 * Created by randombyte on 18.12.2014.
 */
public class BeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Food> foodList;
    OnFoodItemClickListener onFoodItemClickListener;

    private static final int EMPTY_ITEM = 10;

    public BeAdapter(@NonNull List<Food> foodList, @NonNull OnFoodItemClickListener onFoodItemClickListener) {
        this.foodList = foodList;
        this.onFoodItemClickListener = onFoodItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Neue(s) View(s)
        View rootView;

        if (viewType == EMPTY_ITEM) {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_empty, parent, false);
            return new EmptyViewHolder(rootView);
        } else {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_food_item, parent, false);
            return new BeViewHolder(rootView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (foodList.size() == 0) return EMPTY_ITEM;
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        //View verändern

        //Nur verändern, wenns kein EMPTY_ITEM ist. EMPTY_ITEM muss nicht verändert werden
        if (holder instanceof BeViewHolder) {

            BeViewHolder beViewHolder = (BeViewHolder) holder;

            Food food = foodList.get(position);
            beViewHolder.userCreated.setVisibility(food.isUserCreated() ? View.VISIBLE : View.GONE);
            if (!food.isUserCreated()) {
                beViewHolder.rootView.setBackgroundColor(Color.WHITE); //Nur UserCreated Items touchable
            } else {
                beViewHolder.rootView.setBackgroundResource(R.drawable.item_background_ripple);
            }
            beViewHolder.foodName.setText(food.getName());        //Fleisch
            beViewHolder.beAmount.setText(food.getBe() + " BE");   //8.9 BE

            final int id = foodList.get(position).getId(); //Die id muss final sein; sie verändert sich nicht bei Veränderungen anderer Einträge, im Gegensatz zu der Position in der Liste

            beViewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFoodItemClickListener.onFoodItemClick(id);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size() == 0 ? 1 : foodList.size();
    }

    public void setFoodList(List<Food> foodList, boolean notifyDataSetChanged) {
        this.foodList = foodList;
        if (notifyDataSetChanged) {
            notifyDataSetChanged();
        }
    }

    public List<Food> getFoodList() {
        return foodList;
    }

    public static class BeViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        public ImageView userCreated;
        public TextView foodName;
        public TextView beAmount;
        public TextView amount;

        public BeViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            userCreated = (ImageView) rootView.findViewById(R.id.user_created_icon);
            foodName = (TextView) rootView.findViewById(R.id.food);
            beAmount = (TextView) rootView.findViewById(R.id.be);
            amount = (TextView) rootView.findViewById(R.id.amount);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {

        public View rootView;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
        }
    }
}
