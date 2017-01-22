package com.vicpin.cleanrecyclerview.sample.view.presenter;

import com.vicpin.cleanrecyclerview.sample.model.Item;
import com.vicpin.presenteradapter.ViewHolderPresenter;

/**
 * Created by Alvaro on 21/12/2016.
 */

public class AdapterItemPresenter extends ViewHolderPresenter<Item, AdapterItemPresenter.View> {

    @Override public void onCreate() {
        setContent();
    }

    private void setContent() {
        getView().setTitle(getData().getTitle());
        getView().setDescription(getData().getDescription());
        getView().setImage(getData().getImageUrl());
    }

    public interface View {
        void setTitle(String title);

        void setDescription(String description);

        void setImage(String url);
    }
}
