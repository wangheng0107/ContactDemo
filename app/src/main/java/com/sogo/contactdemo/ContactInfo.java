package com.sogo.contactdemo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzheng on 2017/4/18.
 */

public class ContactInfo {
    public String id;
    public String contactsId;
    public String n;/*name*/
    public String[] p;/*phone*/
    public String l;

    public int deleted;
    public int timesContacted;
    public int starred;
    public int pinned;
    public int dirty;
    public String deletedTime;
    public String lastTimeContacted;
    public String customRingtone;
    public String photoUri;
    public String accountName;
    public String accountType;

    private transient List<String> phoneList = new ArrayList<>();

    public ContactInfo() {
    }

    public void addPhoneNumber(String phoneNumber) {
        this.phoneList.add(phoneNumber);
        this.p = this.phoneList.toArray(new String[phoneList.size()]);
    }
}
