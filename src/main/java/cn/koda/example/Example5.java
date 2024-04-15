package cn.koda.example;

import cn.koda.util.PostDominanceFinder;
import heros.solver.Pair;
import sootup.analysis.intraprocedural.ForwardFlowAnalysis;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.MutableBasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
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

public class Example5 {

    static class TaintDom {
        boolean isTainted;

        public TaintDom(boolean isTainted) {
            this.isTainted = isTainted;
        }

        TaintDom join(TaintDom other) {
            return new TaintDom(other.isTainted || this.isTainted);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TaintDom taintDom = (TaintDom) o;
            return isTainted == taintDom.isTainted;
        }

        @Override
        public int hashCode() {
            return Objects.hash(isTainted);
        }

        @Override
        public String toString() {
            return "TaintDom{" +
                    "isTainted=" + isTainted +
                    '}';
        }

        public static TaintDom getTainted() {
            return new TaintDom(true);
        }

        public static TaintDom getUntainted() {
            return new TaintDom(false);
        }
    }

    static class InfoFlowAnalysis extends ForwardFlowAnalysis<Map<LValue, TaintDom>> {

        boolean considerImplicitFlow;
        PostDominanceFinder postDom;

        Stack<Pair<AbstractConditionExpr, TaintDom>> stk;
        TaintDom condVal;

        Stmt imPostDomStmt;
        // nondetermined execution in simple worklist algorithm
        boolean visitControlBodyStmt;
        Stmt stmtInControlBody;

        /**
         * Construct the analysis from StmtGraph.
         *
         * @param graph
         */
        public <B extends BasicBlock<B>> InfoFlowAnalysis(StmtGraph<B> cfg, boolean considerImplicitFlow) {
            super(cfg);
            this.considerImplicitFlow = considerImplicitFlow;
            if (considerImplicitFlow) {
                this.postDom = new PostDominanceFinder(cfg);
                // hack for visit control body first
                this.visitControlBodyStmt = false;
                this.stmtInControlBody = null;
                //save cond status
                this.stk = new Stack<>();
                // cached cond val merged for all stack element
                this.condVal = TaintDom.getUntainted();
                // dominator of cond
                this.imPostDomStmt = null;
            }
        }

        void run() {
            execute();
        }

        public boolean isSource(MethodSignature signature) {
            String name = signature.getName().toLowerCase();
            return name.contains("password") || name.contains("permission") || name.contains("secret");
        }

        public boolean isSink(MethodSignature signature) {
            String name = signature.getName().toLowerCase();
            return name.contains("internet") || name.contains("print");
        }

        void report(Stmt s) {
            System.out.println("Find security issue in: " + s);
            System.out.println("Location: " + s.getPositionInfo() + "\n");
        }

        TaintDom eval(Value v, Map<LValue, TaintDom> in) {
            if (in.containsKey(v)) {
                return in.get(v);
            }
            TaintDom val = TaintDom.getUntainted();
            for (Value use: v.getUses()) {
                val = val.join(eval(use, in));
            }
            return val;
        }

