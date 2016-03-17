package com.example.alexpotter.movies;

public class Film {
    protected String title;
    protected String plot;
    protected String releaseDate;
    protected String posterUrl;

    /**
     * @param filmTitle
     * @param filmPlot
     * @param filmReleaseDate
     * @param filmPosterUrl
     */
    Film (String filmTitle, String filmPlot, String filmReleaseDate, String filmPosterUrl) {
        this.title = filmTitle;
        this.plot = filmPlot;
        this.releaseDate = filmReleaseDate;
        this.posterUrl = filmPosterUrl;
    }

    public String getTitle() {
        return this.title;
    }

    public String getFilmPlot() {
        return this.plot;
    }

    public String getReleaseDate() {
        return this.releaseDate;
    }

    public String getPosterUrl() {
        return this.posterUrl;
    }
}
