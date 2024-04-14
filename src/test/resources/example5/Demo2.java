class Demo2 {

    static String getUserPermission(String user) {
        // data receive from internet
        return "select_from_db";
    }

    static boolean isAdmin(String s) {
        // auth here
        return s.equals("root");
    }

    static void printSomethingIfPrivileged(boolean isAdmin) {
        if (isAdmin) {
            System.out.println("wo bu zuo ren la!");
        }
    }

    public static void main(String[] args) {
        String user = "DIO";
        String a = getUserPermission(user);
        String str = a;
        a = "";
        boolean isAdmin = false;
        if (isAdmin(str)) {
            isAdmin = true;
        }
        printSomethingIfPrivileged(isAdmin);
    }

}
