package com.example.myapplication.fragment.biblegames;

public class ModelGames {
    private String verseName;
    private String verseText;

    public ModelGames(String verseName, String verseText) {
        this.verseName = verseName;
        this.verseText = verseText;
    }

    public String getVerseName() {
        return verseName;
    }

    public void setVerseName(String verseName) {
        this.verseName = verseName;
    }

    public String getVerseText() {
        return verseText;
    }

    public void setVerseText(String verseText) {
        this.verseText = verseText;
    }
}
