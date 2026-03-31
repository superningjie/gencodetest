package cds0.opmc.cea.business.bo;

public class AssObjScoItemInstEntityBO {
    private Long id;
    private String scoreValue;
    private String levelEntry;
    private String levelScore;
    private String mapScore;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(String scoreValue) {
        this.scoreValue = scoreValue;
    }

    public String getLevelEntry() {
        return levelEntry;
    }

    public void setLevelEntry(String levelEntry) {
        this.levelEntry = levelEntry;
    }

    public String getLevelScore() {
        return levelScore;
    }

    public void setLevelScore(String levelScore) {
        this.levelScore = levelScore;
    }

    public String getMapScore() {
        return mapScore;
    }

    public void setMapScore(String mapScore) {
        this.mapScore = mapScore;
    }
}
