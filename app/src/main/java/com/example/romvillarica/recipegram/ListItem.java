package com.example.romvillarica.recipegram;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Rom Villarica on 11/19/2017.
 */

public class ListItem {
    private String nameOfPoster;
    private String uniqueId;
    private String date;
    private String image1Url;
    private String image2Url;
    private String description;
    private String instructions;
    private double rating;
    private int ratingCount;

    public String getNameOfPoster() {
        return nameOfPoster;
    }

    public void setNameOfPoster(String nameOfPoster) {
        this.nameOfPoster = nameOfPoster;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateInReadableForm() {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            Date d = df.parse(date);
            DateFormat df2 = new SimpleDateFormat("EEE MMM dd");
            return df2.format(d);
        }
        catch (Exception e) {
            return "Failure to parse!";
        }
    }

    public String getImage1Url() {
        return image1Url;
    }

    public void setImage1Url(String imageUrl) {
        image1Url = imageUrl;
    }

    public String getImage2Url() {
        return image2Url;
    }

    public void setImage2Url(String imageUrl) {
        image2Url = imageUrl;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
        ratingCount = 1;
    }

    public void addRating(double rating) {
        double currTotal = this.rating * ratingCount + rating;
        ratingCount += 1;
        this.rating = currTotal / ratingCount;
    }
}
