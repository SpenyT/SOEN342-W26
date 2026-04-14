import java.io.File;

public class Main {
    public static void main(String[] args) {
        new File("data").mkdirs();
        new Console().run();
    }
}
