package cn.koda.example;

import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

public class Example6 {
    public static void main(String[] args) {
        JavaSourcePathAnalysisInputLocation inputLocation = new JavaSourcePathAnalysisInputLocation("src/test/resources/example6/");
        JavaView view = new JavaView(inputLocation);

        JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
        JavaClassType mainClassSignature = identifierFactory.getClassType("Example6");

        SootClass sc = view.getClass(mainClassSignature).get();
        SootMethod entryMethod =
                sc.getMethods().stream().filter(e -> e.getName().equals("entryMethod")).findFirst().get();

        MethodSignature entryMethodSignature = entryMethod.getSignature();

        JimpleBasedInterproceduralCFG icfg =
                new JimpleBasedInterproceduralCFG(view, entryMethodSignature, false, false);

        ClassHierarchyAnalysisAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);
        CallGraph cg = cha.initialize(Collections.singletonList(entryMethodSignature));
        cg.callsFrom(entryMethodSignature).forEach(System.out::println);
        String dotStr = icfg.buildICFGGraph(cg);
        try {
            Files.write(Paths.get("src/test/resources/example6/ICFG.dot"), dotStr.getBytes());
            System.out.println("Dot file written to src/test/resources/example6/ICFG.dot.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
