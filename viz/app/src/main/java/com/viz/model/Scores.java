package com.viz.model;

public class Scores
{
    private String disgust;

    private String sadness;

    private String contempt;

    private String anger;

    private String happiness;

    private String neutral;

    private String surprise;

    private String fear;

    public String getDisgust ()
    {
        return disgust;
    }

    public void setDisgust (String disgust)
    {
        this.disgust = disgust;
    }

    public String getSadness ()
    {
        return sadness;
    }

    public void setSadness (String sadness)
    {
        this.sadness = sadness;
    }

    public String getContempt ()
    {
        return contempt;
    }

    public void setContempt (String contempt)
    {
        this.contempt = contempt;
    }

    public String getAnger ()
    {
        return anger;
    }

    public void setAnger (String anger)
    {
        this.anger = anger;
    }

    public String getHappiness ()
    {
        return happiness;
    }

    public void setHappiness (String happiness)
    {
        this.happiness = happiness;
    }

    public String getNeutral ()
    {
        return neutral;
    }

    public void setNeutral (String neutral)
    {
        this.neutral = neutral;
    }

    public String getSurprise ()
    {
        return surprise;
    }

    public void setSurprise (String surprise)
    {
        this.surprise = surprise;
    }

    public String getFear ()
    {
        return fear;
    }

    public void setFear (String fear)
    {
        this.fear = fear;
    }

    public String getStrongestEmotion(){
        float disgust = Float.valueOf(getDisgust());
        float sadness = Float.valueOf(getSadness());
        float contempt = Float.valueOf(getContempt());
        float anger = Float.valueOf(getAnger());
        float happiness = Float.valueOf(getHappiness());
        float neutral = Float.valueOf(getNeutral());
        float surprise = Float.valueOf(getSurprise());
        float fear = Float.valueOf(getFear());

        float max = 0;
        String emotion = "None";

        if (disgust > max) {
            max = disgust;
            emotion = "disgust";
        }
        if (sadness > max) {
            max = sadness;
            emotion = "sadness";
        }
        if (contempt > max) {
            max = contempt;
            emotion = "contempt";
        }
        if (anger > max) {
            max = anger;
            emotion = "anger";
        }
        if (happiness > max) {
            max = happiness;
            emotion = "happiness";
        }
        if (neutral > max) {
            max = neutral;
            emotion = "neutral";
        }
        if (surprise > max) {
            max = surprise;
            emotion = "surprise";
        }
        if (fear > max) {
            max = fear;
            emotion = "fear";
        }

        return emotion;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [disgust = "+disgust+", sadness = "+sadness+", contempt = "+contempt+", anger = "+anger+", happiness = "+happiness+", neutral = "+neutral+", surprise = "+surprise+", fear = "+fear+"]";
    }
}