package ch.ipa.jacqueline.backend.entities;

public enum TaskStatus {
    OFFEN("Offen"),
    IN_BEARBEITUNG("In Bearbeitung"),
    ABGESCHLOSSEN("Abgeschlossen");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}