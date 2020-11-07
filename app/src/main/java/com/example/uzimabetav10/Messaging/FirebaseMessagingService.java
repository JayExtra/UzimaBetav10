package com.example.uzimabetav10.Messaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.uzimabetav10.R;
import com.example.uzimabetav10.mpesa.Item;
import com.example.uzimabetav10.mpesa.MpesaResponse;
import com.example.uzimabetav10.ui.CoverPayment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.List;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        createNotificationChannel();

        FirebaseApp.initializeApp(this);

        String payload = remoteMessage.getData().get("payload");

        Gson gson = new Gson();

        MpesaResponse mpesaResponse = gson.fromJson(payload, MpesaResponse.class);
        String id = mpesaResponse.getBody().getStkCallback().getCheckoutRequestID();


        if (mpesaResponse.getBody().getStkCallback().getResultCode() != 0) {

            String reason = mpesaResponse.getBody().getStkCallback().getResultDesc();

            CoverPayment.mpesaListener.sendFailed(reason);

            String messageTitle = remoteMessage.getNotification().getTitle();
            String messageBody = remoteMessage.getNotification().getBody();
            String messageIcon = remoteMessage.getNotification().getIcon();
            String click_action = remoteMessage.getNotification().getClickAction();
            String dataMessage = remoteMessage.getData().get("message");
            String dataFrom = remoteMessage.getData().get("from_user_id");
            String dataTo = remoteMessage.getData().get("receiver_id");

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                    .setSmallIcon(R.drawable.ic_allert)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            Intent resultIntent = new Intent (click_action);
            resultIntent.putExtra("message" , dataMessage);
            resultIntent.putExtra("from_user_id" , dataFrom);
            resultIntent.putExtra("receiver_id" , dataTo);


            PendingIntent resultPendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            mBuilder.setContentIntent(resultPendingIntent);


            int mNotificationId = (int) System.currentTimeMillis();

            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            mNotifyMgr.notify(mNotificationId , mBuilder.build() );




        } else {

            List<Item> list = mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItem();

            String receipt = "";
            String date = "";
            String phone = "";
            String amount = "";


            for (Item item : list) {

                if (item.getName().equals("MpesaReceiptNumber")) {
                    receipt = item.getValue();
                }
                if (item.getName().equals("TransactionDate")) {
                    date = item.getValue();
                }
                if (item.getName().equals("PhoneNumber")) {
                    phone = item.getValue();

                }
                if (item.getName().equals("Amount")) {
                    amount = item.getValue();
                }

            }
            CoverPayment.mpesaListener.sendSuccesfull(amount, phone, date, receipt);
            Log.d(
                    "MetaData",
                    "\nReceipt: $receipt\nDate: ${getDate(date)}\nPhone: $phone\nAmount: $amount"
            );
            //Log.d("NewDate", getDate(date.toLong()))
        }

        FirebaseMessaging.getInstance()
                .unsubscribeFromTopic(id);





    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.default_notification_channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}
