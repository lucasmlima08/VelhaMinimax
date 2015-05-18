package com.uepb.ia.velhaminimax;

import android.content.Context;
import android.media.MediaPlayer;

public class Audio {

    public static Context context;

    // ------------------------------------------------------------------------------------------ //

    public static MediaPlayer music;

    public static void playBackgroundMusic(){
        int audio = context.getResources().getIdentifier("musica_de_fundo", "raw", context.getPackageName());
        music = MediaPlayer.create(context, audio);
        music.setLooping(true);
        music.start();
    }

    public static void pauseBackgroundMusic(){
        if (music.isPlaying())
            music.pause();
    }

    public static void unpauseBackgroundMusic(){
        if (!music.isPlaying())
            music.start();
    }

    public static void restartBackgroundMusic(){
        music.stop();
        music.setLooping(true);
        music.start();
    }

    public static void stopBackgroundMusic(){
        if (music.isPlaying()){
            music.stop();
        }
    }

    // ------------------------------------------------------------------------------------------ //

    public static MediaPlayer soundEffect;

    public static void playSoundEffect(String toque){
        try {
            soundEffect = MediaPlayer.create(context, getEffect(toque, context));
            soundEffect.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopSoundEffect();
                    soundEffect = null;
                }
            });
            soundEffect.start();
        } catch (Exception e) {}
    }

    // Retorna o RAW
    public static int getEffect(String toque, Context context){
        int sound = context.getResources().getIdentifier(toque, "raw", context.getPackageName());
        return sound;
    }

    public static void stopSoundEffect(){
        if ((soundEffect != null) && (soundEffect.isPlaying())){
            soundEffect.stop();
            soundEffect.release();
        }
    }
}
