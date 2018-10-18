package com.hlzx.wenutil.dialog.interfaces;

/**
 * Created by alan on 2016/3/15.
 */
public interface IOperation {

    /**
     * cancel the Operation.
     */
    void onCanceled();

    /**
     * get the photo by opening Album.
     */
    void onOpenAlbum();

    /**
     * take photo.
     */
    void onTakePhoto();

}
