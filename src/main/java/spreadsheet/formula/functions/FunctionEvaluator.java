package spreadsheet.formula.functions;

import java.util.List;

public final class FunctionEvaluator {
    private FunctionEvaluator() {
    }

    public static double evaluate(FunctionType type, List<Double> values) {
        // simple evaluator for basic functions
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Function has no numeric values");
        }
        switch (type) {
            case SUM:
                double sum = 0.0;
                for (double v : values) {
                    sum += v;
                }
                return sum;
            case MIN:
                double min = values.get(0);
                for (double v : values) {
                    min = Math.min(min, v);
                }
                return min;
            case MAX:
                double max = values.get(0);
                for (double v : values) {
                    max = Math.max(max, v);
                }
                return max;
            case AVERAGE:
                double total = 0.0;
                for (double v : values) {
                    total += v;
                }
                return total / values.size();
            default:
                throw new IllegalStateException("Unknown function type: " + type);
        }
    }
}