        @Override
        protected void flowThrough(@Nonnull Map<LValue, TaintDom> in, Stmt s, @Nonnull Map<LValue, TaintDom> out) {
            copy(in, out);
            if (considerImplicitFlow) {
                if (s.equals(stmtInControlBody)) {
                    visitControlBodyStmt = true;
                    stmtInControlBody = null;
                }

                if (!stk.isEmpty() && s.equals(imPostDomStmt) && visitControlBodyStmt) {
                    Pair<AbstractConditionExpr, TaintDom> peek = stk.pop();
                    visitControlBodyStmt = false;
                }

                if (s instanceof JIfStmt) {
                    AbstractConditionExpr cond = ((JIfStmt) s).getCondition();
                    stk.push(new Pair<>(cond, eval(cond, in)));
                    BasicBlock<?> curBlock = graph.getBlockOf(s);
                    BasicBlock<?> immediateDominator = postDom.getImmediateDominator(curBlock);
                    if (immediateDominator != null ) {
                        imPostDomStmt = immediateDominator.getHead();
                        // find the control body stmt hack.
                        List<MutableBasicBlock> successors = (List<MutableBasicBlock>)curBlock.getSuccessors();
                        stmtInControlBody = successors.stream()
                                .filter(bb -> !bb.getHead().equals(imPostDomStmt))
                                .collect(Collectors.toList()).get(0).getHead();
                    }
                    visitControlBodyStmt = false;
                    if (!stk.isEmpty()) {
                        condVal = TaintDom.getUntainted();
                        for (Pair<AbstractConditionExpr, TaintDom> pair : stk) {
                            condVal = condVal.join(pair.getO2());
                        }
                        return;
                    }
                }
            }

            if (s instanceof AbstractDefinitionStmt) {
                AbstractDefinitionStmt defS = (AbstractDefinitionStmt)s;
                LValue def = defS.getLeftOp();
                if (s.containsInvokeExpr() && isSource(s.getInvokeExpr().getMethodSignature())) {
                    out.put(def, TaintDom.getTainted());
                } else {
                    TaintDom val = eval(defS.getRightOp(), in);
                    if (considerImplicitFlow) {
                        val = val.join(condVal);
                    }
                    out.put(def, val);
                }
            }

            if (s.containsInvokeExpr()) {
                AbstractInvokeExpr call = s.getInvokeExpr();
                if (isSink(call.getMethodSignature())) {
                    boolean isTainted = call.getArgs()
                            .stream()
                            .anyMatch(arg -> {
                                TaintDom val = eval(arg, in);
                                if (considerImplicitFlow) {
                                    val = val.join(condVal);
                                }
                                return val.isTainted;
                            });
                    if (isTainted) {
                        report(s);
                    }
                }
            }

        }

        @Nonnull
        @Override
        protected Map<LValue, TaintDom> newInitialFlow() {
            return new HashMap<>();
        }

        @Override
        protected void merge(@Nonnull Map<LValue, TaintDom> in1, @Nonnull Map<LValue, TaintDom> in2, @Nonnull Map<LValue, TaintDom> out) {
            copy(in1, out);
            for (Map.Entry<LValue, TaintDom> entry : in2.entrySet()) {
                LValue key = entry.getKey();
                TaintDom val = entry.getValue();
                if (out.containsKey(key)) {
                    out.put(key, out.get(key).join(val));
                } else {
                    out.put(key, val);
                }
            }
        }

        @Override
        protected void copy(@Nonnull Map<LValue, TaintDom> source, @Nonnull Map<LValue, TaintDom> dest) {
            dest.putAll(source);
        }
    }

    public static void main(String[] args) {
        JavaSourcePathAnalysisInputLocation inputLocation = new JavaSourcePathAnalysisInputLocation("src/test/resources/example5/");
        JavaView view = new JavaView(inputLocation);
        JavaClassType classType = view.getIdentifierFactory().getClassType("Demo3");
        Optional<JavaSootClass> classOpt = view.getClass(classType);
        if (!classOpt.isPresent()) {
            System.out.println("class not found");
            return;
        }

        JavaSootClass sootClass = classOpt.get();
        MethodSignature methodSignature = view.getIdentifierFactory().getMethodSignature(
                classType,
                "main",
                "void",
                Collections.singletonList("java.lang.String[]"));
        Optional<JavaSootMethod> method = view.getMethod(methodSignature);
        if (!method.isPresent()) {
            System.out.println("Method not found");
            return;
        }

        JavaSootMethod sootMethod = method.get();

        StmtGraph<?> cfg = sootMethod.getBody().getStmtGraph();
        InfoFlowAnalysis analysis = new InfoFlowAnalysis(cfg, true);
        analysis.run();

    }

}
