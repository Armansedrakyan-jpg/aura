package com.example.auraclone.dto;

/**
 * Результат анализа "ауры" — возвращаем на фронт (веб/Android).
 * Поля соответствуют сущности AuraAnalysis из ТЗ Aura v1.0.
 */
public class AuraAnalysisResult {

    private int score;              // AURA score, например 0-100
    private String level;           // например "MID", "HIGH", "LEGENDARY"
    private String vibe;            // короткая характеристика вайба
    private String energy;          // характеристика энергии
    private String confidence;      // уровень уверенности
    private String personality;     // черты личности
    private String verdict;         // итоговый вердикт/вывод

    public AuraAnalysisResult() {
    }

    public AuraAnalysisResult(int score, String level, String vibe, String energy,
                               String confidence, String personality, String verdict) {
        this.score = score;
        this.level = level;
        this.vibe = vibe;
        this.energy = energy;
        this.confidence = confidence;
        this.personality = personality;
        this.verdict = verdict;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getVibe() {
        return vibe;
    }

    public void setVibe(String vibe) {
        this.vibe = vibe;
    }

    public String getEnergy() {
        return energy;
    }

    public void setEnergy(String energy) {
        this.energy = energy;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }
}
