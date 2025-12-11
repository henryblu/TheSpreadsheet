package spreadsheet.formula;

import java.util.List;

import spreadsheet.formula.ast.ExpressionNode;
import spreadsheet.formula.eval.CellLookup;
import spreadsheet.formula.eval.FormulaEvaluator;
import spreadsheet.formula.lexer.FormulaTokenizer;
import spreadsheet.formula.lexer.Token;
import spreadsheet.formula.parser.ShuntingYardParser;

// Minimal facade that keeps callers unaware of tokenizer/evaluator wiring based on class discussion

public final class FormulaEngine {
    public static double evaluate(String expression, CellLookup lookup) {
        List<Token> tokens = FormulaTokenizer.tokenize(expression);
        ExpressionNode ast = ShuntingYardParser.parse(tokens);
        return FormulaEvaluator.evaluate(ast, lookup);
    }
}
