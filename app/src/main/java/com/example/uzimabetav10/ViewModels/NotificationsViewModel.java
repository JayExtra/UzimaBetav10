package com.example.uzimabetav10.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.uzimabetav10.Messaging.NotificationsConstructor;
import com.example.uzimabetav10.Repositories.FirebaseNotificationRepository;

import java.util.List;

public class NotificationsViewModel extends ViewModel implements FirebaseNotificationRepository.OnFirestoreTaskComplete {

    private MutableLiveData<List<NotificationsConstructor>> notListModelData = new MutableLiveData<>();

    private FirebaseNotificationRepository mFirebaseNotificationRepository = new FirebaseNotificationRepository(this);

    public LiveData<List<NotificationsConstructor>> getNotListModelData() {
        return notListModelData;
    }


    public NotificationsViewModel(){

        mFirebaseNotificationRepository.getNotificationData();

    }

    @Override
    public void notListDataAdded(List<NotificationsConstructor> notListModelList) {

        notListModelData.setValue(notListModelList);

    }

    @Override
    public void onError(Exception e) {



    }
}
