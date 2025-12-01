package org.example.htmlfx.toolkits;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class Music {
    private MediaPlayer mediaPlayer;

    public Music() {
        try {
            // Sử dụng đường dẫn tệp hợp lệ (URI)
            String filePath = "src/main/resources/music/Chrismax.wav";
            File file = new File(filePath);

            if (!file.exists()) {
                throw new IllegalArgumentException("Tệp âm thanh không tồn tại: " + filePath);
            }

            Media media = new Media(file.toURI().toString());
            this.mediaPlayer = new MediaPlayer(media);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Không thể khởi tạo MediaPlayer. Kiểm tra đường dẫn tệp âm thanh!");
        }
    }

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }

    public double getVolume() {
        return mediaPlayer != null ? mediaPlayer.getVolume() : 0.0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
    }
}