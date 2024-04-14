class Demo1 {

    static void sendToInternet(String data) {
        System.out.println(data);
    }

    static String getPassword(String user) {
        // mock
        return "";
    }

    public static void main(String[] args) {
        String user = "DIO";
        String passwd = getPassword(user);
        String info = user + passwd;
        sendToInternet(info);
    }

}
