package com.merseyside.admin.player.Utilities;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

/**
 * Created by Admin on 26.12.2016.
 */

public class TrackDecoder extends AsyncTask<Void, Void, Void> {
    private final static int SILENCE_MINUS_EDGE = -1000;
    private final static int SILENCE_PLUS_EDGE = 1000;

    private final static int QUARANTEE = 100;

    private final static int KOEF = 88;

    private int silencePointMS;
    private String path;
    private int duration;
    MySilenceFindListener mySilenceFindListener;

    public interface MySilenceFindListener{
        public void silenceFound(int silencePointMS);
        public void silenceNotFound(int silencePointMS);
    }

    public TrackDecoder(String path, int duration){
        this.path = path;
        this.duration = duration;
    }

    public void setMySilenceFindListener(MySilenceFindListener mySilenceFindListener){
        this.mySilenceFindListener = mySilenceFindListener;
    }

    public short[] decode(String path, int startMs, int maxMs) throws FileNotFoundException, IOException, DecoderException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);

        float totalMs = 0;
        boolean seeking = true;

        File file = new File(path);
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file), 8 * 1024);
        } catch (FileNotFoundException ignored){
            throw new FileNotFoundException();
        }
        try {
            Bitstream bitstream = new Bitstream(inputStream);
            Decoder decoder = new Decoder();

            boolean done = false;
            while (! done) {
                Header frameHeader = bitstream.readFrame();
                if (frameHeader == null) {
                    done = true;
                } else {
                    totalMs += frameHeader.ms_per_frame();

                    if (totalMs >= startMs) {
                        seeking = false;
                    }

                    if (! seeking) {
                        SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);

                        if (output.getSampleFrequency() != 44100
                                || output.getChannelCount() != 2) {
                            throw new DecoderException("mono or non-44100 MP3 not supported",  new Throwable());
                        }

                        short[] pcm = output.getBuffer();
                        for (short s : pcm) {
                            outStream.write(s & 0xff);
                            outStream.write((s >> 8 ) & 0xff);
                        }
                    }

                    if (totalMs >= (startMs + maxMs)) {
                        done = true;
                    }
                }
                bitstream.closeFrame();
            }
            byte[] bytes = outStream.toByteArray();
            short[] shorts = new short[bytes.length/2];
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

            return shorts;
        } catch (BitstreamException e) {
            throw new IOException("Bitstream error: " + e);
        } catch (DecoderException e) {
            throw new DecoderException("mes", e);
        } finally {
            inputStream.close();
        }
    }

    private void analiseResult(short[] shorts){
        if (shorts.length!=0) {
            int count = 0, iterations = 1000, L = 0, R = shorts.length, k = 0;
            for (int i = 0; i < iterations; i++) {
                k = (L + R) / 2;
                if (shorts[k] > SILENCE_MINUS_EDGE && shorts[k] < SILENCE_PLUS_EDGE) R = k;
                else L = k;
            }
            try {
                for (int i = k; i < k + 1000; i++) {
                    if (shorts[i] > SILENCE_MINUS_EDGE && shorts[i] < SILENCE_PLUS_EDGE) {
                        count++;

                        if (count == QUARANTEE) {

                            silencePointMS = i / KOEF;
                            return;
                        }
                    } else {
                        count = 0;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                silencePointMS = -1;
                return;
            }
            int i;
            for (i = shorts.length - 1; i >= 0; i--) {
                if (shorts[i] < SILENCE_MINUS_EDGE || shorts[i] > SILENCE_PLUS_EDGE) break;
            }
            if (shorts.length - i < QUARANTEE) silencePointMS = -1;
            else silencePointMS = i / KOEF;
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            analiseResult(decode(path, (duration-Settings.SILENCE_END_DURATION), Settings.SILENCE_END_DURATION));
        } catch (FileNotFoundException e){
            silencePointMS = -1;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (silencePointMS!=-1)  mySilenceFindListener.silenceFound(Settings.SILENCE_END_DURATION - silencePointMS);
        else mySilenceFindListener.silenceNotFound(0);
    }
}
