package com.counterfiet.finalproject.ui.advance.view;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {

    private File file;
    private AdvanceView advanceView;
    private static int DEFAULT_BUFFER_SIZE=4096;

    public ProgressRequestBody(File file, AdvanceView advanceView) {
        this.file = file;
        this.advanceView = advanceView;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse("image/*");
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long file_length=file.length();
        byte[] buffer=new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in=new FileInputStream(file);
        long uploaded=0;
        try {
            int read;
            Handler handler=new Handler(Looper.getMainLooper());
            while((read=in.read(buffer))!=-1){
                handler.post(new ProgressUpdater(uploaded,file_length));
                uploaded+=read;
                sink.write(buffer,0,read);
            }
        }finally {
            in.close();
        }
    }

    private class ProgressUpdater implements Runnable {
        private long uploaded;
        private long filelength;

        public ProgressUpdater(long uploaded, long filelength) {
            this.uploaded = uploaded;
            this.filelength = filelength;
        }

        @Override
        public void run() {
            advanceView.onProgressUpdate((int)(100*uploaded/filelength));
        }
    }
}
