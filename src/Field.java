/**
 * Represents one field of an instruction
 */
public class Field {
    private String name;
    private int size;
    private Integer value;

    public Field(String name, Integer value) {
        this.name = name;
        this.value = value;
        switch (name) {
            case "op":
            case "func":
                this.size = 6;
                break;
            case "immediate":
                this.size = 16;
                break;
            case "address":
                this.size = 26;
                break;
            default:
                this.size = 5;
                break;
        }
    }
    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public Integer getValue() {
        return value;
    }

}
