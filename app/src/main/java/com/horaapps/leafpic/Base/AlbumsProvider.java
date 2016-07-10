package com.horaapps.leafpic.Base;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by weconstudio on 07/07/16.
 */
public class AlbumsProvider {

    //https://github.com/HoraApps/LeafPic/blob/dcbee1b9aeb3f7c7df2c4a20527727819b90584c/app/src/main/java/com/leafpic/app/Base/MediaStoreHandler.java

    private Context context;

    public  AlbumsProvider(Context context) {
        this.context = context;
    }

    public static ArrayList<Album> getMediaStoreAlbums(Context context) {
        ArrayList<Album> list = new ArrayList<Album>();

       CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        ArrayList<String> excludedAlbums =  new ArrayList<String>();//h.getExcludedALbumsIDs();


        String[] projection = new String[]{
                MediaStore.Files.FileColumns.PARENT,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };

        Uri images = MediaStore.Files.getContentUri("external");



        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE +"="+ MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +" or "+
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                + " ) GROUP BY ( "+ MediaStore.Files.FileColumns.PARENT +" ";


        Cursor cur = context.getContentResolver().query(
                images, projection, selection, null, null);

        if(cur != null) {
            if (cur.moveToFirst()) {
                int idColumn = cur.getColumnIndex(MediaStore.Files.FileColumns.PARENT);
                int pathColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                do if (!excludedAlbums.contains(cur.getString(idColumn))){
                    Album album = new Album(cur.getLong(idColumn),
                            cur.getString(pathColumn),
                            getAlbumPhotosCount(context, cur.getLong(idColumn)));
                    //Log.wtf("asd",album.getName() +" - "+ album.getCount());
                    //album.setCoverPath(h.getPhotPrevieAlbum(album.ID));
                  album.media.addAll(getFirstAlbumPhoto(context,album.getId()));
                    list.add(album);
                }
                while (cur.moveToNext());
            }
            cur.close();
        }
        return list;
    }

    private static int getAlbumPhotosCount(Context context, long id) {
        int c = 0;

        Uri images = MediaStore.Files.getContentUri("external");
        String[] projection = new String[]{ MediaStore.Files.FileColumns.PARENT };

        String selection = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +
                                   " or "+ MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ") and " +
                                   MediaStore.Files.FileColumns.PARENT + "='" + id + "'";

        Cursor cur = context.getContentResolver().query(
                images, projection, selection, null, null);
        if (cur != null) {
            c = cur.getCount();
            cur.close();
        }
        return c;
    }

    public static ArrayList<Media> getFirstAlbumPhoto(Context context ,long id) {
        return getAlbumPhotos(context, id, 1, ImageFileFilter.FILTER_ALL);
    }

    public static ArrayList<Media> getAlbumPhotos(Context context,long id, int n, int filter) {

        String limit = n == -1 ? "" : "LIMIT " + n;

        ArrayList<Media> list = new ArrayList<Media>();

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.ORIENTATION
        };

        Uri images = MediaStore.Files.getContentUri("external");
        String selection;/* = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore
                                                                                           .Files.FileColumns.MEDIA_TYPE_IMAGE +
                                   " or " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                                   + ") and " + MediaStore.Files.FileColumns.PARENT + "='" + id +
                                    "'";*/

        switch (filter){

            case ImageFileFilter.FILTER_IMAGES:
                selection = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + ") and " + MediaStore.Files.FileColumns.PARENT + "='" + id + "'";
                break;

            case ImageFileFilter.FILTER_ALL:
            default:
                selection = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " or " +
                                    MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                                    + ") and " + MediaStore.Files.FileColumns.PARENT + "='" + id + "'";
                break;
        }


        Cursor cur = context.getContentResolver().query(
                images, projection, selection, null,
                " " + MediaStore.Images.Media.DATE_TAKEN + " DESC " + limit);

        if (cur != null) {
            if (cur.moveToFirst()) {

                do {
                    Media m = new Media(cur);
                    list.add(m);
                    //Log.wtf("asd", m.getPath() +" - "+ m.getDateModified());
                } while (cur.moveToNext());
            }
            cur.close();
        }
        return list;
    }
//
//    public void getThumnails(){
//
//        String[] projection = new String[]{
//                MediaStore.Images.Thumbnails.DATA,
//                MediaStore.Images.Thumbnails.IMAGE_ID
//        };
//
//        Uri images = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
//        Cursor s = MediaStore.Images.Thumbnails.query(context.getContentResolver(),images,projection);
//        Cursor cur = MediaStore.Images.Thumbnails.query(context.getContentResolver(),images,projection);
//                /*context.getContentResolver().query(
//                images,
//                projection,
//                null, null, null);*/
//        if (cur.moveToFirst()){
//            int pathColumn = cur.getColumnIndex(
//                    MediaStore.Images.Thumbnails.DATA);
//            int ThumbCOlumn = cur.getColumnIndex(
//                    MediaStore.Images.Thumbnails.IMAGE_ID);
//            do {
//                Log.wtf("data",cur.getString(pathColumn));
//                Log.wtf("data-thumb",cur.getString(ThumbCOlumn));
//            }while (cur.moveToNext());
//        }
//        cur.close();
//    }
//
//    public static void deleteAlbumMedia(Album a, Context context1){
//        String[] projection = { MediaStore.Images.Media._ID };
//
//        String selection = MediaStore.Files.FileColumns.PARENT + " = ?";
//        String[] selectionArgs = new String[] { a.id };
//
//        Uri queryUri = MediaStore.Files.getContentUri("external");
//        ContentResolver contentResolver = context1.getContentResolver();
//        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
//        if (c.moveToFirst()) {
//            int columnID = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
//            do {
//                long id = c.getLong(columnID);
//                Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
//                contentResolver.delete(deleteUri, null, null);
//            }while (c.moveToNext());
//        }
//        c.close();
//
//    }
//

//
//    public String getAlbumPhoto(String a) {
//        String asd = null;
//
//        String[] projection = new String[]{
//                MediaStore.Images.Media.DATA,
//                MediaStore.Files.FileColumns.PARENT
//        };
//
//        Uri images = MediaStore.Files.getContentUri("external");
//
//        String selectionImages = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " or " +
//                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
//                + ") and " + MediaStore.Images.ImageColumns.DATA + "='" + a + "'";
//
//        Cursor cur = context.getContentResolver().query(
//                images,
//                projection,
//                selectionImages,
//                null, null);
//
//        if (cur != null) {
//            if (cur.moveToFirst()) {
//                int pathColumn = cur.getColumnIndex(
//                        MediaStore.Files.FileColumns.PARENT);
//                asd =cur.getString(pathColumn);
//            }
//            cur.close();
//        }
//
//        return asd;
//    }
}
