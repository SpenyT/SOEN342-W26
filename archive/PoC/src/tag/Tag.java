package tag;

public class Tag {
    private final String name;

    public Tag(String name) {
        this.name = name.toLowerCase().trim();
    }

    public String getName() { return name; }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        return name.equals(((Tag) o).name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }
}
