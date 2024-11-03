package synth.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Solver;

import synth.cfg.CFG;
import synth.cfg.Terminal;

import static synth.core.Utils.expand;
import static synth.core.Utils.isValid;

public class ConstraintBasedSynthesizer implements ISynthesizer {
    // Counter for the non-terminal symbol E and B
    private static int eCount = 0;
    private static int bCount = 0;
    
    /**
     * Synthesize a program f(x, y, z) based on a context-free grammar and examples 
     * by using the Z3 SMT solver for pruning the search space.
     * 
     * @param cfg      the context-free grammar
     * @param examples a list of examples
     * @return the program or null to indicate synthesis failure
     */
    @Override
    public Program synthesize(CFG cfg, List<Example> examples) {
        // Initialize the work list with the start symbol from CFG
        Queue<ASTNode> workList = new LinkedList<>();
        workList.add(new ASTNode(cfg.getStartSymbol(), Collections.emptyList()));
        // ASTNode temp = new ASTNode(
        //         new Terminal("Ite"), 
        //         List.of(
        //             new ASTNode(new Terminal("Lt"), List.of(new ASTNode(new Terminal("x"), Collections.emptyList()), new ASTNode(new Terminal("y"), Collections.emptyList()))), 
        //             new ASTNode(
        //                 new Terminal("Ite"), 
        //                 List.of(
        //                     new ASTNode(new Terminal("Lt"), List.of(new ASTNode(new Terminal("y"), Collections.emptyList()), new ASTNode(new Terminal("z"), Collections.emptyList()))),
        //                     new ASTNode(new Terminal("z"), Collections.emptyList()),
        //                     new ASTNode(new Terminal("y"), Collections.emptyList())
        //                 )
        //             ),
        //             new ASTNode(
        //                 new Terminal("Ite"), 
        //                 List.of(
        //                     new ASTNode(new Terminal("Lt"), List.of(new ASTNode(new Terminal("x"), Collections.emptyList()), new ASTNode(new Terminal("z"), Collections.emptyList()))),
        //                     new ASTNode(new Terminal("z"), Collections.emptyList()),
        //                     new ASTNode(new Terminal("x"), Collections.emptyList())
        //                 )
        //             )
        //         )
        //     );
        // System.out.println(isSatisfiable(temp, examples));
        // workList.add(temp);

        while (!workList.isEmpty()) {
            ASTNode node = workList.remove();

            // If the node is complete, evaluate the program and check if it satisfies all examples
            if (node.isComplete()) {
                Program program = new Program(node);

                if (isValid(program, examples)) {
                    return program;
                }
            }
            // Otherwise, expand the node if its abstract syntax tree is satisfiable for all examples
            else if (isSatisfiable(node, examples)) {
                workList.addAll(expand(node, cfg));
            }
        }

        return null;    
    }

    /**
     * Check if the AST node is satisfiable for all examples using the Z3 SMT solver
     * 
     * @param node
     * @param examples
     * @return true if the node is satisfiable for all examples, false otherwise
     */
    private boolean isSatisfiable(ASTNode node, List<Example> examples) {
        // Initialize the Z3 context and solver
        Context ctx = new Context();
        Solver solver = ctx.mkSolver();
        // Reset the counter for the non-terminal symbol E and B
        eCount = 0;
        bCount = 0;

        // Use the Z3 SMT solver to check if the node is satisfiable
        Expr expr = toZ3Expr(node, ctx);
        for (Example example : examples) {
            for (Map.Entry<String, Integer> entry : example.getInput().entrySet()) {
                solver.add(ctx.mkEq(ctx.mkIntConst(entry.getKey()), ctx.mkInt(entry.getValue())));
            }
            solver.add(ctx.mkEq(expr, ctx.mkInt(example.getOutput())));

            if (solver.check() == com.microsoft.z3.Status.UNSATISFIABLE) {
                // System.out.println(Arrays.toString(solver.getAssertions()) + " is unsatisfiable");
                ctx.close();
                return false;
            } else {
                solver.reset();
            }
        }

        ctx.close();
        return true;
    }

    /**
     * Convert the AST node to a Z3 expression
     * 
     * @param node
     * @param ctx
     * @return the Z3 expression
     */
    private Expr toZ3Expr(ASTNode node, Context ctx) {
        switch (node.getSymbol().toString()) {
            case "Lt":
            case "Eq":
            case "And":
            case "Or":
            case "Not":
            case "B":
                return toZ3BoolExpr(node, ctx);
            case "Ite":
            case "x":
            case "y":
            case "z":
            case "1":
            case "2":
            case "3":
            case "Add":
            case "Multiply":
            case "E":
                return toZ3ArithmeticExpr(node, ctx);
            default:
                throw new RuntimeException("Unknown symbol: " + node.getSymbol());
        }
    }

    private BoolExpr toZ3BoolExpr(ASTNode node, Context ctx) {
        switch (node.getSymbol().toString()) {
            case "Lt":
                return ctx.mkLt(toZ3ArithmeticExpr(node.getChild(0), ctx), toZ3ArithmeticExpr(node.getChild(1), ctx));
            case "Eq":
                return ctx.mkEq(toZ3Expr(node.getChild(0), ctx), toZ3Expr(node.getChild(1), ctx));
            case "And":
                return ctx.mkAnd(toZ3BoolExpr(node.getChild(0), ctx), toZ3BoolExpr(node.getChild(1), ctx));
            case "Or":
                return ctx.mkOr(toZ3BoolExpr(node.getChild(0), ctx), toZ3BoolExpr(node.getChild(1), ctx));
            case "Not":
                return ctx.mkNot(toZ3BoolExpr(node.getChild(0), ctx));
            case "B":
                return ctx.mkBoolConst("B" + bCount++);
            default:
                throw new RuntimeException("Unknown symbol: " + node.getSymbol());
        }
    }

    private ArithExpr toZ3ArithmeticExpr(ASTNode node, Context ctx) {
        switch (node.getSymbol().toString()) {
            case "Ite":
                return (IntExpr) ctx.mkITE(toZ3BoolExpr(node.getChild(0), ctx), toZ3Expr(node.getChild(1), ctx), toZ3Expr(node.getChild(2), ctx));
            case "x":
                return ctx.mkIntConst("x");
            case "y":
                return ctx.mkIntConst("y");
            case "z":
                return ctx.mkIntConst("z");
            case "1":
                return ctx.mkInt(1);
            case "2":
                return ctx.mkInt(2);
            case "3":
                return ctx.mkInt(3);
            case "Add":
                return ctx.mkAdd(toZ3ArithmeticExpr(node.getChild(0), ctx), toZ3ArithmeticExpr(node.getChild(1), ctx));
            case "Multiply":
                return ctx.mkMul(toZ3ArithmeticExpr(node.getChild(0), ctx), toZ3ArithmeticExpr(node.getChild(1), ctx));
            case "E":
                return ctx.mkIntConst("E" + eCount++);
            default:
                throw new RuntimeException("Unknown symbol: " + node.getSymbol());
        }
    }
}