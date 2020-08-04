package com.example.habobarberinterface.Common;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect,  View view,
                               RecyclerView parent,  RecyclerView.State state) {

        outRect.left = space;
        outRect.top = space;
        outRect.bottom = space;
        outRect.right = space;
    }
}

