package cn.koda.example;


import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

import java.util.Collections;
import java.util.Set;

public class Example5 {
    public static void main(String[] args) {
        // Create a analysis input location.
        JavaSourcePathAnalysisInputLocation inputLocation = new JavaSourcePathAnalysisInputLocation("src/test/resources/example4/");
        JavaView view = new JavaView(inputLocation);

        // Create CHA
        RapidTypeAnalysisAlgorithm rta = new RapidTypeAnalysisAlgorithm(view);

        // Create type hierarchy
        ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);

        // Get A classType
        JavaClassType classTypeA = view.getIdentifierFactory().getClassType("A");
        Set<ClassType> subClassTypes = typeHierarchy.subtypesOf(classTypeA);
//        System.out.println("subClassTypes of A: " + subClassTypes);

        // entry method signature
        ClassType classTypeDemo = view.getIdentifierFactory().getClassType("Demo1");
        MethodSignature entryMethodSignature = view.getIdentifierFactory().
                getMethodSignature(classTypeDemo,
                        "main",
                        "void",
                        Collections.singletonList("java.lang.String[]"));

        // sootup's CHA
        CallGraph cg = rta.initialize();
        cg.callsFrom(entryMethodSignature).forEach(System.out::println);
    }

}
