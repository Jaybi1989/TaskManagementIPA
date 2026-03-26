package ch.ipa.jacqueline.backend.entities;

public enum TaskPriority {
    NIEDRIG("Niedrig"),
    MITTEL("Mittel"),
    HOCH("Hoch");

    private final String label;

    TaskPriority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}