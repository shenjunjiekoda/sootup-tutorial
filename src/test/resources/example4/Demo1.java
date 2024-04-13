class A {
    void foo() {
        System.out.println("A::foo()");
    }
}

class B extends A {
}

class C extends A {
    void foo() {
        System.out.println("C::foo()");
    }
}


public class Demo1 {
    public static void main(String[] args) {
        A a = new A();
        a.foo();
    }
}

