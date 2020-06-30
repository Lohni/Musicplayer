package com.example.musicplayer.ui.dashboard;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicplayer.MainActivity;
import com.example.musicplayer.R;
import com.example.musicplayer.ui.layout.DraggableCoordinatorLayout;
import com.example.musicplayer.ui.songlist.SongList;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate;

public class DashboardFragment extends Fragment {
    private static int CARD_COUNT = 6;
    private static String[] CARD_COLOR={"#a7c0cd","#ffa270","#4b636e","#ff7043","#78909c","#c63f17"};
    private static int[] CARD_DRAWABLE={R.drawable.ic_album_black_24dp,R.drawable.ic_edit_black_24dp,R.drawable.ic_equalizer_black_24dp,R.drawable.ic_folder_black_24dp,
            R.drawable.ic_library_music_black_24dp,R.drawable.ic_playlist_play_black_24dp};
    private static String[] CARD_TEXT={"Album","Music Tag Editor","Equalizer","Folder","All songs","Playlist"};

    private static int[] CARD_CONFIG=new int[6];

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dashboard,container,false);
        RecyclerView recyclerView = root.findViewById(R.id.dashboar_recycler_view);

        MainActivity mainActivity =(MainActivity) requireActivity();
        mainActivity.getSupportActionBar().setTitle("Dashboard");
        
        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        getCardConfig(sharedPreferences.getString("CARD_CONFIG","0,1,2,3,4,5,"));

        CardAdapter cardAdapter = new CardAdapter(generateCardNumbers());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new CardItemTouchHelperCallback(cardAdapter));
        cardAdapter.setItemTouchHelper(itemTouchHelper);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.setAdapter(cardAdapter);
        recyclerView
                .setAccessibilityDelegateCompat(new RecyclerViewAccessibilityDelegate(recyclerView) {
                    @NonNull
                    @Override
                    public AccessibilityDelegateCompat getItemDelegate() {
                        return new ItemDelegate(this) {

                            @Override
                            public void onInitializeAccessibilityNodeInfo(View host,
                                                                          AccessibilityNodeInfoCompat info) {
                                super.onInitializeAccessibilityNodeInfo(host, info);
                                int position = recyclerView.getChildLayoutPosition(host);
                                if (position != 0) {
                                    info.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.move_card_up_action,
                                            host.getResources().getString(R.string.cat_card_action_move_up)));
                                }
                                if (position != (CARD_COUNT - 1)) {
                                    info.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                                            R.id.move_card_down_action,
                                            host.getResources().getString(R.string.cat_card_action_move_down)));
                                }

                            }

                            @Override
                            public boolean performAccessibilityAction(View host, int action, Bundle args) {
                                int fromPosition = recyclerView.getChildLayoutPosition(host);
                                if (action == R.id.move_card_down_action) {
                                    swapCards(fromPosition, fromPosition + 1, cardAdapter);
                                    return true;
                                } else if (action == R.id.move_card_up_action) {
                                    swapCards(fromPosition, fromPosition - 1, cardAdapter);
                                    return true;
                                }

                                return super.performAccessibilityAction(host, action, args);
                            }
                        };
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CARD_CONFIG",saveCardConfig());
        editor.apply();
    }

    private static int[] generateCardNumbers() {
        int[] cardNumbers = new int[CARD_COUNT];
        for (int i = 0; i < CARD_COUNT; i++) {
            cardNumbers[i] = i + 1;
        }
        return cardNumbers;
    }



    private class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int[] cardNumbers;
        private int colorIndex=0;

        private ItemTouchHelper itemTouchHelper;

        private CardAdapter(int[] cardNumbers) {
            this.cardNumbers = cardNumbers;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.menu_card, parent, false);
            long height = getResources().getDisplayMetrics().heightPixels;
            float dip = 164f;
            Resources r = getResources();
            long px = (long) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dip,
                    r.getDisplayMetrics()
            );

            int final_height = (int) ((height-px)/3);
            ViewGroup.LayoutParams par = view.getLayoutParams();
            par.height=final_height;
            //view.setMinimumHeight((int) ((height-px)/3));
            view.setBackgroundColor(Color.parseColor(CARD_COLOR[CARD_CONFIG[colorIndex]]));
            colorIndex+=1;
            return new CardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            ((CardViewHolder) viewHolder).bind(cardNumbers[position], itemTouchHelper);
        }


        @Override
        public int getItemCount() {
            return cardNumbers.length;
        }

        private void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
            this.itemTouchHelper = itemTouchHelper;
        }

        private class CardViewHolder extends RecyclerView.ViewHolder {

            private final TextView titleView;
            private final View dragHandleView;

            private CardViewHolder(View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.cat_card_list_item_title);
                dragHandleView = itemView.findViewById(R.id.cat_card_list_item_drag_handle);
            }

            @SuppressLint("ClickableViewAccessibility")
            private void bind(int cardNumber, final ItemTouchHelper itemTouchHelper) {
                titleView.setText(CARD_TEXT[CARD_CONFIG[cardNumber-1]]);
                ImageView img = (ImageView)dragHandleView;
                img.setImageResource(CARD_DRAWABLE[CARD_CONFIG[cardNumber-1]]);
                dragHandleView.setOnDragListener(
                        (v, event) -> {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                itemTouchHelper.startDrag(CardViewHolder.this);
                                return true;
                            }
                            return false;
                        });
                dragHandleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CardClickCallback(CARD_CONFIG[cardNumber-1]);
                    }
                });
            }
        }
    }

    private static class CardItemTouchHelperCallback extends ItemTouchHelper.Callback {

        private static final int DRAG_FLAGS = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
        private static final int SWIPE_FLAGS = 0;

        private final CardAdapter cardAdapter;

        @Nullable private MaterialCardView dragCardView;

        private CardItemTouchHelperCallback(CardAdapter cardAdapter) {
            this.cardAdapter = cardAdapter;
        }

        @Override
        public int getMovementFlags(
                @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(DRAG_FLAGS, SWIPE_FLAGS);
        }


        @Override
        public boolean onMove(
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder,
                @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            swapCards(fromPosition, toPosition, cardAdapter);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);

            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
                dragCardView = (MaterialCardView) viewHolder.itemView;
                dragCardView.setDragged(true);
            } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE && dragCardView != null) {
                dragCardView.setDragged(false);
                dragCardView = null;
            }
        }


    }

    private static void swapCards(int fromPosition, int toPosition, CardAdapter cardAdapter) {

        List<Integer> newList = new ArrayList<>();
        for(int i=0;i<cardAdapter.cardNumbers.length;i++){
            newList.add(cardAdapter.cardNumbers[i]);
        }
        int fromNumber = cardAdapter.cardNumbers[fromPosition];
        newList.remove(fromPosition);
        newList.add(toPosition,fromNumber);

        for(int i=0;i<newList.size();i++){
            cardAdapter.cardNumbers[i]=newList.get(i);
        }
        swapCardConfig(cardAdapter.cardNumbers);
        cardAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    private void CardClickCallback(int cardnumber){
        if(cardnumber==4){
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new SongList()).addToBackStack(null).commit();
        }
    }

    private static void swapCardConfig(int[] adapterConfig){
        for(int i=0;i<adapterConfig.length;i++){
            CARD_CONFIG[i]=adapterConfig[i]-1;
        }
    }

    private String saveCardConfig(){
        StringBuilder str = new StringBuilder();
        for(int i=0;i<CARD_CONFIG.length;i++){
            str.append(CARD_CONFIG[i]).append(",");
        }
        return str.toString();
    }

    private void getCardConfig(String conf){
        StringTokenizer stringTokenizer = new StringTokenizer(conf,",");
        for(int i=0;i<CARD_CONFIG.length;i++){
            CARD_CONFIG[i] = Integer.parseInt(stringTokenizer.nextToken());
        }
    }

}
