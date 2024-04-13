package cn.koda.example;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.DotExporter;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

public class Example2 {
    public static void main(String[] args) {
        JavaSourcePathAnalysisInputLocation inputLocation
                = new JavaSourcePathAnalysisInputLocation("src/test/resources/example2/");
        JavaView view = new JavaView(inputLocation);
        JavaClassType classType = view.getIdentifierFactory().getClassType("Demo2");
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
                        Collections.singletonList("int"));
        Optional<JavaSootMethod> method = view.getMethod(methodSignature);
        if (!method.isPresent()) {
            System.out.println("Method not found");
            return;
        }

        JavaSootMethod sootMethod = method.get();

        System.out.println("method signature: " + sootMethod.getSignature());

        System.out.println(sootMethod.getBody());

        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        String dotStr = DotExporter.buildGraph(stmtGraph, false, null, methodSignature);
        try {
            Files.write(Paths.get("src/test/resources/example2/Demo2.dot"), dotStr.getBytes());
            System.out.println("Dot file written to src/main/resources/example2/Demo2.dot .");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
