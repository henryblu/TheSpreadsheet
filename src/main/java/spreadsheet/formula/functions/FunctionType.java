package spreadsheet.formula.functions;

public enum FunctionType {
    SUM,
    MIN,
    MAX,
    AVERAGE;

    public static FunctionType fromName(String name) {
        String upper = name.toUpperCase();
        switch (upper) {
            case "SUM":
                return SUM;
            case "MIN":
                return MIN;
            case "MAX":
                return MAX;
            case "AVERAGE":
                return AVERAGE;
            default:
                throw new IllegalArgumentException("Unknown function: " + name);
        }
    }
}
