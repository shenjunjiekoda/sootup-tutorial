public class Test1 {
    public static void foo(int a, int b) {
        int x = a + b;
        int y = a * b;
        while (y > a + b) {
            a --;
            x = a + b;
        }
    }
}