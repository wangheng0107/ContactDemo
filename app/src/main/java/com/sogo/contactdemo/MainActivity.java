package com.sogo.contactdemo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.sogo.contactdemo.permissions.AfterPermissionGranted;
import com.sogo.contactdemo.permissions.AppOpsPermissions;
import com.sogo.contactdemo.permissions.EasyPermissions;
import com.sogo.contactdemo.permissions.PermissionCallback;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tabMode).setOnClickListener(this);
        doBorrow();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tabMode:
                doBorrow();
                break;
        }
    }

    private void doBorrow() {
        /*1,去依次串行获取 通讯录 通话记录 短信。中间可能弹出权限提醒
         * 对获取到的数据如果通讯录或者通话记录为0条，则终止流程，toast提示。
         * 获取成功的话则上传，上传结果是否成功不作为能否借款的条件。*/
        checkContactsPermissionAndCollect(new PermissionCallback() {
            @Override
            public void onPermissionGranted() {
            }

            @Override
            public void onPermissionDenied() {

            }
        });
    }


    @AfterPermissionGranted(EasyPermissions.REQUEST_CODE_READ_CONTACTS)
    private void checkContactsPermissionAndCollect(final PermissionCallback callback) {
        String[] providerPerm = {Manifest.permission.READ_CONTACTS};

        if (EasyPermissions.hasPermissions(this, providerPerm)) {
            if (AppOpsPermissions.hasContactPermission(this, "android:read_contacts")) {
                Observable.create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        /**1,获取通讯录信息*/
                        List<ContactInfo> contactInfos = getContacts(MainActivity.this);
                        if (contactInfos != null && !contactInfos.isEmpty()) {
                            Gson gson = new Gson();
                            String gsonStirng = gson.toJson(contactInfos);
                            Log.e("MainActivity", "通讯录数据：" + gsonStirng);
                            subscriber.onNext(true);
                        } else {
                            subscriber.onNext(false);
                        }
                    }
                }).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Boolean>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Boolean aBoolean) {
                                if (callback != null) {
                                    if (aBoolean) {
                                        callback.onPermissionGranted();
                                    } else {
                                        callback.onPermissionDenied();
                                    }
                                }
                            }
                        });

            } else {
                if (callback != null) {
                    callback.onPermissionDenied();
                }
            }

        } else {
            EasyPermissions.requestPermissions(this,
                    EasyPermissions.REQUEST_CODE_READ_CONTACTS,
                    providerPerm);
        }
    }

    public static List<ContactInfo> getContacts(Context context) {
        long timeOld = System.currentTimeMillis();
        List<ContactInfo> infos = new ArrayList<>();
        //获取联系人信息的Uri
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        //获取ContentResolver
        ContentResolver contentResolver = context.getContentResolver();
        try {
            //继续解析
            Cursor rawCursor = contentResolver.query(ContactsContract.RawContacts.CONTENT_URI, null,
                    null, null, null);
            if (rawCursor != null && rawCursor.getCount() > 0) {
                while (rawCursor.moveToNext()) {
                    ContactInfo contactInfo = new ContactInfo();
                    String rawContactId = rawCursor.getString(rawCursor.getColumnIndex(ContactsContract.RawContacts._ID));
                    String contactId = rawCursor.getString(rawCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                    String displayName = rawCursor.getString(rawCursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
                    String account_name = rawCursor.getString(rawCursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                    String account_type = rawCursor.getString(rawCursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                    String customRingtone = rawCursor.getString(rawCursor.getColumnIndex(ContactsContract.RawContacts.CUSTOM_RINGTONE));
                    String lastTimeContacted = rawCursor.getString(rawCursor.getColumnIndex(ContactsContract.RawContacts.LAST_TIME_CONTACTED));
                    //0为默认 1 表示这行数据已经删除
                    int deleted = rawCursor.getInt(rawCursor.getColumnIndex(ContactsContract.RawContacts.DELETED));
                    //是否收藏（1收藏，0没收藏）
                    int starred = rawCursor.getInt(rawCursor.getColumnIndex(ContactsContract.RawContacts.STARRED));
                    //version变化，值为1，需同步数据
                    int dirty = rawCursor.getInt(rawCursor.getColumnIndex(ContactsContract.RawContacts.DIRTY));
                    //与该联系人联系的次数
                    int timesContacted = rawCursor.getInt(rawCursor.getColumnIndex(ContactsContract.RawContacts.TIMES_CONTACTED));
                    int pinned = 0;//是否被固定
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        pinned = rawCursor.getInt(rawCursor.getColumnIndex(ContactsContract.RawContacts.PINNED));
                    }
                    contactInfo.accountName = account_name;
                    contactInfo.accountType = account_type;
                    contactInfo.customRingtone = customRingtone;
                    contactInfo.lastTimeContacted = lastTimeContacted;
                    contactInfo.deleted = deleted;
                    contactInfo.starred = starred;
                    contactInfo.timesContacted = timesContacted;
                    contactInfo.dirty = dirty;
                    contactInfo.pinned = pinned;
                    contactInfo.n = displayName;
                    contactInfo.id = rawContactId;
                    contactInfo.contactsId = contactId;

                    Cursor cursor = contentResolver.query(uri, null,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?", new String[]{displayName}, null);

                    if (cursor != null && cursor.getCount() > 0) {
                        ContactInfo lastContactInfo = null;
                        while (cursor.moveToNext()) {
                            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String contactsId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                            String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                            final int latestModifyDate = cursor.getColumnIndex("contact_last_updated_timestamp"); //API 18 才有这列
                            String latestModifyStr = "0";//加个默认值，表示为空
                            if (latestModifyDate > -1) {
                                latestModifyStr = cursor.getString(latestModifyDate);
                            }
                            contactInfo.l = latestModifyStr;
                            contactInfo.photoUri = photoUri;
                            if (lastContactInfo != null && TextUtils.equals(lastContactInfo.contactsId, contactsId)) {
                                lastContactInfo.addPhoneNumber(phoneNumber);//可能为多个号码
                            } else {
                                contactInfo.addPhoneNumber(phoneNumber);//添加电话
                                lastContactInfo = contactInfo;
                            }
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }

                    //删除时间
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        Cursor deletedCursor = contentResolver.query(ContactsContract.DeletedContacts.CONTENT_URI, null,
                                ContactsContract.DeletedContacts.CONTACT_ID + "=?", new String[]{rawContactId}, null);

                        if (deletedCursor != null && deletedCursor.getCount() > 0) {
                            if (deletedCursor.moveToNext()) {
                                String deletedCursorString = deletedCursor.getString(deletedCursor.getColumnIndex(ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP));
                                contactInfo.deletedTime = deletedCursorString;
                            }
                        }
                        if (deletedCursor != null) {
                            deletedCursor.close();
                        }
                    }

                    infos.add(contactInfo);
                }
            }

            if (rawCursor != null) {
                rawCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        long timeNew = System.currentTimeMillis();
        Log.e("MainActivity", "查询通讯录数据库时间:" + (timeNew - timeOld));
        return infos;
    }

}
