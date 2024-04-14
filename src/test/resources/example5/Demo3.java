class Demo3 {

    static String getSecret() {
        // data receive from internet
        return "6666666";
    }

    public static void main(String[] args) {
        String secret = getSecret();
        String publicInfo = "This is public information.";

        int secretLength = secret.length();
        String message = "";

        for (int i = 0; i < secretLength; i++) {
            message += "ou la";
        }

        System.out.println("Jo ta ro say: " + message);
    }
}

