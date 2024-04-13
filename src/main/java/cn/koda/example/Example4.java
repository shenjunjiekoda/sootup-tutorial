package cn.koda.example;

import sootup.analysis.intraprocedural.ForwardFlowAnalysis;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.expr.AbstractUnopExpr;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class Example4 {

    public static class SignDomain {
        public enum Sign {
            POSITIVE, NEGATIVE, ZERO, TOP, BOTTOM
        }

        private final Sign sign;
        SignDomain(Sign sign) {
            this.sign = sign;
        }

        SignDomain(int x) {
            if (x > 0) {
                sign = Sign.POSITIVE;
            } else if (x < 0) {
                sign = Sign.NEGATIVE;
            } else {
                sign = Sign.ZERO;
            }

        }

        public Sign getSign () {
            return sign;
        }

        public static SignDomain positive() {
            return new SignDomain(Sign.POSITIVE);
        }
        public static SignDomain negative() {
            return new SignDomain(Sign.NEGATIVE);
        }
        public static SignDomain zero() {
            return new SignDomain(Sign.ZERO);
        }
        public static SignDomain top() {
            return new SignDomain(Sign.TOP);
        }
        public static SignDomain bottom() {
            return new SignDomain(Sign.BOTTOM);
        }

        // least upper bound
        public static SignDomain join(SignDomain a, SignDomain b) {
            if (a.getSign() == Sign.TOP || b.getSign() == Sign.TOP){
                return top();
            }
            if (a.getSign() == Sign.BOTTOM) {
                return b;
            }
            if (b.getSign() == Sign.BOTTOM) {
                return a;
            }
            if (a.getSign() == b.getSign()) {
                return a;
            }
            return top();
        }

        public static SignDomain plus(SignDomain a, SignDomain b) {
            if (a.getSign() == Sign.TOP || b.getSign() == Sign.TOP) {
                return top();
            }
            if (a.getSign() == Sign.BOTTOM || b.getSign() == Sign.BOTTOM) {
                return bottom();
            }
            if (a.getSign() == Sign.POSITIVE) {
                if (b.getSign() == Sign.POSITIVE) {
                    return positive();
                }
                if (b.getSign() == Sign.NEGATIVE) {
                    return top();
                }
                if (b.getSign() == Sign.ZERO) {
                    return positive();
                }
            }
            if (a.getSign() == Sign.NEGATIVE) {
                if (b.getSign() == Sign.POSITIVE) {
                    return top();
                }
                if (b.getSign() == Sign.NEGATIVE) {
                    return negative();
                }
                if (b.getSign() == Sign.ZERO) {
                    return negative();
                }
            }
            if (a.getSign() == Sign.ZERO) {
                return b;
            }
            return bottom();
        }

        public static SignDomain minus(SignDomain a, SignDomain b) {
            if (a.getSign() == Sign.TOP || b.getSign() == Sign.TOP) {
                return top();
            }
            if (a.getSign() == Sign.BOTTOM || b.getSign() == Sign.BOTTOM) {
                return bottom();
            }
            if (a.getSign() == Sign.POSITIVE) {
                if (b.getSign() == Sign.POSITIVE) {
                    return positive();
                }
                if (b.getSign() == Sign.NEGATIVE) {
                    return top();
                }
                if (b.getSign() == Sign.ZERO) {
                    return positive();
                }
            }
            if (a.getSign() == Sign.NEGATIVE) {
                if (b.getSign() == Sign.POSITIVE) {
                    return top();
                }
                if (b.getSign() == Sign.NEGATIVE) {
                    return negative();
                }
                if (b.getSign() == Sign.ZERO) {
                    return negative();
                }
            }
            if (a.getSign() == Sign.ZERO) {
                return b;
            }
            return bottom();
        }

        public static SignDomain multiply(SignDomain a, SignDomain b) {
            if (a.getSign() == Sign.TOP || b.getSign() == Sign.TOP) {
                return top();
            }
            if (a.getSign() == Sign.BOTTOM || b.getSign() == Sign.BOTTOM) {
                return bottom();
            }
            if (a.getSign() == Sign.POSITIVE) {
                if (b.getSign() == Sign.POSITIVE) {
                    return positive();
                }
                if (b.getSign() == Sign.NEGATIVE) {
                    return top();
                }
                if (b.getSign() == Sign.ZERO) {
                    return positive();
                }
            }
            if (a.getSign() == Sign.NEGATIVE) {
                if (b.getSign() == Sign.POSITIVE) {
                    return top();
                }
                if (b.getSign() == Sign.NEGATIVE) {
                    return negative();
                }
                if (b.getSign() == Sign.ZERO) {
                    return negative();
                }
            }
            if (a.getSign() == Sign.ZERO) {
                return b;
            }
            return bottom();
        }

        public static SignDomain divide(SignDomain a, SignDomain b) {
            if (a.getSign() == Sign.TOP || b.getSign() == Sign.TOP) {
                return top();
            }
            if (a.getSign() == Sign.BOTTOM || b.getSign() == Sign.BOTTOM) {
                return bottom();
            }
            if (a.getSign() == Sign.POSITIVE) {
                if (b.getSign() == Sign.POSITIVE) {
                    return positive();
                }
                if (b.getSign() == Sign.NEGATIVE) {
                    return top();
                }
                if (b.getSign() == Sign.ZERO) {
                    return positive();
                }
            }
            if (a.getSign() == Sign.NEGATIVE) {
                if (b.getSign() == Sign.POSITIVE) {
                    return top();
                }
                if (b.getSign() == Sign.NEGATIVE) {
                    return negative();
                }
                if (b.getSign() == Sign.ZERO) {
                    return negative();
                }
            }
            if (a.getSign() == Sign.ZERO) {
                return b;
            }
            return bottom();
        }

        public static SignDomain arithOp(SignDomain a, SignDomain b, String op) {
            switch(op) {
                case " + ":
                    return plus(a, b);
                case " - ":
                    return minus(a, b);
                case " * ":
                    return multiply(a, b);
                case " / ":
                    return divide(a, b);
            }
            return SignDomain.top();
        }

    }


    public static class SignAnalysis extends ForwardFlowAnalysis<Map<LValue, SignDomain>> {


        /**
         * Construct the analysis from StmtGraph.
         *
         * @param graph
         */
        public <B extends BasicBlock<B>> SignAnalysis(StmtGraph<B> graph) {
            super(graph);
        }

        public void run() {
            execute();
        }

        @Override
        protected void flowThrough(@Nonnull Map<LValue, SignDomain> in, Stmt d, @Nonnull Map<LValue, SignDomain> out) {
            copy(in, out);

            if (d instanceof AbstractDefinitionStmt) {
                AbstractDefinitionStmt defStmt = (AbstractDefinitionStmt) d;
                LValue def = defStmt.getLeftOp();
                SignDomain sign = SignDomain.top();

                Value rVal = defStmt.getRightOp();
                if (rVal instanceof AbstractBinopExpr) {
                    AbstractBinopExpr expr = (AbstractBinopExpr) rVal;
                    Immediate lhs = expr.getOp1();
                    Immediate rhs = expr.getOp2();

                    SignDomain lhsSign = out.getOrDefault(lhs, SignDomain.bottom());
                    SignDomain rhsSign = out.getOrDefault(rhs, SignDomain.bottom());

                    if (lhs instanceof IntConstant) {
                        lhsSign = new SignDomain(((IntConstant) lhs).getValue());
                    }
                    if (rhs instanceof IntConstant) {
                        rhsSign = new SignDomain(((IntConstant) rhs).getValue());
                    }

                    sign = SignDomain.arithOp(lhsSign, rhsSign, expr.getSymbol());
                } else if (rVal instanceof Immediate) {
                    sign = out.getOrDefault(rVal, SignDomain.bottom());
                    if (rVal instanceof IntConstant) {
                        sign = new SignDomain(((IntConstant)rVal).getValue());
                    }
                }
                out.put(def, sign);
            }
        }

        @Nonnull
        @Override
        protected Map<LValue, SignDomain> newInitialFlow() {
            return new HashMap<>();
        }

        @Override
        protected void merge(@Nonnull Map<LValue, SignDomain> in1, @Nonnull Map<LValue, SignDomain> in2, @Nonnull Map<LValue, SignDomain> out) {
            copy(in1, out);
            for (Map.Entry<LValue, SignDomain> entry : in2.entrySet()) {
                LValue key = entry.getKey();
                SignDomain val = entry.getValue();
                if (out.containsKey(key)) {
                    out.put(key, SignDomain.join(out.get(key), val));
                } else {
                    out.put(key, val);
                }
            }
        }

        @Override
        protected void copy(@Nonnull Map<LValue, SignDomain> source, @Nonnull Map<LValue, SignDomain> dest) {
            dest.putAll(source);
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

        SignAnalysis analysis = new SignAnalysis(stmtGraph);
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
