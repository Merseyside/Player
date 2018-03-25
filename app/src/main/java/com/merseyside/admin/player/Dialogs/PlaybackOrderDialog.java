package com.merseyside.admin.player.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Window;

import com.merseyside.admin.player.ActivitesAndFragments.PlaylistTracks;
import com.merseyside.admin.player.AdaptersAndItems.ItemAdapter;
import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.MySwipeRefreshLayout;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;

/**
 * Created by Admin on 06.01.2017.
 */

public class PlaybackOrderDialog extends Dialog{

    private ArrayList<Track> list;
    private Context context;
    private MyOrderDialogListener myOrderDialogListener;

    private DragListView mDragListView;
    private MySwipeRefreshLayout mRefreshLayout;
    private ItemAdapter listAdapter;

    private boolean isDragged = false;
    private boolean isCurrentTrackDragged = false;

    private int currentPosition = 0;

    public interface MyOrderDialogListener{
        void itemClicked(ArrayList<Track> list, boolean isChanged, int currentPosition);
        void playlistChanged(ArrayList<Track> list);
        void currentTrackDragged(int newPosition, ArrayList<Track> list);
    }

    public PlaybackOrderDialog(Context context, ArrayList<Track> list, int position, MyOrderDialogListener myOrderDialogListener){
        super(context);
        this.list = list;
        this.context = context;
        this.myOrderDialogListener = myOrderDialogListener;
        this.currentPosition = position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.order_dialog);

        mRefreshLayout = (MySwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRefreshLayout.setEnabled(false);
        mDragListView = (DragListView) findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
            }
            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition == currentPosition) {
                    currentPosition = toPosition;
                    isCurrentTrackDragged = true;
                }else if (fromPosition<=currentPosition && toPosition>=currentPosition) {
                    currentPosition--;
                    isCurrentTrackDragged = true;
                }
                else if (fromPosition >= currentPosition && toPosition <= currentPosition){
                    currentPosition++;
                    isCurrentTrackDragged = true;
                }
                isDragged = true;
                Log.d("Drag", "from = " + fromPosition + " to = " + toPosition + " current = " + currentPosition);
            }
        });

        mRefreshLayout.setScrollingView(mDragListView.getRecyclerView());
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(context, R.color.black));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        setupListRecyclerView();
        if (currentPosition-2 > 0) mDragListView.getRecyclerView().scrollToPosition(currentPosition-2);
    }

    private void setupListRecyclerView() {
        mDragListView.setLayoutManager(new LinearLayoutManager(context));
        ArrayList<Pair<Long, Track>> valid_list = PlaylistTracks.getValidList(list);
        listAdapter = new ItemAdapter(context, valid_list, R.layout.dialog_track_view, R.id.drag_image, false, currentPosition);
        listAdapter.setOnItemClickListener(new ItemAdapter.ItemClickListener() {
            @Override
            public void itemClicked(int position) {
                listAdapter.deleteOnItemCliickListener();
                playTrack(position);
            }

            @Override
            public void itemLongClicked(final int position) {

            }
        });
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setCustomDragItem(null);

    }

    private void playTrack(int position){
        if (isCurrentTrackDragged) myOrderDialogListener.currentTrackDragged(currentPosition, listAdapter.getAll());
        myOrderDialogListener.itemClicked(listAdapter.getAll(), isDragged, position);
        this.dismiss();
    }


    @Override
    public void onBackPressed() {
        if (isCurrentTrackDragged) myOrderDialogListener.currentTrackDragged(currentPosition, listAdapter.getAll());
        if (isDragged) myOrderDialogListener.playlistChanged(listAdapter.getAll());
        super.onBackPressed();
    }
}
