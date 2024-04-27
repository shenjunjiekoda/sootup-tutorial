
public class Example6 {
    public Example6() {
    }

    public static int inc(int a) {
        int b = a + 1;
        return b;
    }

    public static void f() {
        int a = 1;
        int b = inc(a);
    }

    public static void g() {
        int a = 2;
        int b = inc(a);
    }

    public static void entryMethod() {
        f();
        g();
    }
}