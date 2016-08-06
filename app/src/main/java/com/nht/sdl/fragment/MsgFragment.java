package com.nht.sdl.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nht.sdl.CustomGalleryActivity;
import com.nht.sdl.R;
import com.nht.sdl.db.MessagesDB;
import com.nht.sdl.gallery.Action;
import com.nht.sdl.gallery.CustomGallery;
import com.nht.sdl.gallery.GalleryAdapter;
import com.nht.sdl.widget.MyGridView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MsgFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MsgFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    public static final int REQUEST_PIC_LOCAL = 201;
    @BindView(R.id.etName)
    EditText etName;
    @BindView(R.id.etContent)
    EditText etContent;
    @BindView(R.id.etOwnerId)
    EditText etOwnerId;
    @BindView(R.id.gvPhoto)
    MyGridView gvPhoto;
    @BindView(R.id.btnPic)
    Button btnPic;
    @BindView(R.id.btnSave)
    Button btnSave;

    MessagesDB circlesDB;

    private GalleryAdapter adapter;
    ArrayList<CustomGallery> picList;

    private String PHOTO_NAME;
    private String PHOTO_FOLDER;
    SimpleDateFormat dateFormat;

    // TODO: Rename and change types of parameters
    private String mParam1;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment MsgFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MsgFragment newInstance(String param1) {
        MsgFragment fragment = new MsgFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public static MsgFragment newInstance() {
        return new MsgFragment();
    }

    public MsgFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        adapter = new GalleryAdapter(getActivity(), ImageLoader.getInstance());
        adapter.setMultiplePick(false);
        picList = new ArrayList<>();
        adapter.addAll(picList);

        PHOTO_FOLDER = new File(Environment.getExternalStorageDirectory(), "").getPath() + "/Sdl/Camera/";
        PHOTO_NAME = "";
        File file = new File(PHOTO_FOLDER);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_msg, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gvPhoto.setAdapter(adapter);
    }

    @OnClick({R.id.btnSave, R.id.btnPic})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSave:
                saveToDb();

                break;
            case R.id.btnPic:
                showPicAlert();
                break;
        }
    }

    private void saveToDb() {

        ContentValues values = new ContentValues();
        values.put("name",etName.getText().toString());
        values.put("content",etContent.getText().toString());
        values.put("ownerId",etOwnerId.getText().toString());
        values.put("timestamp",String.valueOf(System.currentTimeMillis()));
        values.put("pictures",adapter.getDataString());
        if (circlesDB == null) {
            circlesDB = new MessagesDB(getActivity());
        }
        boolean save = circlesDB.save(values);
        if (save){
            Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_SHORT).show();
            clearData();
        }else {
            Toast.makeText(getActivity(), "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPicAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] mode = {"本地图片", "相机"};
        builder.setSingleChoiceItems(mode, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e("which", Integer.toString(which));
                switch (which) {
                    case 0:
                        getFromLocal();
                        break;
                    case 1:
                        getFromCamera();
                        break;
                }
                dialog.cancel();
            }
        });
        builder.setCustomTitle(null);
        builder.show();
    }

    private void clearData() {
        etName.setText(null);
        etContent.setText(null);
        etOwnerId.setText("1234");
        picList.clear();
        adapter.addAll(picList);
    }
    private void getFromCamera() {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        }
        PHOTO_NAME = "MyPic" + dateFormat.format(System.currentTimeMillis()) + ".jpg";
        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(PHOTO_FOLDER + PHOTO_NAME)));
        startActivityForResult(intent2, Activity.DEFAULT_KEYS_DIALER);
    }

    private void getFromLocal() {
        Intent intent = new Intent(Action.ACTION_MULTIPLE_PICK);
        intent.setClass(getActivity(), CustomGalleryActivity.class);
        startActivityForResult(intent, REQUEST_PIC_LOCAL);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("onActivityResult", "onActivityResult");
        switch (requestCode) {
            case Activity.DEFAULT_KEYS_DIALER:
                String imgurl = PHOTO_FOLDER + PHOTO_NAME;
                File imgFile = new File(imgurl);
                if (imgFile.exists() && Activity.RESULT_OK == resultCode) {
                    CustomGallery pic = new CustomGallery();
                    pic.sdcardPath = imgurl;
                    picList.add(pic);
                    adapter.addAll(picList);
                } else {
                    Toast.makeText(getActivity(), "你取消了拍照", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_PIC_LOCAL:
                if (data != null && data.hasExtra("all_path") && data.getStringArrayExtra("all_path").length > 0) {
                    int picCount = picList.size();

                    String[] all_path = data.getStringArrayExtra("all_path");
                    if (picCount + all_path.length > 9) {
                        Toast.makeText(getActivity(), "图片总数超过9张，请重选", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    CustomGallery item;
                    for (String path : all_path) {
                        item = new CustomGallery();
                        item.sdcardPath = path;
                        picList.add(item);
                    }

                    adapter.addAll(picList);

                } else if (data != null && data.hasExtra("all_path") && data.getStringArrayExtra("all_path").length == 0) {
                    Toast.makeText(getActivity(), "你未选择图片", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
