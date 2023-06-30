package com.example.smsspamclassification;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class ReceiveSMS extends BroadcastReceiver
{
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
        {
            SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            StringBuilder messageBody = new StringBuilder();
            String senderNo = null;

            for (SmsMessage message : smsMessages)
            {
                senderNo = message.getDisplayOriginatingAddress();
                messageBody.append(message.getMessageBody());
            }

            new PostRequest(context, messageBody.toString(), senderNo, false).getResponse();
        }
    }
}
