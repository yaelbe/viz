package com.viz.model;


public class EmotionData
{
    private Scores scores;

    private FaceRectangle faceRectangle;

    public Scores getScores ()
    {
        return scores;
    }

    public void setScores (Scores scores)
    {
        this.scores = scores;
    }

    public FaceRectangle getFaceRectangle ()
    {
        return faceRectangle;
    }

    public void setFaceRectangle (FaceRectangle faceRectangle)
    {
        this.faceRectangle = faceRectangle;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [scores = "+scores+", faceRectangle = "+faceRectangle+"]";
    }
}
