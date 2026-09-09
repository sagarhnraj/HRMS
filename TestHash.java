import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("admin123"));
        System.out.println(encoder.matches("admin123", "$2a$10$C8.M/6Q1E1nI8/VXY5Q2n.E3lM5rR4L0B9kO/w3x2Kj/2.9sO0K.e"));
    }
}
