package nl.kik.commons.datastation.dto.nuts;

public enum Visibility {
    Private("private"), Public("public");

    private String name;

    Visibility(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
