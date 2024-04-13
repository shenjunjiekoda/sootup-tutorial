package cn.koda.example;

import sootup.core.Language;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

import java.util.Collections;
import java.util.Optional;

public class Example1 {
    public static void main(String[] args) {
        JavaSourcePathAnalysisInputLocation inputLocation
                = new JavaSourcePathAnalysisInputLocation("src/test/resources/example1/");
        JavaView view = new JavaView(inputLocation);
        JavaClassType classType = view.getIdentifierFactory().getClassType("HelloWorld");
        Optional<JavaSootClass> classOpt = view.getClass(classType);
        if (!classOpt.isPresent()) {
            System.out.println("Class not found");
            return;
        }

        JavaSootClass sootClass = classOpt.get();
        MethodSignature methodSignature = view.getIdentifierFactory()
                .getMethodSignature(classType,
                        "main",
                        "void",
                        Collections.singletonList("java.lang.String[]"));
        Optional<JavaSootMethod> method = view.getMethod(methodSignature);
        if (!method.isPresent()) {
            System.out.println("Method not found");
            return;
        }

        JavaSootMethod sootMethod = method.get();

        System.out.println("method signature: " + sootMethod.getSignature());

        System.out.println(sootMethod.getBody());

        boolean found = sootMethod.getBody().getStmts().stream().anyMatch(
                stmt->stmt instanceof JInvokeStmt
                && stmt.getInvokeExpr() instanceof JVirtualInvokeExpr
                && stmt.getInvokeExpr().getMethodSignature().getName().equals("println")
                && stmt.getInvokeExpr().getArg(0).equivTo(JavaJimple.getInstance().newStringConstant("Hello World!"))
        );

        if (found) {
            System.out.println("find println invoke stmt");
        } else {
            System.out.println("not find println invoke stmt");
        }
    }
}
