public class Demo2 {
    public void foo(int x) {
        for (int i = 0; i < 5; i ++ ) {
            if (x % 2 == 0) {
                System.out.println("even");
            } else {
                System.out.println("odd");
            }
        }
    }
}