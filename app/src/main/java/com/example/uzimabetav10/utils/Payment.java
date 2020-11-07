package com.example.uzimabetav10.utils;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class Payment {

    @DocumentId
    private String docId;

    private String amount;

    private String receipt;

    private String phone;
    private Date timestamp;

    public Payment(){


    }

    public Payment(String docId, String amount, String receipt, String phone, Date timestamp) {
        this.docId = docId;
        this.amount = amount;
        this.receipt = receipt;
        this.phone = phone;
        this.timestamp = timestamp;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
