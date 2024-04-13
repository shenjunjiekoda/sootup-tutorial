package cn.koda.example;

import sootup.analysis.intraprocedural.ForwardFlowAnalysis;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.expr.AbstractUnopExpr;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.DotExporter;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Example3 {

    public static class AvailExprAnalysis extends ForwardFlowAnalysis<Set<Expr>> {

        /**
         * Construct the analysis from StmtGraph.
         *
         * @param graph
         */
        public <B extends BasicBlock<B>> AvailExprAnalysis(StmtGraph<B> graph) {
            super(graph);
        }

        // Transfer function
        @Override
        protected void flowThrough(@Nonnull Set<Expr> in, Stmt d, @Nonnull Set<Expr> out) {
            // NOTICE: We ignore `InvokeStmt` here.

            // Gen, Kill
            Set<Expr> gen = new HashSet<>();
            Set<Expr> kill = new HashSet<>();

            // Out = In + gen - kill

            // Handle `gen`
            d.getUses().stream().filter(v -> v instanceof AbstractUnopExpr ||
                    v instanceof AbstractBinopExpr || v instanceof JCastExpr)
                    .forEach(v -> gen.add((Expr)v));

            out.addAll(in);
            out.addAll(gen);

            // Handle `kill`
            if (d instanceof AbstractDefinitionStmt) {
                AbstractDefinitionStmt defStmt = (AbstractDefinitionStmt) d;
                LValue def = defStmt.getLeftOp();

                out.stream().filter(expr -> expr.getUses().contains(def))
                        .collect(Collectors.toCollection(() -> kill));
            }
            out.removeAll(kill);

            // For debug
            // print `in`, `kill`, `gen`, `out`
            System.out.println("--------------------");
            System.out.println("Stmt: " + d);
            System.out.println("In: " + in);
            System.out.println("gen: " + gen);
            System.out.println("kill: " + kill);
            System.out.println("out: " + out);
            System.out.println("--------------------");
        }

        // Boundary
        @Nonnull
        @Override
        protected Set<Expr> newInitialFlow() {
            return new HashSet<>();
        }

        // Merge
        @Override
        protected void merge(@Nonnull Set<Expr> in1, @Nonnull Set<Expr> in2, @Nonnull Set<Expr> out) {
            // Intersection of incoming values
            out.addAll(in1);
            out.retainAll(in2);
        }

        // Helper
        @Override
        protected void copy(@Nonnull Set<Expr> source, @Nonnull Set<Expr> dest) {
            dest.addAll(source);
        }

        public void run() {
            execute();
        }
    }

    public static void main(String[] args) {
        JavaSourcePathAnalysisInputLocation inputLocation
                = new JavaSourcePathAnalysisInputLocation("src/test/resources/example3/");
        JavaView view = new JavaView(inputLocation);
        JavaClassType classType = view.getIdentifierFactory().getClassType("Test1");
        Optional<JavaSootClass> classOpt = view.getClass(classType);
        if (!classOpt.isPresent()) {
            System.out.println("Class not found");
            return;
        }

        JavaSootClass sootClass = classOpt.get();
        MethodSignature methodSignature = view.getIdentifierFactory()
                .getMethodSignature(classType,
                        "foo",
                        "void",
                        new ArrayList<String>(2) {
                            {
                                add("int");
                                add("int");
                            }
                        });
        Optional<JavaSootMethod> method = view.getMethod(methodSignature);
        if (!method.isPresent()) {
            System.out.println("Method not found");
            return;
        }

        JavaSootMethod sootMethod = method.get();

        System.out.println("method signature: " + sootMethod.getSignature());

        System.out.println(sootMethod.getBody());

        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        AvailExprAnalysis analysis = new AvailExprAnalysis(stmtGraph);
        analysis.run();

        // Output the result
        stmtGraph.getStmts().stream().forEach(stmt->
        {
            System.out.println("====================");
            System.out.println("Stmt: [" + stmt + "]");
            System.out.println("before: " + analysis.getFlowBefore(stmt));
            System.out.println("after: " + analysis.getFlowAfter(stmt));
            System.out.println("====================");
        });

    }
}
